# Diagramas de Arquitectura — Sistema de Historia Clinica Obstetrica

> **Ultima actualizacion:** 2026-02-12
> **Formato:** Mermaid (renderizable en GitHub, GitLab, VS Code con extension, etc.)

---

## 1. Diagrama de Contexto del Sistema (C4 — Nivel 1)

```mermaid
graph TB
    subgraph Actores["Actores"]
        OBS(["<b>Obstetra</b><br/>Profesional medico que<br/>atiende pacientes y dicta<br/>notas clinicas por voz"])
        AUD_U(["<b>Auditor</b><br/>Revisa trazabilidad de<br/>operaciones sobre datos clinicos"])
        ADM(["<b>Administrador</b><br/>Gestiona usuarios<br/>y roles del sistema"])
    end

    SIS["<b>Sistema de Historia Clinica<br/>Obstetrica</b><br/><br/>Permite grabar voz durante<br/>atencion clinica y convertirla<br/>en historia clinica estructurada<br/>(CLAP/OPS)"]

    subgraph Externos["Sistemas Externos"]
        WSAPI["<b>Web Speech API</b><br/>Reconocimiento de voz<br/>del navegador (Chrome/Edge)"]
        GSTT["<b>Google Speech-to-Text</b><br/>Fallback para transcripcion<br/>de audio WAV/WebM"]
    end

    OBS -->|"Usa<br/>[HTTPS / navegador]"| SIS
    AUD_U -->|"Consulta registros<br/>de auditoria<br/>[HTTPS]"| SIS
    ADM -->|"Gestiona usuarios<br/>y roles<br/>[HTTPS]"| SIS

    SIS -->|"Transcripcion primaria<br/>[JavaScript API]"| WSAPI
    SIS -->|"Transcripcion fallback<br/>[gRPC / REST]"| GSTT

    classDef actor fill:#08427B,stroke:#052E56,color:#fff
    classDef system fill:#1168BD,stroke:#0B4884,color:#fff
    classDef external fill:#999999,stroke:#6B6B6B,color:#fff

    class OBS,AUD_U,ADM actor
    class SIS system
    class WSAPI,GSTT external
```

---

## 2. Diagrama de Contenedores (C4 — Nivel 2)

