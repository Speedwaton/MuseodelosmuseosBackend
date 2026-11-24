package cl.triskeledu.museo.usuarios.error;

import org.springframework.http.HttpStatus;

// Error de negocio con su código HTTP (por ejemplo 404 si no existe, 409 si el correo ya está usado).
public class ApiException extends RuntimeException {
    private final HttpStatus estado;

    public ApiException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
