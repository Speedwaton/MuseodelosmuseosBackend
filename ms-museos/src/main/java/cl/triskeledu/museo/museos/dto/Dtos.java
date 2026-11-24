package cl.triskeledu.museo.museos.dto;

import cl.triskeledu.museo.museos.modelo.EstadoFicha;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Datos que entran a la API de museos. La respuesta es la entidad Museo tal cual (JSON).
public final class Dtos {
    private Dtos() {
    }

    // Quién crea o corrige la ficha: el Administrador la publica directo; el Investigador la deja en revisión.
    public enum RolAutor { ADMINISTRADOR, INVESTIGADOR }

    // POST /api/museos y PUT /api/museos/{id}
    public record MuseoRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(min = 3, message = "Nombre: escribe al menos 3 caracteres") String nombre,
            @NotBlank(message = "El país es obligatorio") String pais,
            @NotBlank(message = "La ciudad es obligatoria") String ciudad,
            @NotBlank(message = "La descripción es obligatoria") @Size(min = 10, message = "Descripción: escribe al menos 10 caracteres") String descripcion,
            @NotBlank(message = "La clasificación es obligatoria") String clasificacion,
            @NotBlank(message = "El proyecto es obligatorio") String proyecto,
            @DecimalMin(value = "-90", message = "Latitud fuera de rango") @DecimalMax(value = "90", message = "Latitud fuera de rango") double latitud,
            @DecimalMin(value = "-180", message = "Longitud fuera de rango") @DecimalMax(value = "180", message = "Longitud fuera de rango") double longitud,
            @Pattern(regexp = "^https?://\\S+\\.\\S+$", message = "El enlace debe comenzar con https://") String urlVisita,
            String imagenUrl,
            @Pattern(regexp = "^[A-Za-z0-9-]{3,20}$", message = "Código QR: 3 a 20 letras, números o guiones") String codigo,
            String fotoRuta,
            @NotNull(message = "Falta el autor") Long autorId,
            @NotNull(message = "Falta el rol del autor") RolAutor rolAutor) {
    }

    // PATCH /api/museos/{id}/revision: el Administrador publica u observa.
    public record RevisionRequest(@NotNull(message = "Falta el estado") EstadoFicha estado, String observacion) {
    }
}
