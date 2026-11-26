package cl.triskeledu.museo.tienda;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio de tienda (puerto 8083).
// Título y descripción que muestra Swagger en http://localhost:<puerto>/swagger-ui.html
@OpenAPIDefinition(info = @Info(title = "ms-tienda", version = "1.0.0",
        description = "Microservicio de tienda de Museo de los Museos: catálogo, compras con cálculo de envío y solicitudes. Puerto 8083."))
@SpringBootApplication
public class TiendaApplication {
    public static void main(String[] args) {
        SpringApplication.run(TiendaApplication.class, args);
    }
}
