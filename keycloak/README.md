# Keycloak — Meru App

Servidor de identidad (autenticación y autorización) para el ecosistema de **Meru App**.
Corre en su propio contenedor Docker independiente del backend Spring Boot.

---

## 📋 Requisitos previos

- Tener instalado y en ejecución [Docker Desktop](https://www.docker.com/products/docker-desktop/).

---

## 🚀 Cómo levantarlo en desarrollo local

1. Abrir una terminal en esta carpeta (`keycloak`):
   ```bash
   cd keycloak
   ```

2. (Opcional) Si querés personalizar las contraseñas podés copiar `.env.example` como `.env`, pero **no es obligatorio**; por defecto ya viene listo para arrancar:
   ```bash
   cp .env.example .env
   ```

3. Levantar los contenedores:
   ```bash
   docker compose up -d
   ```

4. Esperar unos 20-30 segundos para que Keycloak inicialice e importe el realm.

---

## 🌐 Consola de administración

- **URL:** [http://localhost:8081](http://localhost:8081)
- **Usuario administrador (Master):** `admin`
- **Contraseña:** `admin`

> **Nota:** Se utiliza el puerto **8081** para no generar conflicto con el backend de Spring Boot (que corre en el puerto **8080**).

---

## 👥 Datos pre-cargados automáticamente

El archivo `realm-export/meru-app-realm.json` se importa solo al iniciar el contenedor gracias al flag `--import-realm`. Esto deja listo el realm **`meru-app`** con los roles, clientes y los siguientes usuarios de prueba:

| Usuario | Contraseña | Rol asignado | Email |
| :--- | :--- | :--- | :--- |
| `admin.test` | `admin1234` | `admin` | `admin.test@meru.app` |
| `profesor.test` | `profesor1234` | `profesor` | `profesor.test@meru.app` |
| `alumno.test` | `alumno1234` | `alumno` | `alumno.test@meru.app` |

---

## 🛠️ Comandos útiles

- **Ver logs de Keycloak en tiempo real:**
  ```bash
  docker compose logs -f keycloak
  ```

- **Detener el servicio:**
  ```bash
  docker compose down
  ```

- **Reiniciar desde cero (borrar base de datos y reimportar realm limpio):**
  ```bash
  docker compose down -v
  docker compose up -d
  ```

---

## 💾 Exportar cambios al realm (para versionar en Git)

Si realizás modificaciones en la configuración del realm desde la consola de Keycloak (como agregar un nuevo rol, client o usuario) y querés guardarlas para tus compañeros:

```bash
docker exec meru-keycloak /opt/keycloak/bin/kc.sh export \
  --dir /opt/keycloak/data/import \
  --realm meru-app \
  --users realm_file
```

Esto actualizará el archivo en `realm-export/meru-app-realm.json`, el cual luego debés commitear en Git.
