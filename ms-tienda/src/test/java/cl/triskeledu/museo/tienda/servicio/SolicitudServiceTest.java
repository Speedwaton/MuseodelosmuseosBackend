package cl.triskeledu.museo.tienda.servicio;

import cl.triskeledu.museo.tienda.cliente.UsuariosCliente;
import cl.triskeledu.museo.tienda.cliente.UsuariosCliente.UsuarioRemoto;
import cl.triskeledu.museo.tienda.dto.Dtos.CompraRequest;
import cl.triskeledu.museo.tienda.dto.Dtos.InformacionRequest;
import cl.triskeledu.museo.tienda.dto.Dtos.ItemCompra;
import cl.triskeledu.museo.tienda.error.ApiException;
import cl.triskeledu.museo.tienda.modelo.Categoria;
import cl.triskeledu.museo.tienda.modelo.EstadoSolicitud;
import cl.triskeledu.museo.tienda.modelo.Producto;
import cl.triskeledu.museo.tienda.modelo.Solicitud;
import cl.triskeledu.museo.tienda.repositorio.ProductoRepository;
import cl.triskeledu.museo.tienda.repositorio.SolicitudRepository;
import cl.triskeledu.museo.tienda.servicio.Envio.Destino;
import cl.triskeledu.museo.tienda.servicio.Envio.Modalidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas de las compras: el repositorio y el cliente de ms-usuarios están simulados.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SolicitudServiceTest {

    @Mock
    SolicitudRepository solicitudes;
    @Mock
    ProductoRepository productos;
    @Mock
    UsuariosCliente usuarios;           // Simula la llamada HTTP a ms-usuarios

    @InjectMocks
    SolicitudService servicio;

    private final Producto luxometro = new Producto(6L, "Luxómetro para salas", Categoria.ARTICULO, 64_900, 5, 250);
    private final Producto guantes = new Producto(5L, "Guantes de nitrilo (caja 100)", Categoria.ARTICULO, 9_900, 50, 400);

    @BeforeEach
    void preparar() {
        when(usuarios.obtener(3L)).thenReturn(new UsuarioRemoto(3L, "Cliente Demo", "CLIENTE", true));
        when(productos.findById(6L)).thenReturn(Optional.of(luxometro));
        when(productos.findById(5L)).thenReturn(Optional.of(guantes));
        when(solicitudes.save(any(Solicitud.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void comprar_calculaTotalesConLosPreciosDelServidor_yDescuentaStock() {
        when(productos.descontarStock(6L, 2)).thenReturn(1);
        when(productos.descontarStock(5L, 1)).thenReturn(1);

        Solicitud s = servicio.comprar(new CompraRequest(3L,
                List.of(new ItemCompra(6L, 2), new ItemCompra(5L, 1)), Destino.INTERNACIONAL, Modalidad.EXPRESS));

        assertThat(s.getSubtotal()).isEqualTo(2 * 64_900 + 9_900);         // 139.700
        assertThat(s.getEnvio()).isEqualTo(Envio.calcular(900, 139_700, Destino.INTERNACIONAL, Modalidad.EXPRESS));
        assertThat(s.getTotal()).isEqualTo(s.getSubtotal() + s.getEnvio());
        assertThat(s.getResumen()).isEqualTo("2 × Luxómetro para salas, 1 × Guantes de nitrilo (caja 100)");
        assertThat(s.getDestino()).isEqualTo("Internacional · Express");
        assertThat(s.getCliente()).isEqualTo("Cliente Demo");             // Nombre que respondió ms-usuarios
        assertThat(s.getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
        verify(productos).descontarStock(6L, 2);
        verify(productos).descontarStock(5L, 1);
    }

    @Test
    void comprar_sinStock_responde409_yNoGuardaLaSolicitud() {
        when(productos.descontarStock(6L, 9)).thenReturn(0);              // No alcanzó el stock

        assertThatThrownBy(() -> servicio.comprar(new CompraRequest(3L, List.of(new ItemCompra(6L, 9)), Destino.NACIONAL, Modalidad.ESTANDAR)))
                .hasMessage("«Luxómetro para salas» ya no tiene stock suficiente.")
                .extracting(e -> ((ApiException) e).getEstado()).isEqualTo(HttpStatus.CONFLICT);
        verify(solicitudes, never()).save(any());
    }

    @Test
    void comprar_conCuentaDesactivada_responde403_yNoTocaElStock() {
        when(usuarios.obtener(4L)).thenReturn(new UsuarioRemoto(4L, "Lucía Demo", "CLIENTE", false));

        assertThatThrownBy(() -> servicio.comprar(new CompraRequest(4L, List.of(new ItemCompra(5L, 1)), Destino.NACIONAL, Modalidad.ESTANDAR)))
                .extracting(e -> ((ApiException) e).getEstado()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(productos, never()).descontarStock(any(), any(Integer.class));
    }

    @Test
    void comprar_siMsUsuariosNoResponde_propagaElError503() {
        when(usuarios.obtener(3L)).thenThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de usuarios no está disponible."));

        assertThatThrownBy(() -> servicio.comprar(new CompraRequest(3L, List.of(new ItemCompra(5L, 1)), Destino.NACIONAL, Modalidad.ESTANDAR)))
                .hasMessage("El servicio de usuarios no está disponible.");
    }

    @Test
    void pedirInformacion_guardaLaConsultaSinCosto() {
        Producto curso = new Producto(1L, "Introducción a la museología", Categoria.CURSO, 89_000, 30, 0);
        when(productos.findById(1L)).thenReturn(Optional.of(curso));

        Solicitud s = servicio.pedirInformacion(new InformacionRequest(3L, 1L, "  ¿Tiene certificado?  "));

        assertThat(s.getTipo()).isEqualTo("Información");
        assertThat(s.getResumen()).isEqualTo("Introducción a la museología");
        assertThat(s.getMensaje()).isEqualTo("¿Tiene certificado?");
        assertThat(s.getTotal()).isZero();
    }

    @Test
    void listar_sinUsuario_devuelveTodas_conUsuario_soloLasSuyas() {
        servicio.listar(null);
        servicio.listar(3L);

        verify(solicitudes).findAllByOrderByFechaDesc();
        verify(solicitudes).findByUsuarioIdOrderByFechaDesc(3L);
    }

    @Test
    void cambiarEstado_actualizaLaSolicitud() {
        Solicitud s = new Solicitud();
        s.setId(2L);
        when(solicitudes.findById(2L)).thenReturn(Optional.of(s));

        assertThat(servicio.cambiarEstado(2L, EstadoSolicitud.ENVIADO).getEstado()).isEqualTo(EstadoSolicitud.ENVIADO);
    }
}
