package cl.triskeledu.museo.usuarios.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// Convierte los errores en respuestas JSON simples: {"error": "mensaje"}.
@RestControllerAdvice
public class ManejadorErrores {

    // Errores de negocio: usan el código que trae la excepción.
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> negocio(ApiException e) {
        return ResponseEntity.status(e.getEstado()).body(Map.of("error", e.getMessage()));
    }

    // Validaciones (@NotBlank, @Size...): 400 con el primer mensaje.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage()).findFirst().orElse("Datos no válidos");
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }

    // JSON mal escrito o con valores que no existen (por ejemplo un rol desconocido): 400.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "El JSON enviado no es válido"));
    }
}
