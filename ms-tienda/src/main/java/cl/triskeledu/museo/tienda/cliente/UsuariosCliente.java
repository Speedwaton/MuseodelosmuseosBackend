package cl.triskeledu.museo.tienda.cliente;

import cl.triskeledu.museo.tienda.error.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

// Comunicación entre microservicios: ms-tienda le pregunta a ms-usuarios por un cliente antes de registrar su solicitud.
@Component
public class UsuariosCliente {

    // Lo que ms-tienda necesita saber de un usuario (ms-usuarios responde más campos, pero se ignoran).
    public record UsuarioRemoto(Long id, String nombre, String rol, boolean activo) {
    }

    private final RestClient http;

    public UsuariosCliente(@Value("${usuarios.url:http://localhost:8081}") String url) {
        this.http = RestClient.create(url);
    }

    // GET http://localhost:8081/api/usuarios/{id}
    public UsuarioRemoto obtener(Long id) {
        try {
            return http.get().uri("/api/usuarios/{id}", id).retrieve().body(UsuarioRemoto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No existe el usuario " + id + ".");
        } catch (ResourceAccessException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de usuarios no está disponible.");
        }
    }
}
