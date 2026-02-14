package com.clinica.auth.application.service;

import com.clinica.auth.domain.exception.CredencialesInvalidasException;
import com.clinica.auth.domain.exception.RefreshTokenInvalidoException;
import com.clinica.auth.domain.exception.UsuarioYaExisteException;
import com.clinica.auth.domain.model.RefreshToken;
import com.clinica.auth.domain.model.Usuario;
import com.clinica.auth.domain.port.in.LoginUseCase;
import com.clinica.auth.domain.port.in.RefreshTokenUseCase;
import com.clinica.auth.domain.port.in.RegistrarUsuarioUseCase;
import com.clinica.auth.domain.port.out.AuthEventPublisherPort;
import com.clinica.auth.domain.port.out.RefreshTokenRepositoryPort;
import com.clinica.auth.domain.port.out.UsuarioRepositoryPort;
import com.clinica.shared.domain.event.AuditableEvent;
import com.clinica.shared.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements RegistrarUsuarioUseCase, LoginUseCase, RefreshTokenUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final AuthEventPublisherPort eventPublisher;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Usuario registrar(RegistrarUsuarioCommand command) {
        if (usuarioRepository.existsByEmail(command.email())) {
            throw new UsuarioYaExisteException(command.email());
        }

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .nombre(command.nombre())
                .apellido(command.apellido())
                .rol(command.rol())
                .activo(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Usuario saved = usuarioRepository.save(usuario);

        eventPublisher.publish(AuditableEvent.create(
                "auth.usuario.registrado",
                saved.getId(),
                saved.getEmail(),
                "CREATE",
                "USUARIO",
                saved.getId(),
                null,
                null,
                null,
                "ms-auth"
        ));

        return saved;
    }

    @Override
    @Transactional
    public TokenResponse login(LoginCommand command) {
        Usuario usuario = usuarioRepository.findByEmail(command.email())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException();
        }

        if (!passwordEncoder.matches(command.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                usuario.getId(), usuario.getEmail(), usuario.getRol().name());

        // Revocar refresh tokens anteriores
        refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());

        // Crear nuevo refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuario.getId())
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(refreshToken);

        eventPublisher.publish(AuditableEvent.create(
                "auth.usuario.login",
                usuario.getId(),
                usuario.getEmail(),
                "LOGIN",
                "USUARIO",
                usuario.getId(),
                null,
                null,
                null,
                "ms-auth"
        ));

        return new TokenResponse(accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public TokenResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(RefreshTokenInvalidoException::new);

        if (!refreshToken.isValid()) {
            throw new RefreshTokenInvalidoException();
        }

        Usuario usuario = usuarioRepository.findById(refreshToken.getUsuarioId())
                .orElseThrow(RefreshTokenInvalidoException::new);

        // Revocar token actual y todos los anteriores
        refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());

        // Generar nuevos tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                usuario.getId(), usuario.getEmail(), usuario.getRol().name());

        RefreshToken newRefreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuario.getId())
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return new TokenResponse(accessToken, newRefreshToken.getToken());
    }
}
