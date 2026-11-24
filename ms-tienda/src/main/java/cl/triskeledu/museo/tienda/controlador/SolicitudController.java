package cl.triskeledu.museo.tienda.controlador;

import cl.triskeledu.museo.tienda.dto.Dtos.CompraRequest;
import cl.triskeledu.museo.tienda.dto.Dtos.CotizacionResponse;
import cl.triskeledu.museo.tienda.dto.Dtos.EstadoRequest;
import cl.triskeledu.museo.tienda.dto.Dtos.InformacionRequest;
import cl.triskeledu.museo.tienda.modelo.Solicitud;
import cl.triskeledu.museo.tienda.servicio.Envio;
import cl.triskeledu.museo.tienda.servicio.Envio.Destino;
import cl.triskeledu.museo.tienda.servicio.Envio.Modalidad;
import cl.triskeledu.museo.tienda.servicio.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoints REST de compras, consultas y envío: http://localhost:8083/api/...
@RestController
public class SolicitudController {
    private final SolicitudService servicio;

    public SolicitudController(SolicitudService servicio) {
        this.servicio = servicio;
    }

    // GET /api/solicitudes?usuarioId=3 -> las del cliente; sin usuarioId -> todas (Administrador).
    @GetMapping("/api/solicitudes")
    public List<Solicitud> listar(@RequestParam(required = false) Long usuarioId) {
        return servicio.listar(usuarioId);
    }

    // POST /api/solicitudes/compra -> 201 con la solicitud, 409 si falta stock.
    @PostMapping("/api/solicitudes/compra")
    @ResponseStatus(HttpStatus.CREATED)
    public Solicitud comprar(@Valid @RequestBody CompraRequest datos) {
        return servicio.comprar(datos);
    }

    // POST /api/solicitudes/informacion -> 201 con la consulta.
    @PostMapping("/api/solicitudes/informacion")
    @ResponseStatus(HttpStatus.CREATED)
    public Solicitud pedirInformacion(@Valid @RequestBody InformacionRequest datos) {
        return servicio.pedirInformacion(datos);
    }

    // PATCH /api/solicitudes/{id}/estado -> el Administrador avanza el estado.
    @PatchMapping("/api/solicitudes/{id}/estado")
    public Solicitud cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest datos) {
        return servicio.cambiarEstado(id, datos.estado());
    }

    // GET /api/envio/cotizar?gramos=1200&subtotal=50000&destino=INTERNACIONAL&modalidad=EXPRESS -> costo del envío.
    @GetMapping("/api/envio/cotizar")
    public CotizacionResponse cotizar(@RequestParam int gramos, @RequestParam int subtotal,
                                      @RequestParam Destino destino, @RequestParam Modalidad modalidad) {
        int envio = Envio.calcular(gramos, subtotal, destino, modalidad);
        return new CotizacionResponse(gramos, subtotal, destino, modalidad, envio, subtotal + envio);
    }
}
