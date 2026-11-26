package cl.triskeledu.museo.museos.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.triskeledu.museo.museos.dto.Dtos.MuseoRequest;
import cl.triskeledu.museo.museos.dto.Dtos.RevisionRequest;
import cl.triskeledu.museo.museos.modelo.Museo;
import cl.triskeledu.museo.museos.servicio.MuseoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoints REST de museos: http://localhost:8082/api/museos
@Tag(name = "Museos", description = "Fichas de museos, filtros del mapa, revisión y búsqueda por QR")
@RestController
@RequestMapping("/api/museos")
public class MuseoController {
    private final MuseoService servicio;

    public MuseoController(MuseoService servicio) {
        this.servicio = servicio;
    }

    // GET /api/museos?soloPublicados=true&pais=Chile&proyecto=... -> lista para el mapa.
    @Operation(summary = "Listar museos (filtros opcionales: soloPublicados, pais, proyecto)")
    @GetMapping
    public List<Museo> listar(@RequestParam(defaultValue = "false") boolean soloPublicados,
                              @RequestParam(required = false) String pais,
                              @RequestParam(required = false) String proyecto) {
        return servicio.listar(soloPublicados, pais, proyecto);
    }

    // GET /api/museos/{id} -> 200 o 404.
    @Operation(summary = "Obtener una ficha por su id")
    @GetMapping("/{id}")
    public Museo obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    // GET /api/museos/codigo/{codigo} -> la ficha que corresponde a un QR, o 404.
    @Operation(summary = "Buscar la ficha de un código QR")
    @GetMapping("/codigo/{codigo}")
    public Museo porCodigo(@PathVariable String codigo) {
        return servicio.porCodigo(codigo);
    }

    // POST /api/museos -> 201 con la ficha nueva, 409 si el código QR ya existe.
    @Operation(summary = "Crear una ficha (publicada si la crea el Administrador, en revisión si la crea el Investigador)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Museo crear(@Valid @RequestBody MuseoRequest datos) {
        return servicio.crear(datos);
    }

    // PUT /api/museos/{id} -> corrige la ficha.
    @Operation(summary = "Corregir una ficha")
    @PutMapping("/{id}")
    public Museo actualizar(@PathVariable Long id, @Valid @RequestBody MuseoRequest datos) {
        return servicio.actualizar(id, datos);
    }

    // PATCH /api/museos/{id}/revision -> el Administrador publica u observa.
    @Operation(summary = "Publicar u observar una ficha (Administrador)")
    @PatchMapping("/{id}/revision")
    public Museo revisar(@PathVariable Long id, @Valid @RequestBody RevisionRequest datos) {
        return servicio.revisar(id, datos);
    }

    // DELETE /api/museos/{id} -> 204 sin contenido.
    @Operation(summary = "Eliminar una ficha")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