```mermaid
graph TB
    subgraph Cliente["Cliente (Navegador)"]
        FE["<b>Frontend React 18</b><br/>TypeScript + Vite + Zustand<br/>Puerto: 5173 (dev)"]
        WSA["Web Speech API<br/>(es-419)"]
    end

    subgraph Infraestructura["Infraestructura de Soporte"]
        EUR["<b>Eureka Server</b><br/>Service Discovery<br/>:8761"]
        GW["<b>API Gateway</b><br/>Spring Cloud Gateway<br/>Circuit Breaker + CORS<br/>:8080"]
        RMQ["<b>RabbitMQ</b><br/>Exchange: clinica.events<br/>(Topic)<br/>:5672"]
        RED["<b>Redis 7</b><br/>Cache / Sesiones<br/>:6379"]
        ZIP["<b>Zipkin</b><br/>Tracing Distribuido<br/>:9411"]
    end

    subgraph Microservicios["Microservicios (Hexagonal)"]
        AUTH["<b>ms-auth</b><br/>Autenticacion JWT + RBAC<br/>:8081"]
        HC["<b>ms-historia-clinica</b><br/>Core del Dominio<br/>Pacientes + Historias CLAP/OPS<br/>:8082"]
        TR["<b>ms-transcripcion</b><br/>Procesamiento de Voz<br/>Normalizacion CIE-10<br/>:8083"]
        AUD["<b>ms-auditoria</b><br/>Trazabilidad Append-Only<br/>:8084"]
        EXP["<b>ms-exportacion</b><br/>Generacion PDF<br/>Thymeleaf + openhtmltopdf<br/>:8085"]
    end

    subgraph Bases_de_Datos["Bases de Datos (PostgreSQL 16)"]
        DB1[("db_auth<br/>:5432")]
        DB2[("db_historia<br/>:5433")]
        DB3[("db_transcripcion<br/>:5434")]
        DB4[("db_auditoria<br/>:5435")]
        DB5[("db_exportacion<br/>:5436")]
    end

    FE -->|"HTTPS / REST"| GW
    WSA -.->|"Texto transcrito"| FE

    GW -->|"/api/auth/**"| AUTH
    GW -->|"/api/pacientes/**<br/>/api/historias-clinicas/**"| HC
    GW -->|"/api/transcripciones/**"| TR
    GW -->|"/api/auditoria/**"| AUD
    GW -->|"/api/exportaciones/**"| EXP

    AUTH --- EUR
    HC --- EUR
    TR --- EUR
    AUD --- EUR
    EXP --- EUR

    AUTH -->|Eventos| RMQ
    HC -->|Eventos| RMQ
    TR -->|Eventos| RMQ
    EXP -->|Eventos| RMQ
    RMQ -->|Consume TODOS| AUD

    EXP -->|"HTTP @LoadBalanced<br/>(via Eureka)"| HC

    AUTH --> DB1
    HC --> DB2
    TR --> DB3
    AUD --> DB4
    EXP --> DB5

    AUTH -.->|Spans| ZIP
    HC -.->|Spans| ZIP
    TR -.->|Spans| ZIP
    AUD -.->|Spans| ZIP
    EXP -.->|Spans| ZIP

    classDef frontend fill:#4FC3F7,stroke:#0277BD,color:#000
    classDef gateway fill:#FFB74D,stroke:#E65100,color:#000
    classDef service fill:#81C784,stroke:#2E7D32,color:#000
    classDef infra fill:#CE93D8,stroke:#6A1B9A,color:#000
    classDef db fill:#FFF176,stroke:#F57F17,color:#000

    class FE,WSA frontend
    class GW gateway
    class AUTH,HC,TR,AUD,EXP service
    class EUR,RMQ,RED,ZIP infra
    class DB1,DB2,DB3,DB4,DB5 db
```

---

## 3. Flujo de Mensajeria (RabbitMQ)

```mermaid
graph LR
    subgraph Productores["Productores de Eventos"]
        P1["ms-auth"]
        P2["ms-historia-clinica"]
        P3["ms-transcripcion"]
        P4["ms-exportacion"]
    end

    EX{{"<b>clinica.events</b><br/>(Topic Exchange)"}}

    subgraph Routing_Keys["Routing Keys"]
        RK1["auth.usuario.registrado"]
        RK2["auth.usuario.login"]
        RK3["historia.paciente.creado"]
        RK4["historia.paciente.actualizado"]
        RK5["historia.clinica.creada"]
        RK6["historia.clinica.actualizada"]
        RK7["historia.clinica.finalizada"]
        RK8["historia.clinica.anulada"]
        RK9["transcripcion.completada"]
        RK10["exportacion.pdf.generado"]
    end

    Q[["<b>clinica.auditoria.queue</b><br/>(Durable)"]]

    subgraph Bindings["Bindings (Wildcards)"]
        B1["auth.#"]
        B2["historia.#"]
        B3["transcripcion.#"]
        B4["exportacion.#"]
    end

    CONS["<b>ms-auditoria</b><br/>@RabbitListener<br/>→ RegistroAuditoria<br/>(append-only)"]

    P1 --> EX
    P2 --> EX
    P3 --> EX
    P4 --> EX

    EX --> RK1 & RK2 & RK3 & RK4 & RK5 & RK6 & RK7 & RK8 & RK9 & RK10

    RK1 & RK2 -.-> B1
    RK3 & RK4 & RK5 & RK6 & RK7 & RK8 -.-> B2
    RK9 -.-> B3
    RK10 -.-> B4

    B1 & B2 & B3 & B4 --> Q --> CONS

    classDef producer fill:#81C784,stroke:#2E7D32,color:#000
    classDef exchange fill:#FFB74D,stroke:#E65100,color:#000
    classDef queue fill:#4FC3F7,stroke:#0277BD,color:#000
    classDef consumer fill:#EF9A9A,stroke:#C62828,color:#000

    class P1,P2,P3,P4 producer
    class EX exchange
    class Q queue
    class CONS consumer
```

