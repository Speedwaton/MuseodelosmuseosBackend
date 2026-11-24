package cl.triskeledu.museo.tienda.controlador;

import cl.triskeledu.museo.tienda.dto.Dtos.ActivoRequest;
import cl.triskeledu.museo.tienda.dto.Dtos.ProductoRequest;
import cl.triskeledu.museo.tienda.modelo.Producto;
import cl.triskeledu.museo.tienda.servicio.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

// Endpoints REST del catálogo: http://localhost:8083/api/productos
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    // GET /api/productos?soloActivos=true -> catálogo.
    @GetMapping
    public List<Producto> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return servicio.listar(soloActivos);
    }

    // GET /api/productos/{id} -> 200 o 404.
    @GetMapping("/{id}")
    public Producto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    // GET /api/productos/codigo/{codigo} -> el producto de un QR, o 404.
    @GetMapping("/codigo/{codigo}")
    public Producto porCodigo(@PathVariable String codigo) {
        return servicio.porCodigo(codigo);
    }

    // POST /api/productos -> 201 con el producto nuevo.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@Valid @RequestBody ProductoRequest datos) {
        return servicio.crear(datos);
    }

    // PUT /api/productos/{id} -> edita el producto.
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest datos) {
        return servicio.actualizar(id, datos);
    }

    // PATCH /api/productos/{id}/activo -> lo activa o desactiva.
    @PatchMapping("/{id}/activo")
    public Producto cambiarActivo(@PathVariable Long id, @RequestBody ActivoRequest datos) {
        return servicio.cambiarActivo(id, datos.activo());
    }
}
