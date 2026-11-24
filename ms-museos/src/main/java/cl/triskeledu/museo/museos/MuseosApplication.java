package cl.triskeledu.museo.museos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada del microservicio de museos (puerto 8082).
@SpringBootApplication
public class MuseosApplication {
    public static void main(String[] args) {
        SpringApplication.run(MuseosApplication.class, args);
    }
}
