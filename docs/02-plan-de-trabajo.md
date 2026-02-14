# Plan de Trabajo — Sistema de Historia Clinica Obstetrica

> **Ultima actualizacion:** 2026-02-10
> **Referencia:** [01-alcance-del-proyecto.md](./01-alcance-del-proyecto.md)

---

## Fases de implementacion

### Fase 1 — Scaffolding e infraestructura base
**Estado:** COMPLETADA

- [x] Parent POM multi-modulo (Java 21, Spring Boot 3.3.5, Spring Cloud 2024.0.0)
- [x] `shared-kernel`: JWT, excepciones globales, RabbitMQ config, OpenAPI config
- [x] `eureka-server`: Service discovery (puerto 8761)
- [x] `api-gateway`: Rutas a todos los microservicios (puerto 8080)
- [x] `docker-compose.yml`: PostgreSQL x5, Redis, RabbitMQ

---

### Fase 2 — ms-auth (Autenticacion)
**Estado:** COMPLETADA

- [x] Arquitectura hexagonal completa
- [x] Registro, login, refresh token con JWT
- [x] RBAC: roles OBSTETRA, AUDITOR, ADMIN
- [x] Publicacion de eventos a RabbitMQ
- [x] Migraciones Flyway (usuarios, refresh_tokens)
- [x] Tests unitarios (AuthServiceTest, RefreshTokenTest)
- [x] Swagger/OpenAPI documentado

**Puerto:** 8081 | **BD:** db_auth (5432)

---

### Fase 3 — ms-historia-clinica (Core del dominio)
**Estado:** COMPLETADA

- [x] Modelos de dominio: HistoriaClinica, Paciente, SeccionClinica, EventoObstetrico, Medicamento
- [x] Enums: EstadoHistoria, TipoSeccion, OrigenContenido
- [x] Excepciones de dominio
- [x] 9 use cases (puertos de entrada) para Paciente e HistoriaClinica
- [x] 3 puertos de salida (repositorios + event publisher)
- [x] PacienteService + HistoriaClinicaService
- [x] 5 entidades JPA, 5 repositorios Spring Data, 2 persistence adapters
- [x] PacienteController (4 endpoints) + HistoriaClinicaController (7 endpoints)
- [x] 6 DTOs (request/response) con validaciones y OpenAPI
- [x] Event publisher RabbitMQ + Security config JWT
- [x] 5 migraciones Flyway (pacientes, historias, secciones, eventos, medicamentos)
- [x] 30 tests unitarios (dominio + servicios)

**Puerto:** 8082 | **BD:** db_historia (5433)

**Endpoints:**
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | /api/pacientes | Crear paciente |
| GET | /api/pacientes | Listar/buscar pacientes |
| GET | /api/pacientes/{id} | Obtener paciente |
| PUT | /api/pacientes/{id} | Actualizar paciente |
| POST | /api/historias-clinicas | Crear historia clinica |
| GET | /api/historias-clinicas | Listar historias |
| GET | /api/historias-clinicas/{id} | Obtener historia completa |
| PUT | /api/historias-clinicas/{id} | Actualizar secciones/eventos/medicamentos |
| PATCH | /api/historias-clinicas/{id}/finalizar | Finalizar (inmutable) |
| PATCH | /api/historias-clinicas/{id}/revision | Pasar a revision |
| PATCH | /api/historias-clinicas/{id}/anular | Anular historia |

---

### Fase 4 — ms-auditoria (Trazabilidad)
**Estado:** COMPLETADA

