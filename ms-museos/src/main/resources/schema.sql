-- Crea la tabla de museos desde cero cada vez que inicia el microservicio.
DROP TABLE IF EXISTS museos;

CREATE TABLE museos (
    id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(150)  NOT NULL,
    pais          VARCHAR(60)   NOT NULL,
    ciudad        VARCHAR(80)   NOT NULL,
    descripcion   VARCHAR(500)  NOT NULL,
    clasificacion VARCHAR(80)   NOT NULL,
    proyecto      VARCHAR(80)   NOT NULL,
    latitud       DOUBLE        NOT NULL,
    longitud      DOUBLE        NOT NULL,
    url_visita    VARCHAR(300)  NOT NULL,
    imagen_url    VARCHAR(400),
    codigo        VARCHAR(20)   NOT NULL UNIQUE,
    estado        VARCHAR(20)   NOT NULL DEFAULT 'EN_REVISION',
    observacion   VARCHAR(500),
    autor_id      BIGINT,
    foto_ruta     VARCHAR(400)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