---

## 4. Flujo de Autenticacion y Seguridad

### 4a. Login JWT

```mermaid
sequenceDiagram
    participant O as Obstetra
    participant FE as Frontend
    participant GW as Gateway
    participant AUTH as Auth

    O->>FE: Ingresa email y password
    FE->>GW: POST /api/auth/login
    GW->>AUTH: Proxy request
    AUTH->>AUTH: Valida credenciales
    AUTH->>AUTH: Genera JWT 15min y Refresh 7d
    AUTH-->>GW: accessToken y refreshToken
    GW-->>FE: 200 OK
    FE->>FE: Almacena tokens en localStorage
```

### 4b. Request autenticado

```mermaid
sequenceDiagram
    participant O as Obstetra
    participant FE as Frontend
    participant GW as Gateway
    participant MS as Microservicio

    O->>FE: Crear paciente
    FE->>FE: Axios interceptor agrega Bearer JWT
    FE->>GW: POST /api/pacientes
    GW->>MS: Proxy con header JWT
    MS->>MS: JwtAuthenticationFilter valida firma HMAC
    MS->>MS: Extrae claims userId y email y rol
    MS->>MS: Verifica ownership del recurso
    MS-->>GW: 201 Created
    GW-->>FE: Response
```

### 4c. Token expirado y auto refresh

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant GW as Gateway
    participant AUTH as Auth
    participant MS as Microservicio

    FE->>GW: GET /api/historias-clinicas
    GW->>MS: Proxy
    MS-->>GW: 401 Unauthorized
    GW-->>FE: 401
    FE->>FE: Axios interceptor detecta 401
    FE->>GW: POST /api/auth/refresh con refreshToken
    GW->>AUTH: Proxy
    AUTH->>AUTH: Valida refresh token
    AUTH->>AUTH: Genera nuevo par de tokens
    AUTH-->>FE: Nuevo accessToken y refreshToken
    FE->>FE: Actualiza localStorage
    FE->>GW: Reintenta request original con nuevo JWT
    GW->>MS: Proxy
    MS-->>FE: 200 OK con datos