- [x] Modelo de dominio: RegistroAuditoria (append-only)
- [x] Listener RabbitMQ para consumir AuditableEvent (queue con bindings auth.#, historia.#, transcripcion.#, exportacion.#)
- [x] Persistencia inmutable (solo INSERT, REVOKE UPDATE/DELETE en migracion)
- [x] API de consulta con filtros (userId, resourceId, resourceType, action, desde, hasta)
- [x] Migraciones Flyway (registros_auditoria con 6 indices)
- [x] Tests unitarios (4 tests: registro, consulta con filtros, sin filtros, mapeo completo)
- [x] Seguridad: endpoint restringido a roles AUDITOR y ADMIN
- [x] Fix: scanBasePackages en AuditoriaApplication y HistoriaClinicaApplication
- [x] Fix: credenciales application.yml corregidas (clinica/clinica_dev)

**Puerto:** 8084 | **BD:** db_auditoria (5435)

**Endpoints:**
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | /api/auditoria | Consultar registros con filtros (solo AUDITOR/ADMIN) |

---

### Fase 5 — ms-transcripcion (Procesamiento de voz)
**Estado:** COMPLETADA

- [x] Modelos de dominio: Transcripcion, TerminoMedico, EstadoTranscripcion, OrigenTranscripcion
- [x] 3 use cases: ProcesarTextoUseCase, ProcesarAudioUseCase, ObtenerTranscripcionUseCase
- [x] 4 puertos de salida: repositorio, event publisher, SpeechToTextPort, NormalizadorMedicoPort
- [x] TranscripcionService (procesar texto Web Speech API + audio fallback)
- [x] NormalizadorMedicoAdapter (diccionario ~30 terminos obstetricos con CIE-10)
- [x] SpeechToTextStubAdapter (placeholder para Google Speech-to-Text)
- [x] JPA entities + repositorio + persistence adapter
- [x] Event publisher RabbitMQ (transcripcion.completada)
- [x] TranscripcionController (4 endpoints: texto, audio multipart, obtener, listar)
- [x] DTOs (ProcesarTextoRequest, TranscripcionResponse)
- [x] SecurityConfig JWT + OpenApiConfig
- [x] Migracion Flyway (transcripciones + terminos_medicos)
- [x] 8 tests unitarios (TranscripcionServiceTest)

**Puerto:** 8083 | **BD:** db_transcripcion (5434)

**Endpoints:**
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | /api/transcripciones/texto | Procesar texto de Web Speech API |
| POST | /api/transcripciones/audio | Procesar archivo audio (multipart) |
| GET | /api/transcripciones/{id} | Obtener transcripcion |
| GET | /api/transcripciones/historia/{id} | Listar transcripciones por historia |

---

### Fase 6 — ms-exportacion (Generacion de PDF)
**Estado:** COMPLETADA

- [x] Modelos de dominio: ExportJob, DatosHistoriaClinica, EstadoExportacion, FormatoExportacion
- [x] 4 use cases: GenerarExportacionUseCase, ObtenerExportacionUseCase, ListarExportacionesUseCase, DescargarPdfUseCase
- [x] 4 puertos de salida: ExportJobRepositoryPort, PdfGeneratorPort, HistoriaClinicaClientPort, ExportacionEventPublisherPort
- [x] ExportacionService (generar PDF, descargar, listar, obtener con ownership)
- [x] Generacion PDF con Thymeleaf + openhtmltopdf (HTML→PDF)
- [x] Plantilla historia clinica obstetrica (secciones, eventos, medicamentos, firma, marca de agua)
- [x] HTTP client a ms-historia-clinica via RestTemplate + @LoadBalanced (Eureka)
- [x] JPA entity + repositorio + persistence adapter (manual toEntity/toDomain)
- [x] Event publisher RabbitMQ (exportacion.pdf.generado)
- [x] ExportacionController (4 endpoints: generar, obtener, listar, descargar PDF)
- [x] DTOs (GenerarExportacionRequest, ExportJobResponse) con validaciones y OpenAPI
- [x] SecurityConfig JWT + OpenApiConfig
- [x] Migracion Flyway (export_jobs con 4 indices)
- [x] 8 tests unitarios (ExportacionServiceTest)
- [x] Fix: scanBasePackages, credenciales application.yml

**Puerto:** 8085 | **BD:** db_exportacion (5436)

**Endpoints:**
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | /api/exportaciones | Generar exportacion PDF |
| GET | /api/exportaciones | Listar exportaciones del obstetra |
| GET | /api/exportaciones/{id} | Obtener estado de exportacion |
| GET | /api/exportaciones/{id}/descargar | Descargar PDF |

---

### Fase 7 — Frontend React
**Estado:** COMPLETADA

- [x] Scaffolding: React 18 + TypeScript + Vite + Zustand
- [x] API client (Axios) con interceptors JWT (auto-refresh, bearer token)
- [x] Auth store (Zustand) con login, logout, loadFromStorage (parseo JWT)
- [x] Modulo de autenticacion: LoginPage, RegisterPage, ProtectedRoute
- [x] Layout con navbar, selector de idioma, logout
- [x] Dashboard con cards de navegacion
- [x] Modulo pacientes: listado con busqueda, formulario crear/editar
- [x] Modulo historias clinicas: listado, detalle, formulario con secciones CLAP/OPS
- [x] Acciones de estado: revision, finalizar, anular
- [x] Modulo de grabacion de voz: hook useSpeechRecognition (Web Speech API es-419), componente VoiceRecorder
- [x] Transcripcion en tiempo real insertada en seccion activa
- [x] Modulo exportacion PDF: listado, generacion, descarga como blob
- [x] i18n (es/en) con react-i18next + archivos de traduccion completos
- [x] react-helmet-async: titulos dinamicos por pagina
- [x] API clients: auth, pacientes, historias, transcripciones, exportaciones
- [x] Types TypeScript completos (Paciente, HistoriaClinica, ExportJob, Page, etc.)
- [x] 11 tests con Vitest + Testing Library (authStore 5, useSpeechRecognition 4, ProtectedRoute 2)
- [x] Build de produccion verificado (316 kB gzip 103 kB)

**Puerto dev:** 5173 (proxy a API Gateway 8080)

**Paginas:**
| Ruta | Componente | Descripcion |
|------|-----------|-------------|
| /login | LoginPage | Inicio de sesion |
| /register | RegisterPage | Registro de usuario |
| / | DashboardPage | Dashboard principal |
| /pacientes | PacientesPage | Listado con busqueda |
| /pacientes/nuevo | PacienteFormPage | Crear paciente |
| /pacientes/:id | PacienteFormPage | Editar paciente |
| /historias | HistoriasPage | Listado de historias |
| /historias/nueva | HistoriaFormPage | Nueva historia con voz |
| /historias/:id | HistoriaDetallePage | Detalle + acciones |
| /historias/:id/editar | HistoriaFormPage | Editar historia |
| /exportaciones | ExportacionesPage | Listado + descarga PDF |

---

### Fase 8 — Integracion y observabilidad
**Estado:** COMPLETADA

- [x] Dependencias de observabilidad en parent POM (micrometer-tracing, zipkin-reporter, resilience4j, logstash-logback)
- [x] Actuator + tracing distribuido (Micrometer Brave + Zipkin) en los 5 microservicios
- [x] Circuit breakers Resilience4j en API Gateway (5 rutas con fallback controller)
- [x] Circuit breaker en ms-exportacion HTTP client (@CircuitBreaker con fallback)
- [x] Logs centralizados: logback-spring.xml con console (dev) + JSON/Logstash (prod) en 6 servicios
- [x] Docker Compose completo: 5 PostgreSQL + RabbitMQ + Redis + Zipkin + Eureka + Gateway + 5 microservicios
- [x] Healthchecks en infraestructura (postgres, rabbitmq, redis) con depends_on condition
- [x] Tests de integracion con Testcontainers: AuthIntegrationTest (3 tests) + HistoriaClinicaIntegrationTest (3 tests)
- [x] Todos los tests unitarios existentes siguen pasando (30 + 13 + 8 + 4 + 8 = 63 tests)
- [x] Compilacion verificada de los 7 modulos

**Observabilidad:**
- Endpoints actuator: `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`
- Tracing: Micrometer Brave → Zipkin (http://localhost:9411)
- Logs: Pattern con traceId/spanId en desarrollo, JSON structured en produccion (profile `prod`)

**Circuit Breakers (Gateway):**
| Ruta | Circuit Breaker | Fallback |
|------|----------------|----------|
| /api/auth/** | authCircuitBreaker | /fallback/auth |
| /api/pacientes/**, /api/historias-clinicas/** | historiaCircuitBreaker | /fallback/historia |
| /api/transcripciones/** | transcripcionCircuitBreaker | /fallback/transcripcion |
| /api/auditoria/** | auditoriaCircuitBreaker | /fallback/auditoria |
| /api/exportaciones/** | exportacionCircuitBreaker | /fallback/exportacion |

---

## Convenciones del proyecto

- **Arquitectura:** Hexagonal (Ports & Adapters) por microservicio
- **Patron de eventos:** AuditableEvent via RabbitMQ (exchange: `clinica.events`, topic)
- **Seguridad:** JWT stateless, AuthenticatedUser como @AuthenticationPrincipal
- **Persistencia:** JPA con adaptadores manuales (no MapStruct en persistencia), Flyway para migraciones
- **Tests:** JUnit 5 + Mockito, dominio y servicios de aplicacion
- **Ownership:** Cada obstetra solo accede a sus pacientes/historias
- **Versionado:** Historias clinicas inmutables una vez finalizadas, nueva version via crearNuevaVersion()
- **Bloqueo optimista:** @Version en HistoriaClinica para concurrencia
