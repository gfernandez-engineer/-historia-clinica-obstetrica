# Guia de Despliegue y Pruebas — Sistema de Historia Clinica Obstetrica

> **Ultima actualizacion:** 2026-02-13

---

## Parte 1 — Levantar servicios con Docker

### Prerequisitos

- Docker Desktop instalado y corriendo
- Docker Compose v2+
- Puertos libres: 5432-5436, 5672, 6379, 8080-8085, 8761, 9411, 15672
- Minimo 8 GB RAM asignados a Docker

### Orden de arranque

Docker Compose maneja las dependencias automaticamente con `depends_on` + `healthcheck`, pero el orden logico de arranque es:

```
Nivel 1 — Infraestructura (sin dependencias)
├── postgres-auth         (puerto 5432)
├── postgres-historia     (puerto 5433)
├── postgres-transcripcion(puerto 5434)
├── postgres-auditoria    (puerto 5435)
├── postgres-exportacion  (puerto 5436)
├── redis                 (puerto 6379)
├── rabbitmq              (puertos 5672, 15672)
└── zipkin                (puerto 9411)

Nivel 2 — Service Discovery (espera healthcheck de infraestructura)
└── eureka-server         (puerto 8761)

Nivel 3 — Gateway (espera eureka healthy)
└── api-gateway           (puerto 8080)

Nivel 4 — Microservicios (esperan postgres + rabbitmq + eureka healthy)
├── ms-auth               (puerto 8081)
├── ms-historia-clinica   (puerto 8082)
├── ms-transcripcion      (puerto 8083)
├── ms-auditoria          (puerto 8084)
└── ms-exportacion        (puerto 8085)
```

### Comandos Docker Compose

#### Levantar todo el sistema

```bash
docker compose up -d
```

Docker respetara el orden de dependencias automaticamente.

#### Levantar por niveles (manual, si se prefiere control)

```bash
# Nivel 1 — Infraestructura
docker compose up -d postgres-auth postgres-historia postgres-transcripcion postgres-auditoria postgres-exportacion redis rabbitmq zipkin

# Esperar a que todo este healthy (~30s)
docker compose ps

# Nivel 2 — Eureka
docker compose up -d eureka-server

# Esperar a que eureka este healthy (~30s)
docker compose ps

# Nivel 3 — Gateway
docker compose up -d api-gateway

# Nivel 4 — Microservicios (pueden subir en paralelo)
docker compose up -d ms-auth ms-historia-clinica ms-transcripcion ms-auditoria ms-exportacion
```

#### Verificar que todo esta corriendo

```bash
docker compose ps
```

Todos los servicios deben mostrar estado `Up (healthy)`.

#### Ver logs de un servicio especifico

```bash
docker compose logs -f ms-auth
docker compose logs -f ms-historia-clinica
```

#### Detener todo

```bash
docker compose down
```

#### Detener y eliminar volumenes (reset completo de BDs)

```bash
docker compose down -v
```

### Verificacion de salud

| Servicio | URL de healthcheck |
|----------|-------------------|
| Eureka | http://localhost:8761 |
| API Gateway | http://localhost:8080/actuator/health |
| ms-auth | http://localhost:8081/actuator/health |
| ms-historia-clinica | http://localhost:8082/actuator/health |
| ms-transcripcion | http://localhost:8083/actuator/health |
| ms-auditoria | http://localhost:8084/actuator/health |
| ms-exportacion | http://localhost:8085/actuator/health |
| RabbitMQ Management | http://localhost:15672 (clinica / clinica_dev) |
| Zipkin | http://localhost:9411 |

### Swagger UI por microservicio

| Servicio | URL Swagger |
|----------|-------------|
| ms-auth | http://localhost:8081/swagger-ui.html |
| ms-historia-clinica | http://localhost:8082/swagger-ui.html |
| ms-transcripcion | http://localhost:8083/swagger-ui.html |
| ms-auditoria | http://localhost:8084/swagger-ui.html |
| ms-exportacion | http://localhost:8085/swagger-ui.html |

