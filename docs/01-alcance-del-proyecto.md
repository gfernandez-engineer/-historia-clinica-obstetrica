# Alcance del Proyecto — Sistema de Historia Clinica Obstetrica por Voz

> **Documento de referencia**: Este archivo define el alcance, tecnologias y restricciones del sistema. Toda decision de diseno debe ser consistente con lo aqui descrito.

---

## Objetivo

Disenar e implementar un sistema web para obstetras que permita grabar voz durante la atencion clinica y convertirla automaticamente en una historia clinica obstetrica estructurada. El sistema debe priorizar: **reproducibilidad, seguridad, trazabilidad legal y usabilidad clinica**.

---

## 1. Stack tecnologico

| Capa | Tecnologia | Justificacion |
|------|-----------|---------------|
| Backend | Java 21 + Spring Boot 3.x | Ecosistema maduro, soporte LTS |
| Arquitectura | Hexagonal (Ports & Adapters) por microservicio | Desacoplamiento, testabilidad |
| Frontend | React 18 + TypeScript + Vite | Tipado fuerte, rendimiento |
| Estado global | Zustand | Ligero, sin boilerplate, compatible con React 18 |
| UI Components | Storybook 8 | Catalogo de componentes aislados, documentacion visual |
| Metadata | react-helmet-async | Titulos dinamicos por pagina |
| i18n | react-i18next + i18next | Soporte multi-idioma (es/en) |
| Testing frontend | Vitest + Testing Library | Tests unitarios y de componentes |
| Documentacion API | springdoc-openapi (OpenAPI 3.0) | Swagger UI auto-generado por microservicio |
| Base de datos | PostgreSQL 16 (datos clinicos) + Redis (cache/sesiones) | ACID para datos medicos, rendimiento para sesiones |
| Mensajeria | RabbitMQ o Kafka | Comunicacion asincrona entre microservicios |
| Voz a texto | Web Speech API (primario) -> Google Speech-to-Text free tier (fallback) | Costo cero inicial, degradacion controlada |
| Autenticacion | Spring Security + JWT (access + refresh tokens) | Stateless, escalable |
| Contenedores | Docker + Docker Compose (dev) / Kubernetes (prod) | Reproducibilidad de entornos |

---

## 2. Microservicios (bounded contexts)

### 2.1 `ms-auth` — Autenticacion y autorizacion
- Registro, login, refresh de tokens JWT.
- RBAC con roles: `OBSTETRA`, `AUDITOR`, `ADMIN`.
- Permisos granulares por recurso (ej: un obstetra solo accede a sus pacientes).

### 2.2 `ms-transcripcion` — Procesamiento de voz
- Recibe texto transcrito desde el frontend (Web Speech API).
- Fallback: recibe audio WAV/WebM y lo envia a Google Speech-to-Text.
- **Normalizacion de terminologia medica** mediante diccionario obstetrico configurable (ej: "cesarea" -> codigo CIE-10 O82).
- Publica evento `TranscripcionCompletada` al bus de mensajes.

### 2.3 `ms-historia-clinica` — Gestion clinica (core del dominio)
- Consume evento `TranscripcionCompletada` y mapea texto a secciones clinicas.
- Secciones del CLAP/OPS: datos de ingreso, antecedentes, trabajo de parto, parto, recien nacido, puerperio, medicamentos, evolucion.
- CRUD completo de historias clinicas con versionado (cada edicion genera nueva version, nunca se sobrescribe).
- Validaciones de dominio (ej: edad gestacional coherente, fechas logicas).

### 2.4 `ms-auditoria` — Trazabilidad
- Escucha todos los eventos del sistema via bus de mensajes.
- Registra: `quien`, `que`, `cuando`, `desde donde` (IP), `valor anterior`, `valor nuevo`.
- Almacenamiento append-only (inmutable).
- API de consulta para auditores con filtros por fecha, usuario, paciente y tipo de accion.

### 2.5 `ms-exportacion` — Generacion de documentos
- Genera PDF de historia clinica usando plantillas (Thymeleaf + OpenPDF o JasperReports).
- Incluye firma digital del obstetra y marca de agua con fecha/hora.
- Endpoint para impresion directa.

---

## 3. Arquitectura hexagonal (por microservicio)

```
src/main/java/com/clinica/{servicio}/
├── domain/
│   ├── model/          # Entidades: Paciente, HistoriaClinica, EventoObstetrico
│   ├── port/
│   │   ├── in/         # Puertos de entrada (casos de uso)
│   │   └── out/        # Puertos de salida (repositorios, servicios externos)
│   └── exception/      # Excepciones de dominio
├── application/
│   └── service/        # Implementacion de casos de uso
├── infrastructure/
│   ├── adapter/
│   │   ├── in/
│   │   │   └── rest/   # Controllers REST
│   │   └── out/
│   │       ├── persistence/  # JPA repositories, mappers
│   │       └── messaging/    # Publicadores/consumidores de eventos
│   └── config/         # Configuracion Spring
```

**Entidades de dominio clave:**
- `Paciente` (DNI, nombre, edad, antecedentes)
- `HistoriaClinica` (id, paciente, version, secciones[], estado, fechaCreacion)
- `SeccionClinica` (tipo enum, contenido, origen: VOZ|MANUAL)
- `EventoObstetrico` (tipo, fecha, semanaGestacional, observaciones)
- `Medicamento` (nombre, dosis, via, frecuencia, duracion)

