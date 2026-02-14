package com.clinica.auth.application.service;

import com.clinica.auth.domain.exception.CredencialesInvalidasException;
import com.clinica.auth.domain.exception.RefreshTokenInvalidoException;
import com.clinica.auth.domain.exception.UsuarioYaExisteException;
import com.clinica.auth.domain.model.RefreshToken;
import com.clinica.auth.domain.model.Rol;
import com.clinica.auth.domain.model.Usuario;
import com.clinica.auth.domain.port.in.LoginUseCase;
import com.clinica.auth.domain.port.in.RegistrarUsuarioUseCase;
import com.clinica.auth.domain.port.out.AuthEventPublisherPort;
import com.clinica.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.clinica.auth.domain.port.out.UsuarioRepositoryPort;
import com.clinica.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private AuthEventPublisherPort eventPublisher;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Registrar Usuario")
    class RegistrarTests {

        @Test
        @DisplayName("Debe registrar usuario exitosamente")
        void debeRegistrarUsuarioExitosamente() {
            // Given
            var command = new RegistrarUsuarioUseCase.RegistrarUsuarioCommand(
                    "maria@clinica.com", "password123", "Maria", "Garcia", Rol.OBSTETRA);

            when(usuarioRepository.existsByEmail("maria@clinica.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

            // When
            Usuario result = authService.registrar(command);

            // Then
            assertThat(result.getEmail()).isEqualTo("maria@clinica.com");
            assertThat(result.getNombre()).isEqualTo("Maria");
            assertThat(result.getApellido()).isEqualTo("Garcia");
            assertThat(result.getRol()).isEqualTo(Rol.OBSTETRA);
            assertThat(result.isActivo()).isTrue();
            assertThat(result.getPasswordHash()).isEqualTo("hashedPassword");

            verify(eventPublisher).publish(any());
        }

        @Test
        @DisplayName("Debe lanzar excepcion si email ya existe")
        void debeLanzarExcepcionSiEmailYaExiste() {
            var command = new RegistrarUsuarioUseCase.RegistrarUsuarioCommand(
                    "maria@clinica.com", "password123", "Maria", "Garcia", Rol.OBSTETRA);

            when(usuarioRepository.existsByEmail("maria@clinica.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.registrar(command))
                    .isInstanceOf(UsuarioYaExisteException.class);

            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        private Usuario usuarioActivo;

        @BeforeEach
        void setUp() {
            usuarioActivo = Usuario.builder()
                    .id(UUID.randomUUID())
                    .email("maria@clinica.com")
                    .passwordHash("hashedPassword")
                    .nombre("Maria")
                    .apellido("Garcia")
                    .rol(Rol.OBSTETRA)
                    .activo(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }

        @Test
        @DisplayName("Debe hacer login exitosamente")
        void debeHacerLoginExitosamente() {
            var command = new LoginUseCase.LoginCommand("maria@clinica.com", "password123");

            when(usuarioRepository.findByEmail("maria@clinica.com")).thenReturn(Optional.of(usuarioActivo));
            when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("jwt-token");
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

            LoginUseCase.TokenResponse result = authService.login(command);

            assertThat(result.accessToken()).isEqualTo("jwt-token");
            assertThat(result.refreshToken()).isNotBlank();

            verify(refreshTokenRepository).revokeAllByUsuarioId(usuarioActivo.getId());
            verify(refreshTokenRepository).save(any(RefreshToken.class));
            verify(eventPublisher).publish(any());
        }

        @Test
        @DisplayName("Debe lanzar excepcion con email incorrecto")
        void debeLanzarExcepcionConEmailIncorrecto() {
            var command = new LoginUseCase.LoginCommand("noexiste@clinica.com", "password123");
            when(usuarioRepository.findByEmail("noexiste@clinica.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(command))
                    .isInstanceOf(CredencialesInvalidasException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepcion con password incorrecto")
        void debeLanzarExcepcionConPasswordIncorrecto() {
            var command = new LoginUseCase.LoginCommand("maria@clinica.com", "wrongpassword");
            when(usuarioRepository.findByEmail("maria@clinica.com")).thenReturn(Optional.of(usuarioActivo));
            when(passwordEncoder.matches("wrongpassword", "hashedPassword")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(command))
                    .isInstanceOf(CredencialesInvalidasException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepcion si usuario inactivo")
        void debeLanzarExcepcionSiUsuarioInactivo() {
            Usuario inactivo = Usuario.builder()
                    .id(UUID.randomUUID())
                    .email("maria@clinica.com")
                    .passwordHash("hashedPassword")
                    .activo(false)
                    .rol(Rol.OBSTETRA)
                    .build();

            var command = new LoginUseCase.LoginCommand("maria@clinica.com", "password123");
            when(usuarioRepository.findByEmail("maria@clinica.com")).thenReturn(Optional.of(inactivo));

            assertThatThrownBy(() -> authService.login(command))
                    .isInstanceOf(CredencialesInvalidasException.class);
        }
    }

    @Nested
    @DisplayName("Refresh Token")
    class RefreshTests {

        @Test
        @DisplayName("Debe renovar tokens exitosamente")
        void debeRenovarTokensExitosamente() {
            UUID userId = UUID.randomUUID();
            RefreshToken validToken = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .usuarioId(userId)
                    .token("valid-refresh-token")
                    .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                    .revoked(false)
                    .createdAt(Instant.now())
                    .build();

            Usuario usuario = Usuario.builder()
                    .id(userId)
                    .email("maria@clinica.com")
                    .rol(Rol.OBSTETRA)
                    .build();

            when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validToken));
            when(usuarioRepository.findById(userId)).thenReturn(Optional.of(usuario));
            when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("new-jwt");
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

            LoginUseCase.TokenResponse result = authService.refresh("valid-refresh-token");

            assertThat(result.accessToken()).isEqualTo("new-jwt");
            assertThat(result.refreshToken()).isNotBlank();
            assertThat(result.refreshToken()).isNotEqualTo("valid-refresh-token");

            verify(refreshTokenRepository).revokeAllByUsuarioId(userId);
        }

        @Test
        @DisplayName("Debe lanzar excepcion con token expirado")
        void debeLanzarExcepcionConTokenExpirado() {
            RefreshToken expired = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .usuarioId(UUID.randomUUID())
                    .token("expired-token")
                    .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                    .revoked(false)
                    .createdAt(Instant.now().minus(8, ChronoUnit.DAYS))
                    .build();

            when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

            assertThatThrownBy(() -> authService.refresh("expired-token"))
                    .isInstanceOf(RefreshTokenInvalidoException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepcion con token revocado")
        void debeLanzarExcepcionConTokenRevocado() {
            RefreshToken revoked = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .usuarioId(UUID.randomUUID())
                    .token("revoked-token")
                    .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                    .revoked(true)
                    .createdAt(Instant.now())
                    .build();

            when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revoked));

            assertThatThrownBy(() -> authService.refresh("revoked-token"))
                    .isInstanceOf(RefreshTokenInvalidoException.class);
        }

        @Test
        @DisplayName("Debe lanzar excepcion con token inexistente")
        void debeLanzarExcepcionConTokenInexistente() {
            when(refreshTokenRepository.findByToken("no-existe")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refresh("no-existe"))
                    .isInstanceOf(RefreshTokenInvalidoException.class);
        }
    }
}