```

---

## 5. Arquitectura Hexagonal (por Microservicio)

```mermaid
graph TB
    subgraph Exterior["Actores Externos"]
        REST_CLIENT["Cliente REST<br/>(API Gateway)"]
        RABBIT_IN["RabbitMQ<br/>(Eventos entrantes)"]
        RABBIT_OUT["RabbitMQ<br/>(Eventos salientes)"]
        DATABASE[("PostgreSQL")]
        HTTP_EXT["Servicio Externo<br/>(ej: ms-historia desde ms-exportacion)"]
    end

    subgraph Hexagono["Microservicio (Arquitectura Hexagonal)"]

        subgraph Adapters_In["Adaptadores de Entrada (Driving)"]
            CTRL["<b>REST Controller</b><br/>@RestController<br/>DTOs Request/Response<br/>Validaciones Jakarta"]
            LISTENER["<b>Event Listener</b><br/>@RabbitListener<br/>Consume AuditableEvent"]
        end

        subgraph Ports_In["Puertos de Entrada"]
            UC1["CrearXxxUseCase"]
            UC2["ObtenerXxxUseCase"]
            UC3["ListarXxxUseCase"]
            UC4["ActualizarXxxUseCase"]
        end

        subgraph Application["Capa de Aplicacion"]
            SVC["<b>XxxService</b><br/>@Service @Transactional<br/>Implementa use cases<br/>Orquesta puertos de salida"]
        end

        subgraph Domain["Capa de Dominio"]
            MODEL["<b>Modelos</b><br/>@Getter @Builder<br/>Entidades inmutables<br/>Reglas de negocio"]
            EXC["<b>Excepciones</b><br/>DomainException<br/>ResourceNotFoundException"]
        end

        subgraph Ports_Out["Puertos de Salida"]
            REPO_PORT["XxxRepositoryPort"]
            EVENT_PORT["EventPublisherPort"]
            EXT_PORT["ExternalServicePort"]
        end

        subgraph Adapters_Out["Adaptadores de Salida (Driven)"]
            PERSIST["<b>Persistence Adapter</b><br/>JPA Entity + Repository<br/>toEntity() / toDomain()<br/>(manual, sin MapStruct)"]
            PUB["<b>Event Publisher</b><br/>RabbitTemplate<br/>AuditableEvent"]
            HTTP_CLIENT["<b>HTTP Client</b><br/>RestTemplate<br/>@LoadBalanced (Eureka)"]
        end
    end

    REST_CLIENT -->|"HTTP Request"| CTRL
    RABBIT_IN -->|"Mensaje"| LISTENER

    CTRL --> UC1 & UC2 & UC3 & UC4
    LISTENER --> UC1

    UC1 & UC2 & UC3 & UC4 --> SVC
    SVC --> MODEL
    SVC --> EXC
    SVC --> REPO_PORT & EVENT_PORT & EXT_PORT

    REPO_PORT --> PERSIST
    EVENT_PORT --> PUB
    EXT_PORT --> HTTP_CLIENT

    PERSIST --> DATABASE
    PUB --> RABBIT_OUT
    HTTP_CLIENT --> HTTP_EXT

    classDef domain fill:#FFECB3,stroke:#FF8F00,color:#000
    classDef app fill:#C8E6C9,stroke:#2E7D32,color:#000
    classDef port fill:#BBDEFB,stroke:#1565C0,color:#000
    classDef adapter fill:#F8BBD0,stroke:#AD1457,color:#000
    classDef external fill:#E0E0E0,stroke:#616161,color:#000

    class MODEL,EXC domain
    class SVC app
    class UC1,UC2,UC3,UC4,REPO_PORT,EVENT_PORT,EXT_PORT port
    class CTRL,LISTENER,PERSIST,PUB,HTTP_CLIENT adapter
    class REST_CLIENT,RABBIT_IN,RABBIT_OUT,DATABASE,HTTP_EXT external
```

---

## 6. Diagrama de Despliegue (Docker Compose)

```mermaid
graph TB
    subgraph Docker_Network["Red Docker: clinica-network"]

        subgraph Infra["Infraestructura"]
            PG1["postgres-auth<br/>:5432<br/>db_auth"]
            PG2["postgres-historia<br/>:5433<br/>db_historia"]
            PG3["postgres-transcripcion<br/>:5434<br/>db_transcripcion"]
            PG4["postgres-auditoria<br/>:5435<br/>db_auditoria"]
            PG5["postgres-exportacion<br/>:5436<br/>db_exportacion"]
            RMQ["rabbitmq<br/>:5672 (AMQP)<br/>:15672 (Management)"]
            REDIS["redis<br/>:6379"]
            ZIPKIN["zipkin<br/>:9411"]
        end

        subgraph Platform["Plataforma"]
            EUREKA["eureka-server<br/>:8761"]
            GATEWAY["api-gateway<br/>:8080<br/>Circuit Breakers x5"]
        end

        subgraph Services["Microservicios"]
            S1["ms-auth<br/>:8081"]
            S2["ms-historia-clinica<br/>:8082"]
            S3["ms-transcripcion<br/>:8083"]
            S4["ms-auditoria<br/>:8084"]
            S5["ms-exportacion<br/>:8085"]
        end
    end

    BROWSER["Navegador<br/>React :5173"] -->|":8080"| GATEWAY

    GATEWAY --> EUREKA
    S1 & S2 & S3 & S4 & S5 --> EUREKA

    S1 --> PG1
    S2 --> PG2
    S3 --> PG3
    S4 --> PG4
    S5 --> PG5

    S1 & S2 & S3 & S5 -->|"Publish"| RMQ
    RMQ -->|"Consume"| S4

    S1 & S2 & S3 & S4 & S5 -.->|"Spans"| ZIPKIN

    S5 -->|"HTTP via Eureka"| S2

    classDef infra fill:#E1BEE7,stroke:#6A1B9A,color:#000
    classDef platform fill:#FFE0B2,stroke:#E65100,color:#000
    classDef svc fill:#C8E6C9,stroke:#2E7D32,color:#000
    classDef client fill:#B3E5FC,stroke:#0277BD,color:#000

    class PG1,PG2,PG3,PG4,PG5,RMQ,REDIS,ZIPKIN infra
    class EUREKA,GATEWAY platform
    class S1,S2,S3,S4,S5 svc
    class BROWSER client
