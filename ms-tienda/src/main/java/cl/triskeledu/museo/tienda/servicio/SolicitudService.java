package cl.triskeledu.museo.tienda.servicio;

import cl.triskeledu.museo.tienda.cliente.UsuariosCliente;
import cl.triskeledu.museo.tienda.cliente.UsuariosCliente.UsuarioRemoto;
import cl.triskeledu.museo.tienda.dto.Dtos.CompraRequest;
import cl.triskeledu.museo.tienda.dto.Dtos.InformacionRequest;
import cl.triskeledu.museo.tienda.dto.Dtos.ItemCompra;
import cl.triskeledu.museo.tienda.error.ApiException;
import cl.triskeledu.museo.tienda.modelo.EstadoSolicitud;
import cl.triskeledu.museo.tienda.modelo.Producto;
import cl.triskeledu.museo.tienda.modelo.Solicitud;
import cl.triskeledu.museo.tienda.repositorio.ProductoRepository;
import cl.triskeledu.museo.tienda.repositorio.SolicitudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// Reglas de negocio de las compras y consultas.
@Service
public class SolicitudService {
    private final SolicitudRepository solicitudes;
    private final ProductoRepository productos;
    private final UsuariosCliente usuarios;

    public SolicitudService(SolicitudRepository solicitudes, ProductoRepository productos, UsuariosCliente usuarios) {
        this.solicitudes = solicitudes;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    // El Cliente ve las suyas (usuarioId); el Administrador todas (usuarioId = null).
    public List<Solicitud> listar(Long usuarioId) {
        return usuarioId == null ? solicitudes.findAllByOrderByFechaDesc() : solicitudes.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }

    // Compra: revisa el cliente, calcula precios y envío con los datos del servidor, descuenta el stock y crea la solicitud.
    // Si algo falla (por ejemplo, falta stock de un producto), @Transactional deshace todo.
    @Transactional
    public Solicitud comprar(CompraRequest datos) {
        UsuarioRemoto cliente = clienteActivo(datos.usuarioId());
        int subtotal = 0;
        int gramos = 0;
        List<String> lineas = new ArrayList<>();
        for (ItemCompra item : datos.items()) {
            Producto p = productos.findById(item.productoId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe el producto " + item.productoId() + "."));
            if (productos.descontarStock(p.getId(), item.cantidad()) == 0) {
                throw new ApiException(HttpStatus.CONFLICT, "«" + p.getNombre() + "» ya no tiene stock suficiente.");
            }
            subtotal += p.getPrecio() * item.cantidad();
            gramos += p.getPesoGramos() * item.cantidad();
            lineas.add(item.cantidad() + " × " + p.getNombre());
        }
        int envio = Envio.calcular(gramos, subtotal, datos.destino(), datos.modalidad());

        Solicitud s = nueva(cliente, "Compra", String.join(", ", lineas));
        s.setSubtotal(subtotal);
        s.setEnvio(envio);
        s.setTotal(subtotal + envio);
        s.setDestino(datos.destino().etiqueta + " · " + datos.modalidad().etiqueta);
        return solicitudes.save(s);
    }

    // Consulta sobre un curso: no descuenta stock ni tiene costo.
    public Solicitud pedirInformacion(InformacionRequest datos) {
        UsuarioRemoto cliente = clienteActivo(datos.usuarioId());
        Producto p = productos.findById(datos.productoId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe el producto " + datos.productoId() + "."));
        Solicitud s = nueva(cliente, "Información", p.getNombre());
        s.setMensaje(datos.mensaje().trim());
        return solicitudes.save(s);
    }

    // El Administrador cambia el estado (Pendiente, Confirmado, Enviado o Entregado).
    public Solicitud cambiarEstado(Long id, EstadoSolicitud estado) {
        Solicitud s = solicitudes.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe la solicitud " + id + "."));
        s.setEstado(estado);
        return solicitudes.save(s);
    }

    // Le pregunta a ms-usuarios si el cliente existe y si su cuenta está activa.
    private UsuarioRemoto clienteActivo(Long usuarioId) {
        UsuarioRemoto u = usuarios.obtener(usuarioId);
        if (!u.activo()) throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta de " + u.nombre() + " está desactivada.");
        return u;
    }

    private static Solicitud nueva(UsuarioRemoto cliente, String tipo, String resumen) {
        Solicitud s = new Solicitud();
        s.setUsuarioId(cliente.id());
        s.setCliente(cliente.nombre());
        s.setTipo(tipo);
        s.setResumen(resumen);
        s.setFecha(System.currentTimeMillis());
        return s;
    }
}
