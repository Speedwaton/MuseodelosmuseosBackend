# Museo de los Museos — Backend (Spring Boot + microservicios)

API REST de la app **Museo de los Museos**, dividida en 3 microservicios independientes. Cada uno tiene su
propia base de datos MySQL, que se **crea y se puebla con un script** (`schema.sql` + `data.sql`) cada vez
que el servicio inicia.

| Microservicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| `ms-usuarios` | 8081 | `museo_usuarios` | Registro, inicio de sesión (contraseñas con PBKDF2), perfiles y cuentas |
| `ms-museos` | 8082 | `museo_museos` | Fichas de museos, filtros del mapa, revisión (publicar/observar) y búsqueda por QR |
| `ms-tienda` | 8083 | `museo_tienda` | Catálogo, compras con cálculo de envío, stock y solicitudes |

`ms-tienda` se comunica con `ms-usuarios` (`RestClient`) para comprobar que el cliente existe y está activo
antes de registrar una compra, y guarda su nombre en la solicitud.

## Tecnología

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.14 (Web, Data JPA, Validation) |
| MySQL | 8.4 (Laragon), conector `mysql-connector-j` |
| Maven | Wrapper incluido (`mvnw.cmd`) |
| Pruebas | JUnit 5, Mockito, AssertJ, MockMvc (`spring-boot-starter-test`) |

## Estructura de cada microservicio

```
ms-xxx/src/main/java/cl/triskeledu/museo/xxx/
├── XxxApplication.java   Punto de entrada
├── modelo/               Entidades JPA (tablas)
├── repositorio/          Interfaces JpaRepository (consultas)
├── servicio/             Reglas de negocio
├── controlador/          Endpoints REST (@RestController)
├── dto/                  Datos que entran a la API (con validaciones)
└── error/                Errores en JSON: {"error": "mensaje"}
ms-xxx/src/main/resources/
├── application.properties  Puerto y conexión a MySQL
├── schema.sql              Crea las tablas
└── data.sql                Carga los datos de demostración
```

## Cómo ejecutarlo

1. Abre **Laragon** y presiona **Iniciar todo** (o solo MySQL). Usuario `root`, sin contraseña.
2. Desde esta carpeta, inicia cada microservicio en su propia terminal:

```bash
mvnw.cmd -pl ms-usuarios spring-boot:run
```

```bash
mvnw.cmd -pl ms-museos spring-boot:run
```

```bash
mvnw.cmd -pl ms-tienda spring-boot:run
```

3. Comprueba en el navegador: <http://localhost:8082/api/museos>

> Las bases `museo_usuarios`, `museo_museos` y `museo_tienda` se crean solas. Como `schema.sql` borra y vuelve
> a crear las tablas, **cada reinicio deja los datos de demostración originales**.

También se pueden abrir en IntelliJ IDEA (abrir `pom.xml` como proyecto) y ejecutar cada `*Application`.

## Pruebas unitarias

```bash
mvnw.cmd test
```

| Archivo | Qué verifica |
|---|---|
| `ms-usuarios/.../SeguridadTest` | Cifrado PBKDF2: no guarda la clave real, sal distinta y compatibilidad con `data.sql` |
| `ms-usuarios/.../UsuarioServiceTest` | Login (correcto, clave mala, cuenta inactiva, rol equivocado), registro, perfil y activar cuentas |
| `ms-usuarios/.../UsuarioControllerTest` | Códigos HTTP (200, 400, 401, 404) y que la contraseña nunca salga en el JSON |
| `ms-museos/.../MuseoServiceTest` | Filtros del mapa, estado según el rol, código QR repetido (409), permisos y revisión |
| `ms-museos/.../MuseoControllerTest` | Rutas, validaciones (400), 201 al crear y 204 al eliminar |
| `ms-tienda/.../EnvioTest` | Cálculo de envío (pruebas parametrizadas) y envío gratis desde $80.000 |
| `ms-tienda/.../SolicitudServiceTest` | Compra con totales del servidor, falta de stock (409), cuenta inactiva (403) y ms-usuarios caído (503) |
| `ms-tienda/.../ProductoServiceTest` | Catálogo, productos desactivados y códigos repetidos |
| `ms-tienda/.../SolicitudControllerTest` | Cotizador de envío, compra (201/400/409) y validación de consultas |

