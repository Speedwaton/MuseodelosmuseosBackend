package cl.triskeledu.museo.tienda.controlador;

import cl.triskeledu.museo.tienda.error.ApiException;
import cl.triskeledu.museo.tienda.modelo.Solicitud;
import cl.triskeledu.museo.tienda.servicio.SolicitudService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas del controlador de compras y del cotizador de envío.
@WebMvcTest(SolicitudController.class)
class SolicitudControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    SolicitudService servicio;

    @Test
    void cotizar_devuelveEnvioYTotal() throws Exception {
        mvc.perform(get("/api/envio/cotizar").param("gramos", "500").param("subtotal", "129800")
                        .param("destino", "INTERNACIONAL").param("modalidad", "EXPRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.envio").value(32800))
                .andExpect(jsonPath("$.total").value(162600));
    }

    @Test
    void comprar_correcto_responde201() throws Exception {
        Solicitud s = new Solicitud();
        s.setId(2L);
        s.setTotal(162_600);
        when(servicio.comprar(any())).thenReturn(s);

        mvc.perform(post("/api/solicitudes/compra").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"items\":[{\"productoId\":6,\"cantidad\":2}],\"destino\":\"INTERNACIONAL\",\"modalidad\":\"EXPRESS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.total").value(162600));
    }

    @Test
    void comprar_carritoVacio_responde400() throws Exception {
        mvc.perform(post("/api/solicitudes/compra").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"items\":[],\"destino\":\"NACIONAL\",\"modalidad\":\"ESTANDAR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El carrito está vacío."));
        verify(servicio, never()).comprar(any());
    }

    @Test
    void comprar_sinStock_responde409() throws Exception {
        when(servicio.comprar(any())).thenThrow(new ApiException(HttpStatus.CONFLICT, "«Papel tissue libre de ácido» ya no tiene stock suficiente."));

        mvc.perform(post("/api/solicitudes/compra").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"items\":[{\"productoId\":8,\"cantidad\":1}],\"destino\":\"NACIONAL\",\"modalidad\":\"ESTANDAR\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("«Papel tissue libre de ácido» ya no tiene stock suficiente."));
    }

    @Test
    void pedirInformacion_mensajeCorto_responde400() throws Exception {
        mvc.perform(post("/api/solicitudes/informacion").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":3,\"productoId\":1,\"mensaje\":\"hola\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Escribe tu consulta (mínimo 10 caracteres)."));
    }
}
