# Colección de Pruebas de Postman — Meru App

Este directorio contiene la suite completa de pruebas para la API REST del backend de **Meru App** y su integración con **Keycloak**.

---

## 📂 Archivos en esta carpeta

- **`Meru_App_Local_E2E.postman_collection.json`**: Colección v2.1 de Postman con el 100% de los endpoints desarrollados (Autenticación, Seguridad/Roles, Alumnos/Profesores, Entrenamiento, Clases/Reservas y Pagos/Suscripciones).

---

## 🚀 Cómo importar y empezar a probar en 3 pasos

### Paso 1: Importar la colección en Postman
1. Abrí **Postman**.
2. Hacé clic en el botón **Import** (arriba a la izquierda).
3. Arrastrá o seleccioná el archivo `Meru_App_Local_E2E.postman_collection.json`.

### Paso 2: Verificar que los servicios estén corriendo
- **Keycloak:** en `http://localhost:8081` (levantado desde la carpeta `keycloak` con `docker compose up -d`).
- **Backend:** en `http://localhost:8080` (ejecutando la app Spring Boot).

### Paso 3: Obtener el token de autenticación
1. En la barra lateral de Postman, desplegá la carpeta **`0. Autenticación (Keycloak)`**.
2. Ejecutá la petición **`0.1 Obtener Token (admin.test)`** (o profesor/alumno).
3. Hacé clic en **Send**.
4. ¡Listo! El script de prueba captura automáticamente el `access_token` de la respuesta y lo asigna a la variable `jwt_token` de la colección.

> **Importante:** Todas las peticiones del backend ya tienen configurada la herencia de autenticación (`Bearer {{jwt_token}}`), por lo que **no tenés que copiar ni pegar tokens manualmente**.

---

## 🧪 Estructura de carpetas de la colección

1. **`0. Autenticación (Keycloak)`**:
   - `0.1 Obtener Token (admin.test)`
   - `0.2 Obtener Token (profesor.test)`
   - `0.3 Obtener Token (alumno.test)`
2. **`1. Seguridad y Test de Roles`**:
   - `1.1 Ping Público` (para validar conectividad sin token)
   - `1.2 Info y Claims del Token` (muestra el subject, email y roles del JWT actual)
   - `1.3 Test Admin Only` (valida permisos de rol `admin`)
   - `1.4 Test Alumno Only` (valida permisos de rol `alumno`)
   - `1.5 Sincronizar Usuario en BD`
3. **`2. Alumnos y Profesores`**:
   - Registro y consulta de profesores y alumnos, y asignación de profesor a alumno.
4. **`3. Entrenamiento, Rutinas y Progreso`**:
   - CRUD y filtros de ejercicios, registro e historial de 1RM, creación de plantillas de rutinas, clonación a alumnos y registro de progreso de sets.
5. **`4. Clases, Reservas, Check-ins y Calificaciones`**:
   - Creación y listado de clases, flujo de reserva y cancelación, registro de check-in presencial y calificaciones con comentarios.
6. **`5. Planes, Suscripciones y Pagos`**:
   - Creación y consulta de planes y membresías, pago en efectivo (aprobación inmediata), simulación de Mercado Pago y webhook de confirmación.

---

## ⚙️ Variables preconfiguradas (Colección)

| Variable | Valor por defecto | Descripción |
| :--- | :--- | :--- |
| `baseUrl` | `http://localhost:8080` | URL base del backend Spring Boot |
| `keycloakUrl` | `http://localhost:8081` | URL base de Keycloak |
| `jwt_token` | *(Dinámico)* | Se autocompleta con el script de obtención de token |
