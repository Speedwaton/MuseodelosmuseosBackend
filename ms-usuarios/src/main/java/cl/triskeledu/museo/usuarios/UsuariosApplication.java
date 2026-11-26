package cl.triskeledu.museo.usuarios;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio de usuarios (puerto 8081).
// Título y descripción que muestra Swagger en http://localhost:<puerto>/swagger-ui.html
@OpenAPIDefinition(info = @Info(title = "ms-usuarios", version = "1.0.0",
        description = "Microservicio de usuarios de Museo de los Museos: registro, inicio de sesión (PBKDF2), perfiles y cuentas. Puerto 8081."))
@SpringBootApplication
public class UsuariosApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsuariosApplication.class, args);
    }
}
