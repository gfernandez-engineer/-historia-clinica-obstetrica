# Diagramas de Arquitectura — Sistema de Historia Clinica Obstetrica

> **Ultima actualizacion:** 2026-02-13
> **Version para exportacion PDF** (diagramas pre-renderizados como imagenes)

---

## 1. Diagrama de Contexto del Sistema (C4 — Nivel 1)

![Diagrama de Contexto del Sistema](diagrams/01-contexto-sistema.png)

---

## 2. Diagrama de Contenedores (C4 — Nivel 2)

![Diagrama de Contenedores](diagrams/02-contenedores.png)

---

## 3. Flujo de Mensajeria (RabbitMQ)

![Flujo de Mensajeria RabbitMQ](diagrams/03-mensajeria-rabbitmq.png)

---

## 4. Flujo de Autenticacion y Seguridad

### 4a. Login JWT

![Login JWT](diagrams/04a-login-jwt.png)

### 4b. Request autenticado

![Request autenticado](diagrams/04b-request-autenticado.png)

### 4c. Token expirado y auto refresh

![Token refresh](diagrams/04c-token-refresh.png)

---

## 5. Arquitectura Hexagonal (por Microservicio)

![Arquitectura Hexagonal](diagrams/05-arquitectura-hexagonal.png)

---

## 6. Diagrama de Despliegue (Docker Compose)

![Despliegue Docker](diagrams/06-despliegue-docker.png)

---

### 7a. Flujo: Paciente, Historia y Voz

![Flujo Paciente Historia y Voz](diagrams/07a-flujo-paciente-voz.png)

### 7b. Flujo: Finalizar y Exportar PDF

![Flujo Finalizar y Exportar PDF](diagrams/07b-flujo-finalizar-pdf.png)

---

## 8. Modelo de Dominio (ms-historia-clinica)

![Modelo de Dominio](diagrams/08-modelo-dominio.png)

---

## 9. Maquina de Estados — Historia Clinica

![Maquina de Estados](diagrams/09-maquina-estados.png)

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