---

## 4. Frontend React

### 4.1 Modulo de grabacion
- Boton "Grabar nota clinica" con indicador visual de estado (grabando/pausado/detenido).
- Transcripcion en tiempo real con Web Speech API (`SpeechRecognition`).
- Deteccion de idioma: espanol latinoamericano (`es-419`).
- Editor de texto enriquecido (TipTap o Slate.js) para correccion post-transcripcion.
- Almacenamiento temporal del audio en `Blob` para envio al backend como fallback.

### 4.2 Formulario de historia clinica
- Formulario dinamico basado en las secciones del CLAP/OPS.
- Campos pre-llenados con la transcripcion procesada (el obstetra valida y corrige).
- Validacion en frontend con Zod o Yup.
- Autoguardado cada 30 segundos.

### 4.3 Modo offline
- Service Worker + IndexedDB para almacenar historias pendientes.
- Cola de sincronizacion con resolucion de conflictos (last-write-wins o merge manual).
- Indicador visual de estado de conexion.

### 4.4 Exportacion
- Vista previa del PDF en el navegador.
- Descarga e impresion directa.

### 4.5 Storybook
- Catalogo de componentes UI aislados con addon-essentials y addon-a11y.
- Stories para: VoiceRecorder, RichTextEditor, HistoriaForm, OfflineIndicator, componentes comunes.
- Accesible en `http://localhost:6006` durante desarrollo.

### 4.6 Metadata (react-helmet-async)
- Titulos dinamicos por pagina (`<title>` y `<meta description>`).
- `HelmetProvider` en App.tsx, `<Helmet>` en cada page component.

### 4.7 Internacionalizacion (react-i18next)
- Soporte espanol (default) e ingles.
- Archivos de traduccion en `src/i18n/locales/{es,en}.json`.
- Selector de idioma en Header.
- Todas las cadenas de UI via hook `useTranslation()`.

### 4.8 Testing frontend (Vitest + Testing Library)
- `vitest.config.ts` con jsdom environment.
- Tests unitarios para hooks: `useSpeechRecognition`, `useAuth`, `useAutoSave`, `useOfflineSync`.
- Tests de componentes: formularios, VoiceRecorder, ProtectedRoute.
- Cobertura minima 70% en componentes y hooks.

### 4.9 OpenAPI 3.0 (documentacion de APIs)
- springdoc-openapi en cada microservicio con Swagger UI.
- DTOs anotados con `@Schema(description, example)`.
- Controllers anotados con `@Tag`, `@Operation`, `@ApiResponse`.
- Swagger UI aggregado en API Gateway: `http://localhost:8080/swagger-ui.html` (todas las APIs en un punto).

---

## 5. Seguridad y cumplimiento normativo

- **Autenticacion:** JWT con access token (15 min) + refresh token (7 dias) en httpOnly cookie.
- **Autorizacion:** RBAC + ownership (obstetra solo ve sus pacientes).
- **Transporte:** TLS 1.3 obligatorio.
- **Datos en reposo:** cifrado AES-256 para campos sensibles (diagnosticos, datos personales) mediante `@Convert` de JPA.
- **Auditoria:** log inmutable de toda operacion CRUD sobre datos clinicos.
- **Cumplimiento:** Ley 29733 (Proteccion de Datos Personales - Peru) o normativa local aplicable; estandar HL7 FHIR como referencia para interoperabilidad futura.
- **Retencion:** historias clinicas conservadas minimo 20 anos (Norma Tecnica de Salud).

---

## 6. Infraestructura y observabilidad

- **Docker Compose** para entorno local con todos los servicios + PostgreSQL + Redis + RabbitMQ.
- **Kubernetes** (Helm charts) para produccion.
- **API Gateway** (Spring Cloud Gateway) para routing, rate limiting y autenticacion centralizada.
- **Service Discovery** con Eureka o Kubernetes DNS.
- **Monitoreo:** Prometheus + Grafana (metricas), Loki (logs centralizados), Jaeger (tracing distribuido).
- **CI/CD:** GitHub Actions o Jenkins con stages: lint -> test -> build -> scan (Trivy) -> deploy.

---

## 7. Restricciones y consideraciones

- **Compatibilidad de Web Speech API:** solo Chrome/Edge. Documentar limitacion y ofrecer entrada manual como alternativa en otros navegadores.
- **Latencia de transcripcion:** la normalizacion medica debe completarse en < 2 segundos.
- **Concurrencia:** un obstetra puede tener multiples historias abiertas; manejar bloqueo optimista (`@Version` de JPA).
- **Testing backend:** cobertura minima 80% en dominio y aplicacion; tests de integracion con Testcontainers.
- **Testing frontend:** cobertura minima 70% en hooks y componentes con Vitest + Testing Library.
- **Documentacion API:** OpenAPI 3.0 auto-generada con SpringDoc; Swagger UI aggregado en API Gateway.

---

## 8. Entregables esperados

1. Repositorio con estructura multi-modulo (monorepo o multi-repo).
2. Cada microservicio con arquitectura hexagonal implementada.
3. Frontend React funcional con grabacion de voz.
4. Docker Compose para levantar todo el sistema localmente.
5. Documentacion tecnica en la carpeta `docs/`.
6. Tests unitarios y de integracion.

---

> **Fecha de creacion:** 2026-02-09
> **Ultima actualizacion:** 2026-02-09
> **Estado:** Documento de alcance aprobado — base para planificacion.