```

---

### 7a. Flujo: Paciente, Historia y Voz

```mermaid
sequenceDiagram
    participant O as Obstetra
    participant FE as Frontend
    participant WSA as WebSpeechAPI
    participant GW as Gateway
    participant HC as Historia
    participant TR as Transcripcion
    participant RMQ as RabbitMQ
    participant AUD as Auditoria

    O->>FE: Datos del paciente
    FE->>GW: POST /api/pacientes
    GW->>HC: Proxy
    HC->>HC: Persiste Paciente
    HC->>RMQ: historia.paciente.creado
    RMQ->>AUD: Registra auditoria
    HC-->>FE: Paciente creado

    O->>FE: Nueva historia para paciente
    FE->>GW: POST /api/historias-clinicas
    GW->>HC: Proxy
    HC->>HC: Crea Historia en BORRADOR
    HC->>RMQ: historia.clinica.creada
    RMQ->>AUD: Registra auditoria
    HC-->>FE: Historia creada

    O->>FE: Presiona Grabar nota clinica
    FE->>WSA: SpeechRecognition start
    WSA-->>FE: Texto transcrito en tiempo real
    FE->>FE: Inserta texto en seccion activa

    FE->>GW: POST /api/transcripciones/texto
    GW->>TR: Proxy
    TR->>TR: Normaliza terminologia CIE-10
    TR->>RMQ: transcripcion.completada
    RMQ->>AUD: Registra auditoria
    TR-->>FE: Transcripcion normalizada

    O->>FE: Revisa y corrige secciones
    FE->>GW: PUT /api/historias-clinicas/id
    GW->>HC: Proxy
    HC->>HC: Actualiza secciones y eventos
    HC->>RMQ: historia.clinica.actualizada
    RMQ->>AUD: Registra auditoria
    HC-->>FE: Historia actualizada
```

### 7b. Flujo: Finalizar y Exportar PDF

```mermaid
sequenceDiagram
    participant O as Obstetra
    participant FE as Frontend
    participant GW as Gateway
    participant HC as Historia
    participant EXP as Exportacion
    participant RMQ as RabbitMQ
    participant AUD as Auditoria

    O->>FE: Finalizar historia
    FE->>GW: PATCH /api/historias-clinicas/id/finalizar
    GW->>HC: Proxy
    HC->>HC: Estado pasa a FINALIZADA
    HC->>RMQ: historia.clinica.finalizada
    RMQ->>AUD: Registra auditoria
    HC-->>FE: OK

    O->>FE: Exportar PDF
    FE->>GW: POST /api/exportaciones
    GW->>EXP: Proxy
    EXP->>HC: GET historia completa via Eureka
    HC-->>EXP: DatosHistoriaClinica
    EXP->>EXP: Genera PDF con Thymeleaf
    EXP->>RMQ: exportacion.pdf.generado
    RMQ->>AUD: Registra auditoria
    EXP-->>FE: ExportJob creado

    O->>FE: Descargar PDF
    FE->>GW: GET /api/exportaciones/id/descargar
    GW->>EXP: Proxy
    EXP-->>FE: PDF como blob
