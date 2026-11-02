
-- Meru App — Esquema de base de datos (PostgreSQL / Supabase)
-- =====================================================================
-- La identidad (login, password, roles) la gestiona Keycloak por completo.
-- Esta base solo guarda keycloak_id como vínculo con la identidad externa.
-- =====================================================================

-- ---------------------------------------------------------------------
-- MÓDULO: SEGURIDAD / USUARIOS
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    keycloak_id     VARCHAR(36) NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    nombre          VARCHAR(100) NOT NULL,
    apellido        VARCHAR(100) NOT NULL,
    telefono        VARCHAR(30),
    fecha_nacimiento DATE,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
                        CHECK (estado IN ('ACTIVO','INACTIVO')),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE profesor (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE CASCADE,
    especialidad    VARCHAR(100)
);

CREATE TABLE alumno (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL UNIQUE REFERENCES usuario(id) ON DELETE CASCADE,
    profesor_id     BIGINT REFERENCES profesor(id) ON DELETE SET NULL,
    apto_fisico     BOOLEAN NOT NULL DEFAULT false,
    fecha_ingreso   DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE INDEX idx_alumno_profesor ON alumno(profesor_id);

-- ---------------------------------------------------------------------
-- MÓDULO: ENTRENAMIENTO
-- ---------------------------------------------------------------------
CREATE TABLE ejercicio (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    grupo_muscular  VARCHAR(100),
    descripcion     TEXT
);

CREATE TABLE alumno_1rm (
    id              BIGSERIAL PRIMARY KEY,
    alumno_id       BIGINT NOT NULL REFERENCES alumno(id) ON DELETE CASCADE,
    ejercicio_id    BIGINT NOT NULL REFERENCES ejercicio(id) ON DELETE CASCADE,
    valor_1rm       DECIMAL(6,2) NOT NULL,
    fecha_medicion  DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE INDEX idx_alumno_1rm_alumno ON alumno_1rm(alumno_id);
CREATE INDEX idx_alumno_1rm_ejercicio ON alumno_1rm(ejercicio_id);

CREATE TABLE rutina (
    id                  BIGSERIAL PRIMARY KEY,
    profesor_id         BIGINT NOT NULL REFERENCES profesor(id) ON DELETE CASCADE,
    alumno_id           BIGINT REFERENCES alumno(id) ON DELETE SET NULL,
    nombre              VARCHAR(150) NOT NULL,
    fecha_inicio        DATE,
    duracion_semanas    INT NOT NULL CHECK (duracion_semanas > 0)
);

CREATE INDEX idx_rutina_profesor ON rutina(profesor_id);
CREATE INDEX idx_rutina_alumno ON rutina(alumno_id);

CREATE TABLE plan_ejercicio (
    id              BIGSERIAL PRIMARY KEY,
    rutina_id       BIGINT NOT NULL REFERENCES rutina(id) ON DELETE CASCADE,
    ejercicio_id    BIGINT NOT NULL REFERENCES ejercicio(id) ON DELETE RESTRICT,
    sesion          VARCHAR(1) NOT NULL CHECK (sesion IN ('A','B','C','D','E')),
    numero_semana   INT NOT NULL CHECK (numero_semana > 0),
    orden           INT NOT NULL DEFAULT 1,
    series          INT NOT NULL CHECK (series > 0),
    reps            INT NOT NULL CHECK (reps > 0),
    pausa_segundos  INT,
    carga_pct       DECIMAL(5,2),
    rpe             DECIMAL(3,1)
);

CREATE INDEX idx_plan_ejercicio_rutina ON plan_ejercicio(rutina_id);
CREATE INDEX idx_plan_ejercicio_ejercicio ON plan_ejercicio(ejercicio_id);

CREATE TABLE progreso_set (
    id                      BIGSERIAL PRIMARY KEY,
    plan_ejercicio_id       BIGINT NOT NULL REFERENCES plan_ejercicio(id) ON DELETE CASCADE,
    numero_serie            INT NOT NULL CHECK (numero_serie > 0),
    peso_realizado          DECIMAL(6,2) NOT NULL,
    repeticiones_realizadas INT,
    fecha                   DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE INDEX idx_progreso_set_plan_ejercicio ON progreso_set(plan_ejercicio_id);

-- ---------------------------------------------------------------------
-- MÓDULO: CLASES Y RESERVAS
-- ---------------------------------------------------------------------
CREATE TABLE clase (
    id              BIGSERIAL PRIMARY KEY,
    profesor_id     BIGINT NOT NULL REFERENCES profesor(id) ON DELETE CASCADE,
    tipo_actividad  VARCHAR(50) NOT NULL,
    horario         TIMESTAMP NOT NULL,
    cupo_maximo     INT NOT NULL CHECK (cupo_maximo > 0),
    cupo_disponible INT NOT NULL CHECK (cupo_disponible >= 0)
);

CREATE INDEX idx_clase_profesor ON clase(profesor_id);
CREATE INDEX idx_clase_horario ON clase(horario);

CREATE TABLE reserva (
    id              BIGSERIAL PRIMARY KEY,
    clase_id        BIGINT NOT NULL REFERENCES clase(id) ON DELETE CASCADE,
    alumno_id       BIGINT NOT NULL REFERENCES alumno(id) ON DELETE CASCADE,
    estado          VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADA'
                        CHECK (estado IN ('CONFIRMADA','CANCELADA','ASISTIO','NO_ASISTIO')),
    fecha_reserva   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_reserva_clase ON reserva(clase_id);
CREATE INDEX idx_reserva_alumno ON reserva(alumno_id);

CREATE TABLE checkin (
    id              BIGSERIAL PRIMARY KEY,
    alumno_id       BIGINT NOT NULL REFERENCES alumno(id) ON DELETE CASCADE,
    actividad       VARCHAR(50) NOT NULL,
    fecha_hora      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_checkin_alumno ON checkin(alumno_id);
CREATE INDEX idx_checkin_fecha ON checkin(fecha_hora);

CREATE TABLE calificacion (
    id              BIGSERIAL PRIMARY KEY,
    clase_id        BIGINT NOT NULL REFERENCES clase(id) ON DELETE CASCADE,
    alumno_id       BIGINT NOT NULL REFERENCES alumno(id) ON DELETE CASCADE,
    puntaje         INT NOT NULL CHECK (puntaje BETWEEN 1 AND 5),
    comentario      TEXT
);

CREATE INDEX idx_calificacion_clase ON calificacion(clase_id);
CREATE INDEX idx_calificacion_alumno ON calificacion(alumno_id);

-- ---------------------------------------------------------------------
-- MÓDULO: PAGOS
-- ---------------------------------------------------------------------
CREATE TABLE plan (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    precio          DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    duracion_dias   INT NOT NULL CHECK (duracion_dias > 0)
);

CREATE TABLE suscripcion (
    id              BIGSERIAL PRIMARY KEY,
    alumno_id       BIGINT NOT NULL REFERENCES alumno(id) ON DELETE CASCADE,
    plan_id         BIGINT NOT NULL REFERENCES plan(id) ON DELETE RESTRICT,
    fecha_inicio    DATE NOT NULL,
    fecha_fin       DATE NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'
                        CHECK (estado IN ('ACTIVA','VENCIDA','CANCELADA'))
);

CREATE INDEX idx_suscripcion_alumno ON suscripcion(alumno_id);
CREATE INDEX idx_suscripcion_fecha_fin ON suscripcion(fecha_fin);

-- ---------------------------------------------------------------------
-- MÓDULO: SHOP
-- ---------------------------------------------------------------------
CREATE TABLE producto (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    precio          DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    stock           INT NOT NULL DEFAULT 0 CHECK (stock >= 0)
);

CREATE TABLE orden (
    id              BIGSERIAL PRIMARY KEY,
    alumno_id       BIGINT NOT NULL REFERENCES alumno(id) ON DELETE CASCADE,
    total           DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    estado          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                        CHECK (estado IN ('PENDIENTE','PAGADA','CANCELADA')),
    fecha           TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_orden_alumno ON orden(alumno_id);

CREATE TABLE item_orden (
    id                  BIGSERIAL PRIMARY KEY,
    orden_id            BIGINT NOT NULL REFERENCES orden(id) ON DELETE CASCADE,
    producto_id         BIGINT NOT NULL REFERENCES producto(id) ON DELETE RESTRICT,
    cantidad            INT NOT NULL CHECK (cantidad > 0),
    precio_unitario     DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0)
);

CREATE INDEX idx_item_orden_orden ON item_orden(orden_id);
CREATE INDEX idx_item_orden_producto ON item_orden(producto_id);

-- ---------------------------------------------------------------------
-- PAGO (polimórfico: suscripción XOR orden)
-- ---------------------------------------------------------------------
CREATE TABLE pago (
    id                      BIGSERIAL PRIMARY KEY,
    suscripcion_id          BIGINT REFERENCES suscripcion(id) ON DELETE SET NULL,
    orden_id                BIGINT REFERENCES orden(id) ON DELETE SET NULL,
    monto                   DECIMAL(10,2) NOT NULL CHECK (monto >= 0),
    metodo                  VARCHAR(20) NOT NULL CHECK (metodo IN ('MERCADOPAGO','EFECTIVO')),
    estado                  VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                                CHECK (estado IN ('PENDIENTE','APROBADO','RECHAZADO')),
    fecha                   TIMESTAMP NOT NULL DEFAULT now(),
    referencia_externa      VARCHAR(150),
    CONSTRAINT chk_pago_un_solo_concepto CHECK (
        (suscripcion_id IS NOT NULL AND orden_id IS NULL) OR
        (suscripcion_id IS NULL AND orden_id IS NOT NULL)
    )
);

CREATE INDEX idx_pago_suscripcion ON pago(suscripcion_id);
CREATE INDEX idx_pago_orden ON pago(orden_id);