---

## Parte 2 — Pruebas con Bruno (cURLs)

### Configuracion en Bruno

1. Crear una nueva coleccion: **Historia Clinica Obstetrica**
2. Configurar variable de entorno `baseUrl` = `http://localhost:8080`
3. Despues de login, guardar el `accessToken` como variable de entorno

> **Nota:** Todas las peticiones van al API Gateway (puerto 8080) que rutea a cada microservicio.
> Los cURLs tambien pueden ejecutarse directamente en terminal.

---

### Secuencia de pruebas recomendada

El orden importa porque cada paso genera datos necesarios para el siguiente:

```
1. Registrar usuario (obstetra)
2. Login (obtener token)
3. Crear paciente
4. Listar/buscar pacientes
5. Crear historia clinica
6. Actualizar historia (secciones, eventos, medicamentos)
7. Cambiar estado: revision
8. Cambiar estado: finalizar
9. Procesar transcripcion de texto
10. Generar exportacion PDF
11. Descargar PDF
12. Consultar auditoria (requiere usuario AUDITOR)
```

---

### 1. AUTENTICACION (ms-auth)

#### 1.1 Registrar obstetra

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria.garcia@clinica.com",
    "password": "Password123!",
    "nombre": "Maria",
    "apellido": "Garcia",
    "rol": "OBSTETRA"
  }'
```

**Respuesta esperada (201):**
```json
{
  "id": "uuid-generado",
  "email": "maria.garcia@clinica.com",
  "nombre": "Maria",
  "apellido": "Garcia",
  "rol": "OBSTETRA",
  "createdAt": "2026-02-13T..."
}
```

#### 1.2 Registrar auditor (para pruebas de auditoria)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "auditor@clinica.com",
    "password": "Password123!",
    "nombre": "Carlos",
    "apellido": "Ruiz",
    "rol": "AUDITOR"
  }'
```

#### 1.3 Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria.garcia@clinica.com",
    "password": "Password123!"
  }'
```

**Respuesta esperada (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

> **Guardar el `accessToken`** — se usa en todas las peticiones siguientes.
> En Bruno: crear variable `token` con el valor recibido.

#### 1.4 Refresh token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "valor-del-refresh-token"
  }'
```

---

### 2. PACIENTES (ms-historia-clinica)

> Todas las peticiones requieren header: `Authorization: Bearer {{token}}`

#### 2.1 Crear paciente

```bash
curl -X POST http://localhost:8080/api/pacientes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {{token}}" \
  -d '{
    "dni": "12345678",
    "nombre": "Ana",
    "apellido": "Lopez Mendoza",
    "fechaNacimiento": "1990-05-15",
    "telefono": "987654321",
    "direccion": "Av. Lima 123, Lima"
  }'
```

**Respuesta esperada (201):**
```json
{
  "id": "uuid-del-paciente",
  "dni": "12345678",
  "nombre": "Ana",
  "apellido": "Lopez Mendoza",
  "fechaNacimiento": "1990-05-15",
  "telefono": "987654321",
  "direccion": "Av. Lima 123, Lima",
  "createdAt": "2026-02-13T...",
  "updatedAt": "2026-02-13T..."
}
```

> **Guardar el `id` del paciente** — se necesita para crear historias clinicas.

#### 2.2 Crear segundo paciente

```bash
curl -X POST http://localhost:8080/api/pacientes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {{token}}" \
  -d '{
    "dni": "87654321",
    "nombre": "Carmen",
    "apellido": "Torres Silva",
    "fechaNacimiento": "1988-11-20",
    "telefono": "912345678",
    "direccion": "Jr. Cusco 456, Arequipa"
  }'
```

#### 2.3 Listar pacientes (con paginacion)

```bash
curl -X GET "http://localhost:8080/api/pacientes?page=0&size=10" \
  -H "Authorization: Bearer {{token}}"
```

#### 2.4 Buscar paciente por nombre o DNI

```bash
curl -X GET "http://localhost:8080/api/pacientes?buscar=Ana&page=0&size=10" \
  -H "Authorization: Bearer {{token}}"
```

