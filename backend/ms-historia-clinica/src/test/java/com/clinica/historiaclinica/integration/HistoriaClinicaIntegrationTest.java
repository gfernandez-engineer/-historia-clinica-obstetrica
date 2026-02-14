package com.clinica.historiaclinica.integration;

import com.clinica.shared.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class HistoriaClinicaIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("db_historia_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "0");
        registry.add("spring.autoconfigure.exclude", () ->
                "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration");
        registry.add("eureka.client.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String accessToken;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        accessToken = jwtTokenProvider.generateAccessToken(userId, "obstetra@test.com", "OBSTETRA");
    }

    @Test
    void shouldCreatePacienteAndHistoriaClinica() throws Exception {
        // Crear paciente
        Map<String, Object> pacienteRequest = Map.of(
                "dni", "12345678",
                "nombre", "Ana",
                "apellido", "Lopez",
                "fechaNacimiento", "1990-05-15",
                "telefono", "987654321",
                "direccion", "Av. Lima 123"
        );

        MvcResult pacienteResult = mockMvc.perform(post("/api/pacientes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pacienteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.apellido").value("Lopez"))
                .andReturn();

        String pacienteId = objectMapper.readTree(
                pacienteResult.getResponse().getContentAsString()).get("id").asText();

        // Crear historia clinica
        Map<String, Object> historiaRequest = Map.of(
                "pacienteId", pacienteId,
                "notasGenerales", "Primera consulta obstetrica"
        );

        MvcResult historiaResult = mockMvc.perform(post("/api/historias-clinicas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(historiaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.notasGenerales").value("Primera consulta obstetrica"))
                .andReturn();

        String historiaId = objectMapper.readTree(
                historiaResult.getResponse().getContentAsString()).get("id").asText();

        // Obtener historia creada
        mockMvc.perform(get("/api/historias-clinicas/" + historiaId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(historiaId))
                .andExpect(jsonPath("$.estado").value("BORRADOR"));
    }

    @Test
    void shouldListPacientesByObstetra() throws Exception {
        // Crear 2 pacientes
        for (int i = 1; i <= 2; i++) {
            Map<String, Object> request = Map.of(
                    "dni", "DNI-LIST-" + i,
                    "nombre", "Paciente" + i,
                    "apellido", "Test",
                    "fechaNacimiento", "1990-01-0" + i
            );
            mockMvc.perform(post("/api/pacientes")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Listar
        mockMvc.perform(get("/api/pacientes")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized());
    }
}
