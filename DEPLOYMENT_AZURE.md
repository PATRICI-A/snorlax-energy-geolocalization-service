# Guía de Despliegue – Azure App Service

**PATRICI.A** · Estándar de Despliegue para todos los Microservicios

Esta guía aplica para **cualquier microservicio** del proyecto. Reemplaza los valores entre `< >` con los datos de tu servicio. Todo se hace desde el portal web de Azure ([portal.azure.com](https://portal.azure.com)).

---

## Resumen de la arquitectura

```
GitHub Actions
      │
      ├─ ci.yml     → Build + Tests (push y PR a main/develop)
      ├─ sonar.yml  → SonarCloud analysis (push y PR a main/develop)
      └─ cd.yml     → Deploy JAR → Azure App Service (solo cuando ci.yml pasa en main)
```

### Workflows del pipeline

| Archivo | Propósito | Trigger |
|---|---|---|
| `ci.yml` | Build, tests, sube artefactos (JAR, JaCoCo, Surefire) | Push y PR a `main`/`develop` |
| `sonar.yml` | Análisis de calidad con SonarCloud | Push y PR a `main`/`develop` |
| `cd.yml` | Deploy del JAR a Azure App Service | Cuando `ci.yml` pasa en `main` |

> El deploy es **automático** → al mergear a `main` y pasar el CI, el JAR se despliega solo a Azure.

**Recursos que se crean en Azure:**

| Recurso | Nombre | Nota |
|---|---|---|
| Resource Group | `patricia-prod` | Ya creado → pídele acceso a Sebastian |
| App Service Plan | `ASP-LIBREBIAgroup-942d` | Ya creado → selecciónalo al crear tu App Service |
| App Service | `app-patricia-<nombre-servicio>` | Uno por microservicio |
| Service Principal | `sp-patricia-github-actions` | Créalo tú para tu propio repo |

---

## Paso 0 – Crear los servicios externos (Redis y RabbitMQ)

> Haz esto **antes** de configurar Azure. Necesitas las credenciales para el Paso 4.

### Redis – Upstash (free tier)

1. Entra a [upstash.com](https://upstash.com) → crea una cuenta
2. Clic en **+ Create Database**
3. Llena los campos:
   - **Name**: `<nombre-servicio>-redis`
   - **Type**: `Regional`
   - **Region**: la más cercana
   - **TLS**: activado
4. Clic en **Create**
5. En la pantalla del database anota:
   - **Endpoint** → es el `REDIS_HOST`
   - **Port** → es el `REDIS_PORT` (normalmente `6379`)
   - **Token** (clic en el ojo para verlo) → es el `REDIS_PASSWORD`
   - `REDIS_SSL` = `true` (porque TLS está activado)

### RabbitMQ – CloudAMQP (free tier)

1. Entra a [cloudamqp.com](https://cloudamqp.com) → crea una cuenta
2. Clic en **+ Create New Instance**
3. Llena los campos:
   - **Name**: `<nombre-servicio>-rabbit`
   - **Plan**: `Little Lemur` (free)
   - **Region**: la más cercana
4. Clic en **Create instance**
5. Entra a la instancia → sección **AMQP details** y anota:
   - **Hosts** → es el `RABBITMQ_HOST`
   - **Port**: `5672` → es el `RABBITMQ_PORT`
   - **User & Vhost** → es el `RABBITMQ_USERNAME` y también el `SPRING_RABBITMQ_VIRTUAL_HOST`
   - **Password** → es el `RABBITMQ_PASSWORD`

> **Importante:** en CloudAMQP el Virtual Host es el mismo valor que el Username. Necesitas configurar `SPRING_RABBITMQ_VIRTUAL_HOST` con ese valor o la conexión fallará.

---

## Paso 1 – Crear el App Service

1. Entra a [portal.azure.com](https://portal.azure.com)
2. En la barra de búsqueda escribe **"App Services"** → clic en el resultado
3. Clic en **+ Crear** → **Aplicación web**
4. **Pestaña Datos básicos**:
   - **Suscripción**: la del proyecto
   - **Grupo de recursos**: selecciona `patricia-prod` (ya existe, pídele acceso a Sebastian si no lo ves)
   - **Nombre**: `app-patricia-<nombre-servicio>`
   - **Publicar**: `Código`
   - **Pila del entorno de tiempo de ejecución**: `Java 21`
   - **Pila de servidor web Java**: `Java SE (Embedded Web Server)`
   - **Sistema operativo**: `Linux`
   - **Región**: `Canada Central`
   - **Plan de App Service**: selecciona `ASP-LIBREBIAgroup-942d` (ya existe)
5. Clic en **Revisar y crear** → **Crear**

---

## Paso 2 – Configurar variables de entorno

1. Entra al App Service `app-patricia-<nombre-servicio>`
2. En el menú izquierdo → **Configuración** → **Variables de entorno**
3. Clic en **+ Agregar** para cada variable

**Variables obligatorias en todos los microservicios:**

| Nombre | Valor |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` |
| `WEBSITES_PORT` | `8080` |

**Variables según lo que use cada microservicio:**

| Nombre | Descripción |
|---|---|
| `JWT_SECRET` | Clave de firma JWT (mín. 32 caracteres) |
| `JWT_EXPIRATION` | Expiración del access token en ms (ej. `900000`) |
| `REDIS_HOST` | Host de Redis |
| `REDIS_PORT` | Puerto de Redis (default: `6379`) |
| `REDIS_PASSWORD` | Contraseña de Redis |
| `REDIS_SSL` | TLS habilitado (`true` / `false`) |
| `RABBITMQ_HOST` | Host de RabbitMQ |
| `RABBITMQ_PORT` | Puerto de RabbitMQ (default: `5672`) |
| `RABBITMQ_USERNAME` | Usuario de RabbitMQ |
| `RABBITMQ_PASSWORD` | Contraseña de RabbitMQ |
| `SPRING_RABBITMQ_VIRTUAL_HOST` | Virtual host de RabbitMQ (en CloudAMQP es igual al username) |
| `MONGODB_URI` | URI de conexión a MongoDB Atlas |
| `MONGODB_DATABASE` | Nombre de la base de datos |
| `<NOMBRE>_SERVICE_URL` | URL base de otro microservicio |

4. Clic en **Guardar** (arriba)

---

## Paso 3 – Obtener las credenciales de Azure (AZURE_CREDENTIALS)

> El Service Principal ya está creado y tiene acceso al Resource Group `patricia-prod`. **Pídele a Sebastian el JSON `AZURE_CREDENTIALS`** → es el mismo para todos los microservicios del proyecto.

El JSON tiene este formato (Sebastian te lo comparte por el canal del equipo):

```json
{
  "clientId":       "...",
  "clientSecret":   "...",
  "tenantId":       "...",
  "subscriptionId": "..."
}
```

Guárdalo completo → lo usarás en el Paso 4.

---

## Paso 4 – Configurar Secrets en GitHub

1. Ve al repositorio en GitHub
2. **Settings** → **Secrets and variables** → **Actions**
3. Clic en **New repository secret** para cada uno:

| Secret | Valor |
|---|---|
| `AZURE_CREDENTIALS` | El JSON completo del Paso 3 |
| `AZURE_APP_SERVICE_NAME` | `app-patricia-<nombre-servicio>` |
| `JWT_SECRET_TEST` | Cualquier string de ≥ 32 caracteres (solo para CI) |
| `SONAR_TOKEN` | `d453a7a0e951ce1dc893b735caaa7e44be3b1e99` ← compartido por todo el equipo |
| `SONAR_ORGANIZATION` | `patrici-a` ← compartido por todo el equipo |
| `SONAR_HOST_URL` | `https://sonarcloud.io` ← compartido por todo el equipo |

---

## Paso 5 – Configurar SonarCloud

> La organización **`patrici-a`** ya está creada. **No crees una nueva.**

1. Entra a [sonarcloud.io](https://sonarcloud.io) con tu cuenta de GitHub
2. Clic en **+** (arriba a la derecha) → **Analyze new project**
3. En el dropdown de Organization selecciona **`patrici-a`**
4. Crea el proyecto:
   - **Display Name**: `<nombre-servicio>`
   - **Visibility**: `Public`
   - Clic en **Next** → selecciona **With GitHub Actions**
5. Selecciona **Maven** como tipo de proyecto
6. Asegúrate de que tu repo tiene el archivo `sonar-project.properties` con este contenido:
   ```properties
   sonar.organization=patrici-a
   sonar.java.binaries=target/classes
   sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
   ```

---

## Paso 6 – Primer despliegue

En tu terminal, desde la carpeta del proyecto:

```bash
git add .
git commit -m "chore: configure Azure App Service deployment"
git push origin main
```

Ve a GitHub → pestaña **Actions** y verás el pipeline corriendo:

```
ci.yml (~3-5 min)  →  cd.yml (~1-2 min)
sonar.yml (~3 min)    (en paralelo con ci.yml)
```

Al terminar la app queda disponible en:
- **API**: `https://app-patricia-<nombre-servicio>.azurewebsites.net/api/v1/<ruta>`
- **Swagger**: `https://app-patricia-<nombre-servicio>.azurewebsites.net`

---

## Rollback

Si un deploy falla y necesitas volver a una versión anterior:

1. Entra al App Service en el portal
2. Menú izquierdo → **Centro de implementación** → **Registros**
3. Selecciona una versión anterior → **Reimplementar**

---

## Ver logs en tiempo real

1. Entra al App Service en el portal
2. Menú izquierdo → **Flujo de registro**
3. Los logs aparecen en tiempo real

---

## Estándar de nombres

| Recurso | Convención | Ejemplo |
|---|---|---|
| Resource Group | `rg-patricia-<entorno>` | `patricia-prod` |
| App Service Plan | `asp-patricia-<entorno>` | `ASP-LIBREBIAgroup-942d` |
| App Service | `app-patricia-<servicio>` | `app-patricia-auth` |
| Service Principal | `sp-patricia-github-actions` | (fijo para todo el proyecto) |

## Estándar de variables de entorno

| Categoría | Variable | Descripción |
|---|---|---|
| Servidor | `SPRING_PROFILES_ACTIVE` | Perfil activo (`dev` / `prod`) |
| Servidor | `WEBSITES_PORT` | Puerto de la app (siempre `8080`) |
| JWT | `JWT_SECRET` | Clave de firma (mín. 32 chars) |
| JWT | `JWT_EXPIRATION` | Expiración access token en ms |
| Redis | `REDIS_HOST` | Host de Redis |
| Redis | `REDIS_PORT` | Puerto (default: `6379`) |
| Redis | `REDIS_PASSWORD` | Contraseña |
| Redis | `REDIS_SSL` | TLS habilitado (`true`/`false`) |
| RabbitMQ | `RABBITMQ_HOST` | Host del broker |
| RabbitMQ | `RABBITMQ_PORT` | Puerto (default: `5672`) |
| RabbitMQ | `RABBITMQ_USERNAME` | Usuario |
| RabbitMQ | `RABBITMQ_PASSWORD` | Contraseña |
| RabbitMQ | `SPRING_RABBITMQ_VIRTUAL_HOST` | Virtual host (en CloudAMQP = mismo valor que username) |
| MongoDB | `MONGODB_URI` | URI de conexión a Atlas |
| MongoDB | `MONGODB_DATABASE` | Nombre de la base de datos |
| Servicios | `<NOMBRE>_SERVICE_URL` | URL base de otro microservicio |