```bash
curl -X GET "http://localhost:8080/api/pacientes?buscar=12345678&page=0&size=10" \
  -H "Authorization: Bearer {{token}}"
```

#### 2.5 Obtener paciente por ID

```bash
curl -X GET http://localhost:8080/api/pacientes/{{pacienteId}} \
  -H "Authorization: Bearer {{token}}"
```

#### 2.6 Actualizar paciente

```bash
curl -X PUT http://localhost:8080/api/pacientes/{{pacienteId}} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {{token}}" \
  -d '{
    "nombre": "Ana Maria",
    "apellido": "Lopez Mendoza",
    "fechaNacimiento": "1990-05-15",
    "telefono": "999888777",
    "direccion": "Av. Lima 456, Lima"
  }'
```

---

### 3. HISTORIAS CLINICAS (ms-historia-clinica)

#### 3.1 Crear historia clinica

```bash
curl -X POST http://localhost:8080/api/historias-clinicas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {{token}}" \
  -d '{
    "pacienteId": "{{pacienteId}}",
    "notasGenerales": "Paciente acude a control prenatal. Embarazo de 28 semanas."
  }'
```

**Respuesta esperada (201):**
```json
{
  "id": "uuid-de-historia",
  "pacienteId": "uuid-del-paciente",
  "version": 1,
  "estado": "BORRADOR",
  "notasGenerales": "Paciente acude a control prenatal...",
  "secciones": [],
  "eventos": [],
  "medicamentos": [],
  "createdAt": "2026-02-13T...",
  "updatedAt": "2026-02-13T..."
}
```

> **Guardar el `id` de la historia** — se usa en los siguientes pasos.

#### 3.2 Actualizar historia (agregar secciones, eventos y medicamentos)

```bash
curl -X PUT http://localhost:8080/api/historias-clinicas/{{historiaId}} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {{token}}" \
  -d '{
    "notasGenerales": "Control prenatal semana 28. Embarazo normoevolutivo.",
    "secciones": [
      {
        "tipo": "DATOS_INGRESO",
        "contenido": "Paciente de 34 anios, G2P1, acude a control prenatal de las 28 semanas. Peso: 72kg, Talla: 1.60m, PA: 110/70mmHg.",
        "origen": "VOZ",
        "orden": 1
      },
      {
        "tipo": "ANTECEDENTES",
        "contenido": "Parto vaginal previo hace 3 anios sin complicaciones. Sin antecedentes patologicos relevantes. Grupo sanguineo O+.",
        "origen": "VOZ",
        "orden": 2
      },
      {
        "tipo": "EVOLUCION",
        "contenido": "Altura uterina 28cm, FCF 140lpm, movimientos fetales activos. Presentacion cefalica.",
        "origen": "MANUAL",
        "orden": 3
      }
    ],
    "eventos": [
      {
        "tipo": "Control prenatal",
        "fecha": "2026-02-13T10:00:00Z",
        "semanaGestacional": 28,
        "observaciones": "Control normal. Ecografia acorde a edad gestacional."
      },
      {
        "tipo": "Ecografia obstetrica",
        "fecha": "2026-02-13T10:30:00Z",
        "semanaGestacional": 28,
        "observaciones": "Feto unico vivo, presentacion cefalica, liquido amniotico normal, placenta fundo corporal posterior grado II."
      }
    ],
    "medicamentos": [
      {
        "nombre": "Acido folico",
        "dosis": "5mg",
        "via": "Oral",
        "frecuencia": "Cada 24 horas",
        "duracion": "Durante todo el embarazo"
      },
      {
        "nombre": "Sulfato ferroso",
        "dosis": "300mg",
        "via": "Oral",
        "frecuencia": "Cada 24 horas",
        "duracion": "Durante todo el embarazo"
      },
      {
        "nombre": "Calcio",
        "dosis": "500mg",
        "via": "Oral",
        "frecuencia": "Cada 12 horas",
        "duracion": "Desde semana 20"
      }
    ]
  }'
```