```

---

## 8. Modelo de Dominio (ms-historia-clinica)

```mermaid
classDiagram
    class Paciente {
        -UUID id
        -UUID obstetaId
        -String nombre
        -String apellido
        -String dni
        -LocalDate fechaNacimiento
        -String telefono
        -String direccion
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }

    class HistoriaClinica {
        -UUID id
        -UUID pacienteId
        -UUID obstretaId
        -Integer numeroVersion
        -EstadoHistoria estado
        -List~SeccionClinica~ secciones
        -List~EventoObstetrico~ eventos
        -List~Medicamento~ medicamentos
        -Long jpaVersion
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +crearNuevaVersion() HistoriaClinica
        +finalizar()
        +pasarARevision()
        +anular()
    }

    class SeccionClinica {
        -UUID id
        -UUID historiaClinicaId
        -TipoSeccion tipo
        -String contenido
        -OrigenContenido origen
        -Integer orden
        -LocalDateTime createdAt
    }

    class EventoObstetrico {
        -UUID id
        -UUID historiaClinicaId
        -String tipo
        -LocalDate fecha
        -Integer semanaGestacional
        -String observaciones
        -LocalDateTime createdAt
    }

    class Medicamento {
        -UUID id
        -UUID historiaClinicaId
        -String nombre
        -String dosis
        -String via
        -String frecuencia
        -String duracion
        -LocalDateTime createdAt
    }

    class EstadoHistoria {
        <<enumeration>>
        BORRADOR
        EN_REVISION
        FINALIZADA
        ANULADA
    }

    class TipoSeccion {
        <<enumeration>>
        DATOS_INGRESO
        ANTECEDENTES
        TRABAJO_PARTO
        PARTO
        RECIEN_NACIDO
        PUERPERIO
        EVOLUCION
    }

    class OrigenContenido {
        <<enumeration>>
        VOZ
        MANUAL
    }

    Paciente "1" -- "*" HistoriaClinica : tiene
    HistoriaClinica "1" *-- "*" SeccionClinica : contiene
    HistoriaClinica "1" *-- "*" EventoObstetrico : registra
    HistoriaClinica "1" *-- "*" Medicamento : prescribe
    HistoriaClinica --> EstadoHistoria : estado
    SeccionClinica --> TipoSeccion : tipo
    SeccionClinica --> OrigenContenido : origen
```

---

## 9. Maquina de Estados — Historia Clinica

```mermaid
stateDiagram-v2
    [*] --> BORRADOR : Crear historia

    BORRADOR --> BORRADOR : Actualizar secciones<br/>eventos, medicamentos
    BORRADOR --> EN_REVISION : pasarARevision()

    EN_REVISION --> FINALIZADA : finalizar()
    EN_REVISION --> BORRADOR : Rechazar revision

    FINALIZADA --> [*] : Inmutable
    FINALIZADA --> BORRADOR : crearNuevaVersion()<br/>(nueva historia, version+1)

    BORRADOR --> ANULADA : anular()
    EN_REVISION --> ANULADA : anular()

    ANULADA --> [*] : Terminal

    note right of FINALIZADA
        Una vez finalizada, la historia
        es inmutable. Solo se puede
        crear una nueva version.
    end note

    note right of ANULADA
        Estado terminal.
        No se puede revertir.
    end note
```

---

## Resumen de Componentes

| # | Diagrama | Descripcion |
|---|----------|-------------|
| 1 | Contexto del Sistema | Vista de alto nivel: actores y sistemas externos |
| 2 | Contenedores | Microservicios, bases de datos, mensajeria, gateway |
| 3 | Mensajeria RabbitMQ | Exchange topic, routing keys, bindings y cola de auditoria |
| 4a | Login JWT | Flujo de autenticacion con credenciales |
| 4b | Request autenticado | Validacion JWT en cada microservicio |
| 4c | Auto refresh | Renovacion automatica de token expirado |
| 5 | Arquitectura Hexagonal | Capas, puertos y adaptadores por microservicio |
| 6 | Despliegue Docker | Contenedores, puertos y red interna |
| 7a | Paciente, Historia y Voz | Crear paciente, historia, dictado y transcripcion |
| 7b | Finalizar y Exportar PDF | Finalizar historia, generar y descargar PDF |
| 8 | Modelo de Dominio | Clases del core: Paciente, Historia, Secciones, Eventos |
| 9 | Maquina de Estados | Ciclo de vida de HistoriaClinica |