Total: **61 pruebas**. Usan repositorios simulados con Mockito, así que no necesitan MySQL encendido.

## Endpoints

### ms-usuarios — `http://localhost:8081`

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| POST | `/api/usuarios/login` | `correo`, `clave`, `rol` | 200 usuario · 401 datos incorrectos · 403 inactivo o rol distinto |
| POST | `/api/usuarios/registro` | `nombre`, `correo`, `clave`, `avatar` | 201 cuenta de Cliente · 400 validación · 409 correo repetido |
| GET | `/api/usuarios` | | 200 todas las cuentas |
| GET | `/api/usuarios/{id}` | | 200 · 404 |
| PUT | `/api/usuarios/{id}` | `nombre`, `avatar`, `notificaciones`, `textoGrande`, `idioma` | 200 perfil actualizado |
| PATCH | `/api/usuarios/{id}/activo` | `activo` | 200 cuenta activada o desactivada |

### ms-museos — `http://localhost:8082`

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| GET | `/api/museos?soloPublicados=&pais=&proyecto=` | | 200 lista (filtros opcionales) |
| GET | `/api/museos/{id}` | | 200 · 404 |
| GET | `/api/museos/codigo/{codigo}` | | 200 ficha del QR · 404 |
| POST | `/api/museos` | datos de la ficha + `autorId`, `rolAutor` | 201 (Administrador: publicada; Investigador: en revisión) · 409 código repetido |
| PUT | `/api/museos/{id}` | igual que POST | 200 · 403 si el Investigador no es el autor |
| PATCH | `/api/museos/{id}/revision` | `estado`, `observacion` | 200 · 400 si se observa sin motivo |
| DELETE | `/api/museos/{id}` | | 204 |

### ms-tienda — `http://localhost:8083`

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| GET | `/api/productos?soloActivos=` | | 200 catálogo |
| GET | `/api/productos/{id}` | | 200 · 404 |
| GET | `/api/productos/codigo/{codigo}` | | 200 producto del QR · 404 |
| POST | `/api/productos` | datos del producto | 201 · 409 código repetido |
| PUT | `/api/productos/{id}` | datos del producto | 200 |
| PATCH | `/api/productos/{id}/activo` | `activo` | 200 |
| GET | `/api/solicitudes?usuarioId=` | | 200 (sin `usuarioId`: todas) |
| POST | `/api/solicitudes/compra` | `usuarioId`, `items[]`, `destino`, `modalidad` | 201 · 403 cuenta inactiva · 409 sin stock · 503 ms-usuarios caído |
| POST | `/api/solicitudes/informacion` | `usuarioId`, `productoId`, `mensaje` | 201 |
| PATCH | `/api/solicitudes/{id}/estado` | `estado` | 200 |
| GET | `/api/envio/cotizar?gramos=&subtotal=&destino=&modalidad=` | | 200 costo del envío y total |

Los errores siempre responden `{"error": "mensaje"}`, que la app muestra tal cual.

## Cuentas de demostración

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@museo.demo | Admin#2026 |
| Investigador | investigador@museo.demo | Investiga#2026 |
| Cliente | cliente@museo.demo | Cliente#2026 |
| Cliente | lucia@museo.demo | Cliente#2026 |

## Seguridad

Es un backend de desarrollo: no tiene autenticación por token y los servicios escuchan en todas las
interfaces de red. Las contraseñas sí se guardan cifradas (PBKDF2 con sal) y nunca se devuelven en las respuestas.