#### 3.3 Obtener historia clinica completa

```bash
curl -X GET http://localhost:8080/api/historias-clinicas/{{historiaId}} \
  -H "Authorization: Bearer {{token}}"
```

#### 3.4 Listar historias clinicas

```bash
curl -X GET "http://localhost:8080/api/historias-clinicas?page=0&size=10" \
  -H "Authorization: Bearer {{token}}"
```

#### 3.5 Listar historias de un paciente especifico

```bash
curl -X GET "http://localhost:8080/api/historias-clinicas?pacienteId={{pacienteId}}&page=0&size=10" \
  -H "Authorization: Bearer {{token}}"
```

#### 3.6 Pasar a revision

```bash
curl -X PATCH http://localhost:8080/api/historias-clinicas/{{historiaId}}/revision \
  -H "Authorization: Bearer {{token}}"
```

**Respuesta:** estado cambia a `EN_REVISION`.

#### 3.7 Finalizar historia (se vuelve inmutable)

```bash
curl -X PATCH http://localhost:8080/api/historias-clinicas/{{historiaId}}/finalizar \
  -H "Authorization: Bearer {{token}}"
```

**Respuesta:** estado cambia a `FINALIZADA`. Ya no se puede editar.

#### 3.8 Anular historia (crear otra historia para probar)

```bash
# Primero crear otra historia
curl -X POST http://localhost:8080/api/historias-clinicas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {{token}}" \
  -d '{
    "pacienteId": "{{pacienteId}}",
    "notasGenerales": "Historia para anular - prueba"
  }'

# Luego anularla (usar el id de la nueva historia)
curl -X PATCH http://localhost:8080/api/historias-clinicas/{{nuevaHistoriaId}}/anular \
  -H "Authorization: Bearer {{token}}"
```

**Respuesta:** estado cambia a `ANULADA`.

---

### 4. TRANSCRIPCIONES (ms-transcripcion)

#### 4.1 Procesar texto (simulando Web Speech API)

> Usar el ID de una historia en estado BORRADOR.

```bash
curl -X POST http://localhost:8080/api/transcripciones/texto \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {{token}}" \
  -d '{
    "historiaClinicaId": "{{historiaId}}",
    "texto": "Paciente con embarazo de 28 semanas acude a control prenatal. Presion arterial normal, frecuencia cardiaca fetal de 140 latidos por minuto. Se indica cesarea programada por presentacion podalica. Diagnostico preeclampsia leve."
  }'
```

**Respuesta esperada (201):**
```json
{
  "id": "uuid-transcripcion",
  "historiaClinicaId": "uuid-historia",
  "obstetraId": "uuid-obstetra",
  "textoOriginal": "Paciente con embarazo de 28 semanas...",
  "textoNormalizado": "texto con terminos medicos normalizados y codigos CIE-10",
  "estado": "PROCESADO",
  "origen": "WEB_SPEECH",
  "errorDetalle": null,
  "createdAt": "2026-02-13T...",
  "updatedAt": "2026-02-13T..."
}
```

#### 4.2 Obtener transcripcion por ID

```bash
curl -X GET http://localhost:8080/api/transcripciones/{{transcripcionId}} \
  -H "Authorization: Bearer {{token}}"
```

#### 4.3 Listar transcripciones de una historia

```bash
curl -X GET "http://localhost:8080/api/transcripciones/historia/{{historiaId}}?page=0&size=10" \
  -H "Authorization: Bearer {{token}}"
```

#### 4.4 Procesar audio (multipart — fallback)

```bash
curl -X POST http://localhost:8080/api/transcripciones/audio \
  -H "Authorization: Bearer {{token}}" \
  -F "historiaClinicaId={{historiaId}}" \
  -F "archivo=@/ruta/al/audio.wav"
```

> **Nota:** El adaptador de speech-to-text es un stub. Devolvera un texto placeholder.

---

### 5. EXPORTACION PDF (ms-exportacion)

> La historia debe estar FINALIZADA para exportar.

