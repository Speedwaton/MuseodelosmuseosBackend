package cl.triskeledu.museo.museos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio de museos (puerto 8082).
// Título y descripción que muestra Swagger en http://localhost:<puerto>/swagger-ui.html
@OpenAPIDefinition(info = @Info(title = "ms-museos", version = "1.0.0",
        description = "Microservicio de museos de Museo de los Museos: fichas, mapa, revisión y búsqueda por código QR. Puerto 8082."))
@SpringBootApplication
public class MuseosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MuseosApplication.class, args);
    }
}
