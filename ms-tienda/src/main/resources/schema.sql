-- Crea las tablas de la tienda desde cero cada vez que inicia el microservicio.
DROP TABLE IF EXISTS solicitudes;
DROP TABLE IF EXISTS productos;

CREATE TABLE productos (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    categoria   VARCHAR(20)  NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    precio      INT          NOT NULL,
    stock       INT          NOT NULL,
    peso_gramos INT          NOT NULL DEFAULT 0,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    codigo      VARCHAR(20)  NOT NULL UNIQUE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- usuario_id apunta a la tabla usuarios de ms-usuarios (otra base de datos), por eso no es clave foránea.
CREATE TABLE solicitudes (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT       NOT NULL,
    cliente    VARCHAR(100) NOT NULL,
    tipo       VARCHAR(20)  NOT NULL,
    resumen    VARCHAR(500) NOT NULL,
    mensaje    VARCHAR(500) NOT NULL DEFAULT '',
    subtotal   INT          NOT NULL DEFAULT 0,
    envio      INT          NOT NULL DEFAULT 0,
    total      INT          NOT NULL DEFAULT 0,
    destino    VARCHAR(80)  NOT NULL DEFAULT '',
    estado     VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha      BIGINT       NOT NULL,
    INDEX idx_solicitudes_usuario (usuario_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