#### 5.1 Generar exportacion PDF

```bash
curl -X POST http://localhost:8080/api/exportaciones \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {{token}}" \
  -d '{
    "historiaClinicaId": "{{historiaId}}",
    "formato": "PDF"
  }'
```

**Respuesta esperada (200):**
```json
{
  "id": "uuid-exportacion",
  "historiaClinicaId": "uuid-historia",
  "formato": "PDF",
  "estado": "COMPLETADO",
  "archivoUrl": "ruta-interna",
  "errorMensaje": null,
  "createdAt": "2026-02-13T...",
  "completedAt": "2026-02-13T..."
}
```

> **Guardar el `id` de la exportacion** para descargar el PDF.

#### 5.2 Obtener estado de exportacion

```bash
curl -X GET http://localhost:8080/api/exportaciones/{{exportacionId}} \
  -H "Authorization: Bearer {{token}}"
```

#### 5.3 Listar exportaciones del obstetra

```bash
curl -X GET "http://localhost:8080/api/exportaciones?page=0&size=10" \
  -H "Authorization: Bearer {{token}}"
```

#### 5.4 Descargar PDF

```bash
curl -X GET http://localhost:8080/api/exportaciones/{{exportacionId}}/descargar \
  -H "Authorization: Bearer {{token}}" \
  --output historia-clinica.pdf
```

> El archivo `historia-clinica.pdf` se guardara en el directorio actual.

---

### 6. AUDITORIA (ms-auditoria)

> Requiere rol **AUDITOR** o **ADMIN**. Usar el token del usuario auditor.

#### 6.1 Login como auditor

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "auditor@clinica.com",
    "password": "Password123!"
  }'
```

> Guardar este token como `tokenAuditor`.

#### 6.2 Consultar todos los registros de auditoria

```bash
curl -X GET "http://localhost:8080/api/auditoria?page=0&size=20" \
  -H "Authorization: Bearer {{tokenAuditor}}"
```

#### 6.3 Filtrar por tipo de recurso

```bash
curl -X GET "http://localhost:8080/api/auditoria?resourceType=PACIENTE&page=0&size=20" \
  -H "Authorization: Bearer {{tokenAuditor}}"
```

#### 6.4 Filtrar por accion

```bash
curl -X GET "http://localhost:8080/api/auditoria?action=CREATED&page=0&size=20" \
  -H "Authorization: Bearer {{tokenAuditor}}"
```

#### 6.5 Filtrar por usuario especifico

```bash
curl -X GET "http://localhost:8080/api/auditoria?userId={{userIdObstetra}}&page=0&size=20" \
  -H "Authorization: Bearer {{tokenAuditor}}"
```

#### 6.6 Filtrar por rango de fechas

```bash
curl -X GET "http://localhost:8080/api/auditoria?desde=2026-02-13T00:00:00Z&hasta=2026-02-13T23:59:59Z&page=0&size=20" \
  -H "Authorization: Bearer {{tokenAuditor}}"
```

#### 6.7 Filtros combinados

```bash
curl -X GET "http://localhost:8080/api/auditoria?resourceType=PACIENTE&action=CREATED&desde=2026-02-13T00:00:00Z&page=0&size=20" \
  -H "Authorization: Bearer {{tokenAuditor}}"
```

---

## Parte 3 — Configuracion en Bruno (paso a paso)

### Crear el entorno

1. Abrir Bruno > **New Collection** > nombre: `Historia Clinica Obstetrica`
2. Click en **Environments** > **Configure** > **New Environment** > nombre: `Local`
3. Agregar variables:

| Variable | Valor inicial |
|----------|--------------|
| `baseUrl` | `http://localhost:8080` |
| `token` | _(vacio, se llena despues del login)_ |
| `tokenAuditor` | _(vacio, se llena despues del login auditor)_ |
| `pacienteId` | _(vacio, se llena despues de crear paciente)_ |
| `historiaId` | _(vacio, se llena despues de crear historia)_ |
| `transcripcionId` | _(vacio)_ |
| `exportacionId` | _(vacio)_ |

