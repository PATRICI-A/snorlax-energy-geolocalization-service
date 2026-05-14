<div align="center">

# Geolocalization Service — (M07)

### *"Sabe dónde estás, conecta con quienes están cerca"*

---

### Stack Tecnológico

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-47A248?style=for-the-badge&logo=mongodb&logoColor=white)

### Infraestructura & Calidad

![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-Coverage-brightgreen?style=for-the-badge)

### Arquitectura

![Hexagonal](https://img.shields.io/badge/Architecture-Hexagonal-blueviolet?style=for-the-badge)
![REST API](https://img.shields.io/badge/REST-API-009688?style=for-the-badge)

</div>

---

## Tabla de Contenidos

1. [Integrantes](#1-integrantes)
2. [Tecnologías Utilizadas](#2-tecnologías-utilizadas)
3. [Descripción del Módulo](#3-descripción-del-módulo)
4. [Cómo Funciona el Módulo](#4-cómo-funciona-el-módulo)
5. [Diagrama de Datos](#5-diagrama-de-datos)
6. [Diagrama de Clases](#6-diagrama-de-clases)
7. [Diagrama de Componentes](#7-diagrama-de-componentes)
8. [Funcionalidades del Módulo](#8-funcionalidades-del-módulo)
9. [Endpoints Expuestos](#9-endpoints-expuestos)
10. [Colas de Mensajería](#10-colas-de-mensajería)
11. [Evidencia de Pruebas Unitarias](#11-evidencia-de-pruebas-unitarias)
12. [Evidencia de Análisis de Cobertura](#12-evidencia-de-análisis-de-cobertura)
13. [Cómo Ejecutar el Proyecto](#13-cómo-ejecutar-el-proyecto)
14. [Evidencia del Despliegue CI/CD](#14-evidencia-del-despliegue-cicd)
15. [Link Swagger Desplegado](#15-link-swagger-desplegado)
16. [Estructura del Código](#16-estructura-del-código)
17. [Código Documentado](#17-código-documentado)
18. [Conexiones con Servicios Externos](#18-conexiones-con-servicios-externos)
19. [Pipeline de Desarrollo](#19-pipeline-de-desarrollo)
20. [Pipeline de Producción](#20-pipeline-de-producción)
21. [Dockerizado](#21-dockerizado)
22. [Estrategia de Versionamiento](#22-estrategia-de-versionamiento)

---

## 1. Integrantes

| Nombre | Rol |
|---|---|
| [NOMBRE INTEGRANTE 1] | Backend Developer |
| [NOMBRE INTEGRANTE 2] | Backend Developer |
| [NOMBRE INTEGRANTE 3] | Backend Developer |
| [NOMBRE INTEGRANTE 4] | Backend Developer |
| [NOMBRE INTEGRANTE 5] | Backend Developer |

---

## 2. Tecnologías Utilizadas

| Tecnología / Herramienta | Versión | Uso en el módulo |
|---|---|---|
| Java (OpenJDK) | 21 LTS | Lenguaje base del módulo |
| Spring Boot | 4.0.6 | Framework principal |
| Spring Web | Incluido | Exposición de endpoints REST |
| Spring Security + JWT (JJWT) | 0.12.6 | Validación local de tokens JWT firmados con HMAC-SHA256 |
| Spring Data MongoDB | Incluido | Acceso a base de datos documental con índice geoespacial 2dsphere |
| Spring AMQP (RabbitMQ) | Incluido | Publicación de eventos de actualización de ubicación |
| Spring Cloud OpenFeign | 2025.1.1 | Comunicación HTTP con otros microservicios |
| MongoDB Atlas | Cloud | Base de datos principal (colección `locations`) |
| RabbitMQ (CloudAMQP) | Cloud | Broker de mensajería para eventos geoespaciales |
| Google Maps Geocoding API | REST | Resolución automática de zona del campus a partir de coordenadas |
| Apache Maven | 3.9+ | Gestión de dependencias y build |
| Lombok | 1.18.36 | Reducción de boilerplate |
| MapStruct | 1.6.3 | Mapeo entre capas (request → DTO → dominio → documento) |
| JUnit 5 | Incluido | Pruebas unitarias |
| Mockito | Incluido | Simulación de dependencias en pruebas |
| JaCoCo | 0.8.12 | Análisis de cobertura de pruebas (mínimo 80%) |
| SpringDoc OpenAPI | 2.8.6 | Generación automática de Swagger UI |
| Docker | 24.x | Contenedorización del servicio |
| GitHub Actions | N/A | Pipeline CI/CD |

---

## 3. Descripción del Módulo

**Identificador:** M07  
**Nombre técnico del repositorio:** `snorlax-energy-geolocalization-service`  
**Puerto local:** 8084  
**Puerto Docker:** 8084

El módulo de Geolocalización es el responsable de rastrear y consultar la ubicación en tiempo real de los estudiantes dentro del campus universitario. Recibe coordenadas GPS del cliente móvil, las persiste en MongoDB con índice geoespacial 2dsphere, y detecta automáticamente la zona del campus (bloque, edificio) usando la API de Google Maps Geocoding. Publica eventos de ubicación a través de RabbitMQ para que los módulos de Matching y Parches puedan sugerir conexiones entre estudiantes cercanos.

**Requisitos funcionales que implementa este módulo:**

| RF | Nombre |
|---|---|
| RF-G01 | Actualizar ubicación del estudiante autenticado |
| RF-G02 | Consultar última ubicación de un usuario |
| RF-G03 | Consultar ubicación propia del usuario autenticado |
| RF-G04 | Obtener usuarios cercanos dentro de un radio configurable |
| RF-G05 | Detección automática de zona del campus por coordenadas GPS |
| RF-G06 | Publicación de eventos de ubicación para módulos consumidores |

---

## 4. Cómo Funciona el Módulo

### Estilo de Arquitectura: Hexagonal (Ports & Adapters)

El módulo implementa Arquitectura Hexagonal. El dominio no conoce frameworks ni bases de datos. Los controladores REST y los repositorios MongoDB son adaptadores que implementan puertos (interfaces). El flujo de dependencias es unidireccional:

```
Entrypoints / Infrastructure → Application → Domain
```

### Patrones de Diseño Utilizados

| Patrón | Clase(s) donde aplica | Por qué se usa |
|---|---|---|
| Ports & Adapters | Todos los puertos `in/` y `out/` | Desacopla el dominio de Spring, MongoDB y dependencias externas |
| Use Case / Interactor | `UpdateLocationUseCase`, `GetLocationUseCase`, `GetNearbyUsersUseCase` | Encapsula la lógica de negocio por caso de uso, independiente del framework |
| Value Object | `Coordinates` | Valida invariantes geográficas y encapsula la fórmula de Haversine para distancia |
| Adapter | `LocationRepositoryAdapter`, `GoogleGeocodingAdapter`, `LocationEventPublisher` | Implementan puertos de salida aislando la infraestructura del dominio |
| DTO (Data Transfer Object) | `UpdateLocationRequestDto`, `LocationResponseDto`, `NearbyUserResponseDto` | Separa la representación de red del modelo de dominio |

### Módulos que Consume / Produce

| Módulo | Dato que consume/produce | Cómo lo obtiene | Impacto si falla |
|---|---|---|---|
| M01 — Autenticación | `userId` extraído del JWT | Validación local con clave HMAC-SHA256 compartida. Sin llamada HTTP a M01. | Rechaza todas las peticiones con 401 |
| M-Matching | Evento `geo.location.updated` | Publica a RabbitMQ exchange `geo.exchange` con routing key `geo.location.updated` | El evento se pierde; Matching no recibe la actualización (fallo silencioso con log de advertencia) |
| M-Parches | Evento `geo.location.updated` | Mismo exchange RabbitMQ | Igual que Matching |
| Google Maps Geocoding API | `campusZone` (texto del lugar) | HTTP REST sobre `maps.googleapis.com` | Zona queda nula o como la enviada por el cliente; no falla la actualización |

### Flujo General de una Petición (PUT /location)

1. El cliente móvil envía un JWT en el header `Authorization: Bearer <token>` junto con las coordenadas GPS
2. `SecurityConfig.JwtAuthFilter` valida el token localmente con HMAC-SHA256 y extrae el `userId`
3. `GeoController` recibe el request validado y delega a `UpdateLocationPort`
4. `UpdateLocationUseCase` llama a `CampusZoneResolverPort` (implementado por `GoogleGeocodingAdapter`) para detectar la zona del campus
5. El caso de uso persiste la ubicación a través de `LocationRepositoryPort` (implementado por `LocationRepositoryAdapter` → MongoDB Atlas)
6. Se publica el evento `geo.location.updated` a RabbitMQ vía `LocationEventPublisher`
7. Se retorna el `LocationResponseDto` con todos los campos mapeados

---

## 5. Diagrama de Datos

<div align="center">
<img src="docs/Diagrama_Entidad.png" alt="Diagrama de Datos" width="700"/>
</div>

### Colección MongoDB: `locations`

| Campo | Tipo BSON | Restricciones | Descripción |
|---|---|---|---|
| `_id` | String (ObjectId) | PK | Identificador único del documento |
| `userId` | String | Único, indexado | Identificador del estudiante (uno por usuario) |
| `coordinates` | GeoJSON Point | Índice 2dsphere | `{ type: "Point", coordinates: [lng, lat] }` — permite consultas geoespaciales |
| `campusZone` | String | Nullable | Nombre del bloque o zona detectada (ej. "Bloque B, ECI") |
| `accuracy` | Double | Nullable | Precisión GPS en metros reportada por el cliente |
| `updatedAt` | Date | Not null | Timestamp de la última actualización |

> El índice `2dsphere` sobre `coordinates` permite consultas `$nearSphere` nativas de MongoDB para encontrar usuarios cercanos por distancia real.

---

## 6. Diagrama de Clases

<div align="center">
<img src="docs/Diagrama_Clases.png" alt="Diagrama de Clases" width="700"/>
</div>

**Descripción del diagrama:** Muestra las cuatro capas hexagonales: Domain (puertos y modelo), Application (use cases y DTOs), Infrastructure (adaptadores MongoDB, RabbitMQ y Google Maps) y Entrypoints (controlador REST y filtro JWT). Las dependencias siempre apuntan hacia el dominio.

### Clases principales del dominio

| Clase | Tipo | Responsabilidad |
|---|---|---|
| `Location` | Model | Entidad de dominio con datos de ubicación del estudiante |
| `Coordinates` | Value Object | Valida rangos lat/lon e implementa fórmula de Haversine para distancia |
| `UpdateLocationPort` | Port In | Caso de uso: actualizar ubicación del usuario autenticado |
| `GetLocationPort` | Port In | Caso de uso: consultar última ubicación de un usuario |
| `GetNearbyUsersPort` | Port In | Caso de uso: obtener usuarios dentro de un radio dado |
| `LocationRepositoryPort` | Port Out | Contrato de persistencia de ubicaciones |
| `CampusZoneResolverPort` | Port Out | Contrato de resolución de zona del campus |
| `LocationNotFoundException` | Exception | Lanzada cuando no hay ubicación registrada para un userId |
| `UserNotFoundException` | Exception | Lanzada cuando el usuario no existe |
| `InvalidRadiusException` | Exception | Lanzada cuando el radio excede 5000 m o es negativo |

---

## 7. Diagrama de Componentes

<div align="center">
<img src="docs/Diagrama_Componentes.png" alt="Diagrama de Componentes" width="700"/>
</div>

**Descripción del diagrama:** Muestra el microservicio con sus tres adaptadores de entrada/salida: el controlador REST (entrada), el adaptador de persistencia MongoDB y el publicador RabbitMQ (salida). Se aprecia la conexión con MongoDB Atlas, CloudAMQP y la API de Google Maps como servicios externos.

| Componente | Tipo | Interfaces que expone |
|---|---|---|
| `GeoController` | REST Controller | `PUT /api/v1/geo/location`, `GET /api/v1/geo/location/{userId}`, `GET /api/v1/geo/location/me`, `GET /api/v1/geo/nearby` |
| `UpdateLocationUseCase` | Application Service | Puerto In: `UpdateLocationPort` |
| `GetLocationUseCase` | Application Service | Puerto In: `GetLocationPort` |
| `GetNearbyUsersUseCase` | Application Service | Puerto In: `GetNearbyUsersPort` |
| `LocationRepositoryAdapter` | Driven Adapter | Puerto Out: `LocationRepositoryPort` → MongoDB Atlas |
| `GoogleGeocodingAdapter` | Driven Adapter | Puerto Out: `CampusZoneResolverPort` → Google Maps REST API |
| `LocationEventPublisher` | Driven Adapter | Publica a RabbitMQ `geo.exchange` con routing key `geo.location.updated` |
| `SecurityConfig.JwtAuthFilter` | Security Filter | Valida JWT HMAC-SHA256 antes de llegar al controlador |

---

## 8. Funcionalidades del Módulo

| ID | Funcionalidad | RF asociado | Descripción |
|---|---|---|---|
| F01 | Actualizar ubicación | RF-G01 | Recibe coordenadas GPS del cliente autenticado, detecta zona del campus con Google Maps, persiste en MongoDB y publica evento a RabbitMQ |
| F02 | Consultar ubicación de usuario | RF-G02 | Devuelve la última ubicación registrada de cualquier userId. Útil para perfiles y matching |
| F03 | Consultar ubicación propia | RF-G03 | Devuelve la ubicación del usuario autenticado usando el `userId` del JWT |
| F04 | Usuarios cercanos | RF-G04 | Consulta geoespacial por radio (máx. 5000 m), ordena por distancia usando Haversine. Usado por Matching y Parches |
| F05 | Detección de zona campus | RF-G05 | `GoogleGeocodingAdapter` invierte coordenadas a nombre de lugar vía Google Maps API. Prioriza establecimientos y POIs sobre direcciones de calle |
| F06 | Publicación de evento | RF-G06 | `LocationEventPublisher` publica `LocationUpdatedEventDto` al exchange `geo.exchange` de RabbitMQ en cada actualización exitosa |

---

## 9. Endpoints Expuestos

### F01 — Actualizar Ubicación

**Endpoint:** `PUT /api/v1/geo/location`

#### Request

| Campo | Tipo | Origen | Obligatorio | Descripción |
|---|---|---|---|---|
| `Authorization` | String | Header | Sí | `Bearer <JWT>` — userId extraído del token |
| `latitude` | Double | Body | Sí | Latitud GPS. Rango: `-90.0` a `90.0` |
| `longitude` | Double | Body | Sí | Longitud GPS. Rango: `-180.0` a `180.0` |
| `accuracy` | Double | Body | No | Precisión del GPS en metros |
| `campusZone` | String | Body | No | Zona sugerida por el cliente (se reemplaza con la detectada automáticamente) |

#### Response exitoso — `200 OK`

| Campo | Tipo | Descripción |
|---|---|---|
| `userId` | String | ID del estudiante |
| `latitude` | Double | Latitud persistida |
| `longitude` | Double | Longitud persistida |
| `campusZone` | String | Zona del campus detectada por Google Maps |
| `accuracy` | Double | Precisión GPS en metros |
| `updatedAt` | String (ISO 8601) | Timestamp de la actualización |

#### Ejemplo

```
PUT /api/v1/geo/location
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

```json
// Request body
{
  "latitude": 4.6035,
  "longitude": -74.0655,
  "accuracy": 12.5
}
```

```json
// Response 200 OK
{
  "userId": "user-123",
  "latitude": 4.6035,
  "longitude": -74.0655,
  "campusZone": "Escuela Colombiana de Ingeniería Julio Garavito",
  "accuracy": 12.5,
  "updatedAt": "2026-05-13T21:00:00"
}
```

#### Errores manejados

| Código HTTP | Escenario | Mensaje |
|---|---|---|
| 400 | Coordenadas fuera de rango o nulas | `"Latitude must be >= -90"` |
| 401 | JWT inválido o ausente | `"TOKEN_INVALID"` |

---

### F02 — Consultar Ubicación de un Usuario

**Endpoint:** `GET /api/v1/geo/location/{userId}`

#### Request

| Campo | Tipo | Origen | Obligatorio | Descripción |
|---|---|---|---|---|
| `Authorization` | String | Header | Sí | `Bearer <JWT>` |
| `userId` | String | Path | Sí | ID del usuario a consultar |

#### Response exitoso — `200 OK`

| Campo | Tipo | Descripción |
|---|---|---|
| `userId` | String | ID del estudiante |
| `latitude` | Double | Última latitud registrada |
| `longitude` | Double | Última longitud registrada |
| `campusZone` | String | Zona del campus |
| `accuracy` | Double | Precisión GPS |
| `updatedAt` | String (ISO 8601) | Timestamp de la última actualización |

#### Errores manejados

| Código HTTP | Escenario | Mensaje |
|---|---|---|
| 401 | JWT inválido o ausente | `"TOKEN_INVALID"` |
| 404 | No hay ubicación registrada para ese userId | `"LOCATION_NOT_FOUND"` |

---

### F03 — Consultar Ubicación Propia

**Endpoint:** `GET /api/v1/geo/location/me`

Misma respuesta que F02. El `userId` se extrae del JWT — no requiere parámetro.

#### Errores manejados

| Código HTTP | Escenario | Mensaje |
|---|---|---|
| 401 | JWT inválido o ausente | `"TOKEN_INVALID"` |
| 404 | El usuario autenticado no ha registrado ubicación | `"LOCATION_NOT_FOUND"` |

---

### F04 — Usuarios Cercanos

**Endpoint:** `GET /api/v1/geo/nearby`

#### Request

| Campo | Tipo | Origen | Obligatorio | Descripción |
|---|---|---|---|---|
| `Authorization` | String | Header | Sí | `Bearer <JWT>` |
| `latitude` | Double | Query | Sí | Latitud del punto de referencia. Rango: `-90.0` a `90.0` |
| `longitude` | Double | Query | Sí | Longitud del punto de referencia. Rango: `-180.0` a `180.0` |
| `radiusMeters` | Double | Query | No (default: `500`) | Radio de búsqueda en metros. Máximo: 5000 |

#### Response exitoso — `200 OK` — Array

| Campo | Tipo | Descripción |
|---|---|---|
| `userId` | String | ID del estudiante cercano |
| `latitude` | Double | Latitud de ese usuario |
| `longitude` | Double | Longitud de ese usuario |
| `campusZone` | String | Zona del campus |
| `distanceMeters` | Double | Distancia calculada con Haversine desde el punto de referencia |
| `updatedAt` | String (ISO 8601) | Última actualización de ubicación |

#### Ejemplo

```
GET /api/v1/geo/nearby?latitude=4.6035&longitude=-74.0655&radiusMeters=500
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

```json
// Response 200 OK
[
  {
    "userId": "user-456",
    "latitude": 4.6040,
    "longitude": -74.0660,
    "campusZone": "Bloque B",
    "distanceMeters": 87.3,
    "updatedAt": "2026-05-13T20:55:00"
  }
]
```

#### Errores manejados

| Código HTTP | Escenario | Mensaje |
|---|---|---|
| 400 | Radio ≤ 0 o > 5000 metros | `"INVALID_RADIUS"` |
| 400 | Coordenadas fuera de rango | `"BAD_REQUEST"` |
| 401 | JWT inválido o ausente | `"TOKEN_INVALID"` |

---

### Resumen de todos los Endpoints

| Método | Endpoint | Funcionalidad | Código exitoso |
|---|---|---|---|
| PUT | `/api/v1/geo/location` | F01 — Actualizar ubicación | 200 |
| GET | `/api/v1/geo/location/{userId}` | F02 — Consultar ubicación de usuario | 200 |
| GET | `/api/v1/geo/location/me` | F03 — Consultar ubicación propia | 200 |
| GET | `/api/v1/geo/nearby` | F04 — Usuarios cercanos | 200 |

---

## 10. Colas de Mensajería

**Broker utilizado:** RabbitMQ (CloudAMQP)

#### Tópicos / Colas que PUBLICA (produce)

| Exchange | Routing Key | Evento | Payload | Cuándo se publica |
|---|---|---|---|---|
| `geo.exchange` (Topic) | `geo.location.updated` | Ubicación actualizada | `{ userId, latitude, longitude, campusZone, updatedAt }` | Cada vez que un estudiante actualiza su ubicación exitosamente (F01) |

> Si RabbitMQ no está disponible, el fallo se registra como WARNING en los logs pero no interrumpe la respuesta HTTP al cliente — la actualización en MongoDB ya fue exitosa.

#### Tópicos / Colas que CONSUME (suscribe)

Este módulo **no consume** colas. Solo publica.

---

## 11. Evidencia de Pruebas Unitarias

### Clases de prueba implementadas

```
src/test/java/edu/eci/patricia/geolocalization/
├── domain/
│   └── model/
│       └── LocationTest.java                        → Valida invariantes del modelo y Value Object Coordinates
├── application/
│   └── usecase/
│       ├── UpdateLocationUseCaseTest.java           → Valida flujo completo: persistencia + resolución de zona + evento
│       ├── GetLocationUseCaseTest.java              → Valida búsqueda exitosa y lanzamiento de LocationNotFoundException
│       └── GetNearbyUsersUseCaseTest.java           → Valida radio, orden por distancia e InvalidRadiusException
├── entrypoints/
│   ├── rest/controller/
│   │   └── GeoControllerTest.java                  → Pruebas de integración de capa web con @WebMvcTest
│   └── advice/
│       └── GlobalExceptionHandlerTest.java         → Valida todos los mapeos de excepción → HTTP
└── GeolocalizationApplicationTests.java            → Verifica que el contexto Spring arranca correctamente
```

### Cómo ejecutar las pruebas

```bash
# Ejecutar todas las pruebas
./mvnw test

# Ejecutar una prueba específica
./mvnw test -Dtest=UpdateLocationUseCaseTest

# Ejecutar todas las pruebas + reporte JaCoCo
./mvnw verify
```

### Captura — Resultado de ejecución

<div align="center">
<img src="docs/evidencia_pruebas.png" alt="Evidencia pruebas unitarias" width="700"/>
</div>

> ⚠️ **Reemplazar esta imagen** con captura del resultado real de `./mvnw test`

---

## 12. Evidencia de Análisis de Cobertura

### Generar el reporte

```bash
./mvnw clean test jacoco:report
# Reporte en: target/site/jacoco/index.html
```

### Captura — Reporte JaCoCo

<div align="center">
<img src="docs/evidencia_cobertura.png" alt="Reporte de cobertura JaCoCo" width="700"/>
</div>

> ⚠️ **Reemplazar esta imagen** con captura del reporte JaCoCo mostrando % de cobertura

### Métricas objetivo

| Métrica | Objetivo | Obtenido |
|---|---|---|
| Cobertura de instrucciones | ≥ 80% | [X]% |
| Clases excluidas del análisis | Config, DTOs, Mappers, Persistence, Main | — |

> Las clases excluidas del análisis JaCoCo son: `GeolocalizationApplication`, `infrastructure/config/**`, `infrastructure/adapters/persistence/**`, `infrastructure/adapters/adapter/**`, `application/dto/**`, `entrypoints/rest/request/**`, `entrypoints/rest/mapper/**`, `entrypoints/advice/ErrorResponse`.

---

## 13. Cómo Ejecutar el Proyecto

### Prerrequisitos

- Java 21
- Maven 3.9+
- Docker & Docker Compose (solo para modo Docker)

### Variables de entorno requeridas

| Variable | Valor por defecto (dev) | Descripción |
|---|---|---|
| `MONGODB_URI` | Atlas URI hardcodeada en `application-dev.yml` | URI de conexión a MongoDB |
| `RABBITMQ_HOST` | `woodpecker.rmq.cloudamqp.com` | Host de RabbitMQ |
| `RABBITMQ_PORT` | `5671` | Puerto RabbitMQ (SSL) |
| `RABBITMQ_USERNAME` | `thjdybjd` | Usuario RabbitMQ |
| `RABBITMQ_PASSWORD` | (ver `application-dev.yml`) | Contraseña RabbitMQ |
| `RABBITMQ_VIRTUAL_HOST` | `thjdybjd` | Virtual host RabbitMQ |
| `JWT_SECRET` | `dev-secret-key-must-be-at-least-32-characters` | Secreto compartido con M01 para validar JWT |
| `GOOGLE_MAPS_API_KEY` | _(vacío — zona queda nula)_ | API Key de Google Maps Geocoding |
| `SERVER_PORT` | `8084` | Puerto del servidor |

### Modos de ejecución

| Modo | Comando | Puerto | Swagger |
|---|---|---|---|
| Local (perfil dev) | `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` | 8084 | `http://localhost:8084/swagger-ui.html` |
| Docker Compose | `docker compose up --build` | 8084 | `http://localhost:8084/swagger-ui.html` |

### Opción 1 — Ejecución local

```bash
# 1. Clonar el repositorio
git clone https://github.com/[ORG]/snorlax-energy-geolocalization-service.git
cd snorlax-energy-geolocalization-service

# 2. Ejecutar con perfil dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Opción 2 — Docker Compose

```bash
docker compose up --build
```

---

## 14. Evidencia del Despliegue CI/CD

<div align="center">
<img src="docs/evidencia_cicd.png" alt="Pipeline GitHub Actions exitoso" width="700"/>
</div>

> ⚠️ **Reemplazar esta imagen** con captura del pipeline exitoso en GitHub Actions

---

## 15. Link Swagger Desplegado

| Ambiente | URL |
|---|---|
| **Producción** | `[URL DE DESPLIEGUE]/swagger-ui.html` |
| **Local** | `http://localhost:8084/swagger-ui.html` |
| **Docker** | `http://localhost:8084/swagger-ui.html` |
| **OpenAPI JSON** | `http://localhost:8084/v3/api-docs` |

---

## 16. Estructura del Código

```
snorlax-energy-geolocalization-service/
│
├── src/
│   ├── main/
│   │   ├── java/edu/eci/patricia/geolocalization/
│   │   │   │
│   │   │   ├── domain/                              # CAPA DE DOMINIO
│   │   │   │   ├── model/
│   │   │   │   │   └── Location.java               # Entidad principal de dominio
│   │   │   │   ├── ports/
│   │   │   │   │   ├── in/                          # Puertos de entrada (casos de uso)
│   │   │   │   │   │   ├── UpdateLocationPort.java
│   │   │   │   │   │   ├── GetLocationPort.java
│   │   │   │   │   │   └── GetNearbyUsersPort.java
│   │   │   │   │   └── out/                         # Puertos de salida
│   │   │   │   │       ├── LocationRepositoryPort.java
│   │   │   │   │       └── CampusZoneResolverPort.java
│   │   │   │   ├── exceptions/
│   │   │   │   │   ├── LocationNotFoundException.java
│   │   │   │   │   ├── UserNotFoundException.java
│   │   │   │   │   └── InvalidRadiusException.java
│   │   │   │   └── valueobjects/
│   │   │   │       └── Coordinates.java             # Valida lat/lon + Haversine
│   │   │   │
│   │   │   ├── application/                         # CAPA DE APLICACIÓN
│   │   │   │   ├── usecase/
│   │   │   │   │   ├── UpdateLocationUseCase.java
│   │   │   │   │   ├── GetLocationUseCase.java
│   │   │   │   │   └── GetNearbyUsersUseCase.java
│   │   │   │   └── dto/
│   │   │   │       ├── request/
│   │   │   │       │   └── UpdateLocationRequestDto.java
│   │   │   │       └── response/
│   │   │   │           ├── LocationResponseDto.java
│   │   │   │           └── NearbyUserResponseDto.java
│   │   │   │
│   │   │   ├── infrastructure/                      # CAPA DE INFRAESTRUCTURA
│   │   │   │   ├── adapters/
│   │   │   │   │   ├── adapter/
│   │   │   │   │   │   └── LocationRepositoryAdapter.java
│   │   │   │   │   └── persistence/
│   │   │   │   │       ├── entity/
│   │   │   │   │       │   └── LocationDocument.java   # Documento MongoDB con índice 2dsphere
│   │   │   │   │       └── repository/
│   │   │   │   │           └── LocationMongoRepository.java
│   │   │   │   ├── external/
│   │   │   │   │   ├── GoogleGeocodingAdapter.java  # Implementa CampusZoneResolverPort
│   │   │   │   │   ├── LocationEventPublisher.java  # Publica a RabbitMQ
│   │   │   │   │   ├── JwtService.java              # Validación local de JWT
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── GeocodingResponseDto.java
│   │   │   │   │       └── LocationUpdatedEventDto.java
│   │   │   │   └── config/
│   │   │   │       ├── SecurityConfig.java          # JWT filter + SecurityFilterChain
│   │   │   │       └── RabbitMQConfig.java          # Exchange, RabbitTemplate, RabbitAdmin
│   │   │   │
│   │   │   └── entrypoints/                         # CAPA DE ENTRADA
│   │   │       ├── rest/
│   │   │       │   ├── controller/
│   │   │       │   │   └── GeoController.java
│   │   │       │   ├── request/
│   │   │       │   │   └── UpdateLocationRequest.java
│   │   │       │   └── mapper/
│   │   │       │       └── GeoRestMapper.java
│   │   │       └── advice/
│   │   │           ├── GlobalExceptionHandler.java
│   │   │           └── ErrorResponse.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml                      # Config base (perfil activo: dev)
│   │       └── application-dev.yml                  # Config dev: MongoDB Atlas, RabbitMQ, Google Maps
│   │
│   └── test/
│       ├── java/edu/eci/patricia/geolocalization/
│       │   ├── domain/model/LocationTest.java
│       │   ├── application/usecase/
│       │   │   ├── UpdateLocationUseCaseTest.java
│       │   │   ├── GetLocationUseCaseTest.java
│       │   │   └── GetNearbyUsersUseCaseTest.java
│       │   ├── entrypoints/
│       │   │   ├── rest/controller/GeoControllerTest.java
│       │   │   └── advice/GlobalExceptionHandlerTest.java
│       │   └── GeolocalizationApplicationTests.java
│       └── resources/
│           └── application.properties               # Config de test: excluye MongoDB, RabbitMQ y UserDetailsService
│
├── docs/                                            # Imágenes de diagramas y evidencias
├── .github/workflows/                               # Pipelines CI/CD
│   ├── ci.yml
│   └── cd.yml
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 17. Código Documentado

Todos los endpoints están documentados con anotaciones OpenAPI:

- `@Tag(name = "Geolocation", description = "...")` — agrupa los endpoints bajo la categoría Geolocation en Swagger
- `@Operation(summary = "...", description = "...")` — describe el propósito y comportamiento de cada endpoint
- `@ApiResponses` / `@ApiResponse(responseCode = "...", description = "...")` — documenta cada respuesta posible
- `@SecurityRequirement(name = "bearerAuth")` — indica que todos los endpoints requieren JWT
- `@Parameter` y `@Schema` — documentan parámetros de entrada y campos de DTOs

Los casos de uso documentan únicamente lógica no obvia: la fórmula de Haversine en `Coordinates`, el manejo de fallos silenciosos en `LocationEventPublisher` y la priorización de establecimientos en `GoogleGeocodingAdapter`.

Acceso a la documentación interactiva:
- **Swagger UI:** `http://localhost:8084/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8084/v3/api-docs`

---

## 18. Conexiones con Servicios Externos

| Servicio | Tipo | Configuración | Propósito | Manejo de fallo |
|---|---|---|---|---|
| MongoDB Atlas | BD documental | Variable `MONGODB_URI` | Persistencia de ubicaciones con índice geoespacial 2dsphere | Spring lanza excepción; GlobalExceptionHandler retorna 500 |
| RabbitMQ (CloudAMQP) | Broker AMQP | Variables `RABBITMQ_*` | Publicación de eventos `geo.location.updated` para Matching y Parches | Fallo silencioso: se registra WARNING en logs, la respuesta HTTP no se ve afectada |
| Google Maps Geocoding API | HTTP REST | Variable `GOOGLE_MAPS_API_KEY` | Inversión de coordenadas GPS a nombre de zona del campus | Fallo silencioso: `campusZone` queda como la enviada por el cliente o nula |
| M01 — Autenticación | JWT (validación local) | Variable `JWT_SECRET` | Validación de tokens. Sin llamada HTTP directa a M01. | Retorna 401 Unauthorized |

---

## 19. Pipeline de Desarrollo

El pipeline `.github/workflows/ci.yml` se ejecuta en cada push a `main`, `develop` y `feature/**`, y en cada PR a `main` o `develop`.

### Pasos del pipeline

| Paso | Acción | Descripción |
|---|---|---|
| 1 | Checkout | Clona el repositorio con `actions/checkout@v4` |
| 2 | Setup JDK 21 | Configura Java con `actions/setup-java@v4` (distribución Temurin) |
| 3 | Cache Maven | Restaura dependencias cacheadas para acelerar el build |
| 4 | Compilar | `./mvnw compile` — verifica que el código sea válido |
| 5 | Tests | `./mvnw test` — ejecuta las 18 pruebas unitarias |
| 6 | Docker Build | Construye la imagen Docker para validar el Dockerfile |

```yaml
name: CI Pipeline

on:
  push:
    branches: [ main, develop, feature/** ]
  pull_request:
    branches: [ main, develop ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Cache Maven
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
      - run: ./mvnw compile
      - run: ./mvnw test
      - run: docker build -t snorlax-energy-geolocalization:test .
```

---

## 20. Pipeline de Producción

El pipeline `.github/workflows/cd.yml` se activa únicamente en push a `main` con tag `v*.*.*`.

### Pasos del pipeline

| Paso | Acción | Descripción |
|---|---|---|
| 1 | Checkout | Clona el repositorio |
| 2 | Setup JDK 21 | Configura Java |
| 3 | Tests completos | `./mvnw verify` — unitarias + cobertura JaCoCo |
| 4 | Docker Build | Construye imagen `ghcr.io/[org]/snorlax-energy-geolocalization:{tag}` |
| 5 | Docker Push | Publica imagen en GitHub Container Registry |
| 6 | Deploy | Despliega en plataforma cloud usando la imagen publicada |

```yaml
name: CD Pipeline

on:
  push:
    tags: [ 'v*.*.*' ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - run: ./mvnw verify
      - name: Docker Build & Push
        run: |
          docker build -t ghcr.io/[ORG]/snorlax-energy-geolocalization:${{ github.ref_name }} .
          docker push ghcr.io/[ORG]/snorlax-energy-geolocalization:${{ github.ref_name }}
```

### Captura — Pipeline de producción exitoso

<div align="center">
<img src="docs/evidencia_cicd_prod.png" alt="Pipeline de producción exitoso" width="700"/>
</div>

> ⚠️ **Reemplazar esta imagen** con captura del pipeline de producción exitoso

---

## 21. Dockerizado

### Dockerfile

```dockerfile
# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

El `docker-compose.yml` levanta los siguientes servicios en red compartida:

| Servicio | Imagen | Puerto | Perfil Spring |
|---|---|---|---|
| `geolocalization-service` | Build local | 8084:8084 | `docker` |

> MongoDB Atlas y RabbitMQ CloudAMQP son servicios cloud — no se levantan localmente con Docker Compose.

```bash
# Levantar el servicio
docker compose up --build

# Detener
docker compose down

# Ver logs
docker compose logs -f geolocalization-service
```

---

## 22. Estrategia de Versionamiento

### Estrategia de Ramas (Git Flow)

| Rama | Propósito | Reglas |
|---|---|---|
| `main` | Versión estable lista para producción | Solo recibe merges desde `release/*` y `hotfix/*`. Cada merge crea un tag SemVer `vX.Y.Z`. Protegida con PR obligatorio. |
| `develop` | Integración continua de trabajo | Recibe merges desde `feature/*` y `release/*`. Protegida. |
| `feature/*` | Desarrollo de funcionalidades | Base: `develop`. Se fusiona a `develop` mediante PR. |

### Convenciones de ramas

```
feature/[nombre-funcionalidad]
hotfix/[descripcion-del-fix]
release/[version]
```

### Convenciones de commits

```
[tipo]: [descripción específica de la acción]
```

| Tipo | Uso |
|---|---|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de errores |
| `docs` | Cambios en documentación |
| `refactor` | Refactorización sin cambio de comportamiento |
| `test` | Adición o modificación de pruebas |
| `chore` | Cambios de configuración, build o dependencias |

---

<div align="center">

### Equipo Snorlax Energy

![Module](https://img.shields.io/badge/Module-M07_Geolocalization-orange?style=for-the-badge)
![Course](https://img.shields.io/badge/Course-DOSW-orange?style=for-the-badge)
![Year](https://img.shields.io/badge/Year-2026--1-blue?style=for-the-badge)

**Escuela Colombiana de Ingeniería Julio Garavito**

</div>
