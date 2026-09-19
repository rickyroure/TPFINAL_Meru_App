# Meru App

Plataforma de gestión integral para gimnasios: rutinas de entrenamiento, reserva de clases, suscripciones y shop de suplementos/consumibles.

**Trabajo Final Integrador — Tecnicatura Universitaria en Programación**

**Integrantes:**
- Ricardo Roure
- Franco Sarru
- Cristian Samuel Siles Rodriguez

**Tutor/a:** Juan Ignacio Schiavonni

---

## 1. Problema y propuesta

Los gimnasios que ofrecen tanto entrenamiento personalizado como clases grupales suelen gestionar cada aspecto del negocio de forma separada y manual: las rutinas de los alumnos se llevan en papel o planillas sueltas, la reserva de clases se coordina por WhatsApp o presencialmente, y la venta de suplementos/consumibles no está integrada con la administración general del gimnasio.

Meru App busca resolver esto centralizando en una sola plataforma:

- La gestión de rutinas de entrenamiento por parte del profesor.
- La administración y reserva de clases grupales.
- La gestión de suscripciones de los alumnos.
- Un shop online de suplementos y consumibles.

## 2. Alcance del proyecto

El sistema contempla tres roles principales: **Administrador** (superusuario), **Profesor** y **Alumno**, cada uno con funcionalidades y vistas propias, sobre una base común de autenticación, notificaciones y gestión de usuarios.

### Módulo Core / Transversal

- **Autenticación y autorización**: login único con control de acceso por rol (admin, profesor, alumno), pensado como RBAC (tabla de roles y permisos en base de datos, no hardcodeado) para poder sumar roles a futuro sin duplicar lógica.
- **Gestión de usuarios**: alta, baja, edición y recuperación de contraseña.
- **Notificaciones por correo electrónico**: validación de cuenta y reseteo de contraseña, envío de comprobantes de pago, notificaciones generales y promociones, recordatorio de cumpleaños, aviso de vencimiento de membresía y de ausencias.
- **Panel de pagos**: selección de método (Mercado Pago o efectivo) — ver detalle en la sección 4.

### Panel Administrador (Superusuario)

- Alta de profesores, usuarios de administración y alumnos.
- Dashboard: cantidad de alumnos por especialidad, análisis de concurrencia (horarios/días de mayor afluencia), alumnos por profesor, ingresos mensuales/anuales, indicadores de viabilidad económica.
- Configuración de tarifas, planes de descuento, y stock/precios de productos del shop.
- Medidor de capacidad del gimnasio: al ingresar, el alumno selecciona la actividad que va a realizar (musculación, spinning, etc.); en base a esto se gestionan cupos por actividad y se muestra el nivel de ocupación a los demás usuarios.
- Panel de ventas: productos más vendidos, control de stock.

### Panel Profesor

- Generación y asignación de planes de entrenamiento (ejercicios, pesos, series, tiempos), tanto genéricos como personalizados.
- Seguimiento de progreso de cada alumno (evolución de cargas, ejercicios más frecuentes, avances generales).
- Vista de alumnos: ejercicios preferidos y antigüedad en el gimnasio.
- Notificación de ausencias.
- Gestión del shop.
- Ficha técnica del alumno, con énfasis en condiciones médicas o enfermedades a tener en cuenta (dato sensible, con acceso restringido por rol).

### Panel Alumno

- Datos personales, ficha técnica/médica, rutinas favoritas y profesor designado.
- Membresía: días restantes, vencimiento, precios de planes mensuales o packs con descuento.
- Plan actual y clases restantes/pendientes.
- Progreso: evolución respecto a los planes (ej. incremento de peso levantado), con visualización gráfica.
- Reserva de clases con cupo limitado.
- Descargas: planes, ficha técnica e historial consolidado.
- Consulta de capacidad del gimnasio antes de asistir.

### Reservas y cupos — regla de negocio

- Al **confirmar** una clase, el cupo disponible de esa clase disminuye en 1.
- Al **cancelar**, el cupo se incrementa en 1, se deja registro de la cancelación (con horario) y se restituye el crédito al alumno para usar en otra clase.
- La cancelación solo puede gestionarse hasta **30 minutos antes** del horario de inicio de la clase.

## 3. Stack tecnológico

| Componente | Tecnología |
|---|---|
| Frontend | React (Vite) + React Router + Axios |
| Backend | Java + Spring Boot (Spring Web, Spring Data JPA, Spring Security + JWT, Bean Validation) + Maven |
| Documentación de API | Swagger (springdoc-openapi) |
| Base de datos | PostgreSQL (relacional) |
| Testing | JUnit + Mockito |
| API externa | Mercado Pago (Checkout Pro + webhooks) |

**Justificación del modelo relacional:** se evaluó una base no relacional dado que distintos alumnos pueden tener rutinas orientadas a objetivos muy diferentes (ej. running vs. fútbol) o directamente no tener rutina asignada (ej. alumnos que solo asisten a clases de pilates). Sin embargo, esto no es un problema de flexibilidad de esquema: la entidad `Ejercicio` mantiene siempre la misma estructura, y lo que varía es la combinación de ejercicios que integra cada rutina — una relación N a N que se resuelve con una tabla intermedia (`RutinaEjercicio`). Además, el resto del dominio (cupos de clases, suscripciones, stock del shop) es fuertemente transaccional, por lo que las garantías de integridad referencial y atomicidad de PostgreSQL son más apropiadas que un modelo documental.

### Despliegue (a implementar hacia la entrega final)

| Componente | Servicio |
|---|---|
| Backend | Render / Railway |
| Frontend | Vercel / Netlify |
| Base de datos | Supabase |

## 4. Integración con API externa: Mercado Pago

- **Checkout Pro**: pago único, con webhook (IPN) para confirmar la orden automáticamente.
- **Membresía**: pago manual vía link de Checkout Pro generado por el sistema; la renovación automática (Preapproval) queda como mejora a futuro.
- **Pagos en efectivo**: registrados manualmente por el administrador, bajo el mismo modelo de datos que los pagos digitales. El admin carga el monto y a quién corresponde, y el sistema marca la membresía como al día — sin integración externa involucrada.
- Se utilizarán credenciales de sandbox para desarrollo y testing.

## 5. Estructura del repositorio

```
/frontend      → proyecto React
/backend       → proyecto Spring Boot
/database      → scripts DDL, diagrama ER
/docs          → informes y entregas
README.md
```

## 6. Consideraciones adicionales

- **API de clima** (ej. OpenWeatherMap): funcionalidad secundaria para sugerencias contextuales (ej. recomendaciones de entrenamiento outdoor). No forma parte del alcance comprometido para esta entrega.
- **Historial de pagos/facturación**: consultable tanto por el alumno como por el administrador, además del comprobante enviado por correo.
- **Feedback/calificación**: el alumno podrá calificar clases o profesores, aportando datos al panel del administrador.
- **Asociación alumno–profesor**: se podrá reasignar el profesor de un alumno para seguimiento y evolución del entrenamiento.
- **Funcionalidades accesorias**: ej. cronómetro tipo tabata.
- **Pendiente de definición para la 2.ª entrega**: mecanismo de check-in/check-out para actividades sin reserva previa (ej. musculación libre), necesario para que el medidor de capacidad funcione también en esos casos.


-Prueba commit