### Estructura de carpetas sugerida

```
Historia Clinica Obstetrica/
├── 1-Auth/
│   ├── 1.1 Registrar Obstetra
│   ├── 1.2 Registrar Auditor
│   ├── 1.3 Login Obstetra
│   └── 1.4 Refresh Token
├── 2-Pacientes/
│   ├── 2.1 Crear Paciente
│   ├── 2.2 Listar Pacientes
│   ├── 2.3 Buscar Paciente
│   ├── 2.4 Obtener Paciente
│   └── 2.5 Actualizar Paciente
├── 3-Historias Clinicas/
│   ├── 3.1 Crear Historia
│   ├── 3.2 Actualizar Historia (secciones)
│   ├── 3.3 Obtener Historia
│   ├── 3.4 Listar Historias
│   ├── 3.5 Pasar a Revision
│   ├── 3.6 Finalizar
│   └── 3.7 Anular
├── 4-Transcripciones/
│   ├── 4.1 Procesar Texto
│   ├── 4.2 Obtener Transcripcion
│   └── 4.3 Listar por Historia
├── 5-Exportaciones/
│   ├── 5.1 Generar PDF
│   ├── 5.2 Obtener Estado
│   ├── 5.3 Listar Exportaciones
│   └── 5.4 Descargar PDF
└── 6-Auditoria/
    ├── 6.1 Login Auditor
    ├── 6.2 Todos los Registros
    ├── 6.3 Filtrar por Recurso
    ├── 6.4 Filtrar por Accion
    └── 6.5 Filtrar por Fecha
```

### Tips para Bruno

- **Auth heredada:** En la carpeta raiz, configurar Auth > Bearer Token > `{{token}}`. Asi todas las requests heredan el token automaticamente.
- **Scripts post-response:** En el login, agregar un script que guarde el token automaticamente:
  ```javascript
  bru.setEnvVar("token", res.body.accessToken);
  ```
- **Ejecucion secuencial:** Usar el Runner de Bruno para ejecutar toda la carpeta en orden.

---

## Resumen de endpoints

| # | Metodo | Ruta | Servicio | Auth |
|---|--------|------|----------|------|
| 1 | POST | /api/auth/register | ms-auth | No |
| 2 | POST | /api/auth/login | ms-auth | No |
| 3 | POST | /api/auth/refresh | ms-auth | No |
| 4 | POST | /api/pacientes | ms-historia | Si |
| 5 | GET | /api/pacientes | ms-historia | Si |
| 6 | GET | /api/pacientes/{id} | ms-historia | Si |
| 7 | PUT | /api/pacientes/{id} | ms-historia | Si |
| 8 | POST | /api/historias-clinicas | ms-historia | Si |
| 9 | GET | /api/historias-clinicas | ms-historia | Si |
| 10 | GET | /api/historias-clinicas/{id} | ms-historia | Si |
| 11 | PUT | /api/historias-clinicas/{id} | ms-historia | Si |
| 12 | PATCH | /api/historias-clinicas/{id}/revision | ms-historia | Si |
| 13 | PATCH | /api/historias-clinicas/{id}/finalizar | ms-historia | Si |
| 14 | PATCH | /api/historias-clinicas/{id}/anular | ms-historia | Si |
| 15 | POST | /api/transcripciones/texto | ms-transcripcion | Si |
| 16 | POST | /api/transcripciones/audio | ms-transcripcion | Si |
| 17 | GET | /api/transcripciones/{id} | ms-transcripcion | Si |
| 18 | GET | /api/transcripciones/historia/{id} | ms-transcripcion | Si |
| 19 | POST | /api/exportaciones | ms-exportacion | Si |
| 20 | GET | /api/exportaciones | ms-exportacion | Si |
| 21 | GET | /api/exportaciones/{id} | ms-exportacion | Si |
| 22 | GET | /api/exportaciones/{id}/descargar | ms-exportacion | Si |
| 23 | GET | /api/auditoria | ms-auditoria | Si (AUDITOR/ADMIN) |
