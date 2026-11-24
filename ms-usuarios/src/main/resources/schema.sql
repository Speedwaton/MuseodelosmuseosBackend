-- Crea la tabla de usuarios desde cero cada vez que inicia el microservicio.
DROP TABLE IF EXISTS usuarios;

CREATE TABLE usuarios (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    correo         VARCHAR(120) NOT NULL UNIQUE,
    clave_hash     VARCHAR(200) NOT NULL,
    rol            VARCHAR(20)  NOT NULL,
    avatar         VARCHAR(16)  NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    notificaciones BOOLEAN      NOT NULL DEFAULT TRUE,
    texto_grande   BOOLEAN      NOT NULL DEFAULT FALSE,
    idioma         VARCHAR(5)   NOT NULL DEFAULT 'es'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
