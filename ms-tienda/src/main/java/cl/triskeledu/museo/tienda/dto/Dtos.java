package cl.triskeledu.museo.tienda.dto;

import cl.triskeledu.museo.tienda.modelo.Categoria;
import cl.triskeledu.museo.tienda.modelo.EstadoSolicitud;
import cl.triskeledu.museo.tienda.servicio.Envio.Destino;
import cl.triskeledu.museo.tienda.servicio.Envio.Modalidad;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

// Datos que entran a la API de la tienda. Las respuestas son las entidades Producto y Solicitud (JSON).
public final class Dtos {
    private Dtos() {
    }

    // POST /api/productos y PUT /api/productos/{id}
    public record ProductoRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(min = 3, message = "Nombre: escribe al menos 3 caracteres") String nombre,
            @NotNull(message = "Elige una categoría") Categoria categoria,
            @NotBlank(message = "La descripción es obligatoria") @Size(min = 10, message = "Descripción: escribe al menos 10 caracteres") String descripcion,
            @Min(value = 0, message = "El precio no puede ser negativo") int precio,
            @Min(value = 0, message = "El stock no puede ser negativo") int stock,
            @Min(value = 0, message = "El peso no puede ser negativo") int pesoGramos,
            boolean activo,
            @Pattern(regexp = "^[A-Za-z0-9-]{3,20}$", message = "Código QR: 3 a 20 letras, números o guiones") String codigo) {
    }

    // PATCH /api/productos/{id}/activo
    public record ActivoRequest(boolean activo) {
    }

    // Una línea del carrito que se compra.
    public record ItemCompra(@NotNull Long productoId, @Min(value = 1, message = "La cantidad debe ser al menos 1") int cantidad) {
    }

    // POST /api/solicitudes/compra
    public record CompraRequest(
            @NotNull(message = "Falta el cliente") Long usuarioId,
            @NotEmpty(message = "El carrito está vacío.") List<@Valid ItemCompra> items,
            @NotNull(message = "Elige el destino") Destino destino,
            @NotNull(message = "Elige la modalidad") Modalidad modalidad) {
    }

    // POST /api/solicitudes/informacion (consulta sobre un curso)
    public record InformacionRequest(
            @NotNull(message = "Falta el cliente") Long usuarioId,
            @NotNull(message = "Falta el producto") Long productoId,
            @NotBlank(message = "Escribe tu consulta") @Size(min = 10, message = "Escribe tu consulta (mínimo 10 caracteres).") String mensaje) {
    }

    // PATCH /api/solicitudes/{id}/estado
    public record EstadoRequest(@NotNull(message = "Falta el estado") EstadoSolicitud estado) {
    }

    // GET /api/envio/cotizar
    public record CotizacionResponse(int gramos, int subtotal, Destino destino, Modalidad modalidad, int envio, int total) {
    }
}
