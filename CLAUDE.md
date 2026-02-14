# CLAUDE.md — Instrucciones del Proyecto

> **Ultima actualizacion:** 2026-02-10
> **Plan detallado con checkboxes:** `docs/02-plan-de-trabajo.md`

## Contexto
Sistema de historia clinica obstetrica por voz. Backend Java 21 + Spring Boot 3.3.5, microservicios hexagonales, PostgreSQL 16, RabbitMQ, React 18 + TS.

## Al inicio de cada sesion
1. Leer `docs/02-plan-de-trabajo.md` para ver que fases estan completas y cual sigue
2. No repetir trabajo ya completado

---

## Decisiones de arquitectura tomadas

### Persistencia
- Adaptadores manuales con `toEntity()` / `toDomain()`. NO MapStruct.
- Child entities de HistoriaClinica (secciones, eventos, medicamentos) como tablas independientes sin `@OneToMany` en JPA
- `HistoriaClinicaPersistenceAdapter` gestiona hijos explicitamente (delete + saveAll por lote)
- `@Modifying` + `@Query` en repos de hijos para bulk delete

### Dominio
- HistoriaClinica es inmutable una vez finalizada — nueva edicion via `crearNuevaVersion()`
- Bloqueo optimista con `@Version` (campo `jpaVersion`)
- Estados: BORRADOR → EN_REVISION → FINALIZADA | ANULADA
- Ownership: si no es owner → lanzar `NotFoundException` (no revelar existencia)

### Eventos
- `AuditableEvent` al exchange `clinica.events` (RabbitMQ topic)
- Routing keys: `{servicio}.{recurso}.{accion}` (ej: `historia.paciente.creado`)

### Auditoria
- ms-auditoria consume TODOS los eventos via RabbitMQ (bindings: `auth.#`, `historia.#`, `transcripcion.#`, `exportacion.#`)
- Tabla append-only: REVOKE UPDATE, DELETE en migracion SQL
- Endpoint restringido a roles AUDITOR y ADMIN via `hasAnyRole("AUDITOR", "ADMIN")`
- Consulta con filtros opcionales (userId, resourceId, resourceType, action, desde, hasta)
- Todos los `@SpringBootApplication` deben incluir `scanBasePackages = {"com.clinica.{servicio}", "com.clinica.shared"}`

### Seguridad
- JWT stateless, `AuthenticatedUser(userId, email, rol)` via `@AuthenticationPrincipal`
- Cada microservicio tiene su `SecurityConfig` (Swagger y actuator sin auth)

### API
- DTOs como Java records + validaciones Jakarta + `@Schema` OpenAPI
- Paginacion via `Pageable` en listados
- Busqueda case-insensitive con JPQL LIKE

---

## Arquitectura hexagonal (obligatoria en cada microservicio)
```
domain/model/           → entidades (Lombok @Getter @Builder)
domain/port/in/         → use cases (interface + Command record)
domain/port/out/        → repositorios, publishers (interfaces)
domain/exception/       → extienden DomainException o ResourceNotFoundException
application/service/    → @Service @Transactional, implementan use cases
infrastructure/adapter/in/rest/     → controllers + DTOs
infrastructure/adapter/out/persistence/ → JPA entities, repos, adapters
infrastructure/adapter/out/messaging/   → publishers RabbitMQ
infrastructure/config/  → SecurityConfig, OpenApiConfig
```

## Convenciones de codigo
- Paquete base: `com.clinica.{nombre-servicio}`
- Use cases: infinitivo (`CrearPacienteUseCase`) con `Command` record interno
- Servicios: implementan multiples use cases, inyectan puertos
- Excepciones: `DomainException` (negocio), `ResourceNotFoundException` (404)
- Migraciones: Flyway `V{n}__descripcion.sql`
- Eventos: `AuditableEvent.create(...)` del shared-kernel
- Tests: JUnit 5 + Mockito, `@ExtendWith(MockitoExtension.class)`

## Puertos
| Servicio | Puerto | BD | Estado |
|----------|--------|----|--------|
| eureka-server | 8761 | - | Completo |
| api-gateway | 8080 | - | Completo |
| ms-auth | 8081 | db_auth (5432) | Completo |
| ms-historia-clinica | 8082 | db_historia (5433) | Completo |
| ms-transcripcion | 8083 | db_transcripcion (5434) | Completo |
| ms-auditoria | 8084 | db_auditoria (5435) | Completo |
| ms-exportacion | 8085 | db_exportacion (5436) | Completo |

## Compilacion
```bash
# Instalar parent + shared-kernel (necesario antes de compilar/testear modulos)
mvn install -N -q && mvn install -pl shared-kernel -DskipTests -q

# Compilar modulo
mvn compile -pl shared-kernel,ms-{nombre}

# Tests
mvn test -pl ms-{nombre}
```

## Lo que NO hacer
- No crear docs/README salvo que se pida
- No agregar features o refactors no solicitados
- No usar MapStruct en persistence adapters
- No exponer ownership en errores
- No commitear sin que se pida
