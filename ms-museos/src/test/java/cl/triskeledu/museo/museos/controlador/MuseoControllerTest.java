package cl.triskeledu.museo.museos.controlador;

import cl.triskeledu.museo.museos.error.ApiException;
import cl.triskeledu.museo.museos.modelo.EstadoFicha;
import cl.triskeledu.museo.museos.modelo.Museo;
import cl.triskeledu.museo.museos.servicio.MuseoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas del controlador de museos con MockMvc y el servicio simulado.
@WebMvcTest(MuseoController.class)
class MuseoControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    MuseoService servicio;

    private static Museo prado() {
        Museo m = new Museo();
        m.setId(4L);
        m.setNombre("Museo Nacional del Prado");
        m.setPais("España");
        m.setCodigo("MUS-004");
        m.setEstado(EstadoFicha.PUBLICADO);
        return m;
    }

    @Test
    void listar_pasaLosFiltrosAlServicio() throws Exception {
        when(servicio.listar(true, "España", null)).thenReturn(List.of(prado()));

        mvc.perform(get("/api/museos").param("soloPublicados", "true").param("pais", "España"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Museo Nacional del Prado"))
                .andExpect(jsonPath("$[0].estado").value("PUBLICADO"));
    }

    @Test
    void porCodigo_devuelveLaFichaDelQr() throws Exception {
        when(servicio.porCodigo("MUS-004")).thenReturn(prado());

        mvc.perform(get("/api/museos/codigo/MUS-004"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void obtener_inexistente_responde404() throws Exception {
        when(servicio.obtener(99L)).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "No existe el museo 99."));

        mvc.perform(get("/api/museos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe el museo 99."));
    }

    @Test
    void crear_conUrlInvalida_responde400_sinLlamarAlServicio() throws Exception {
        String json = """
                {"nombre":"Museo X","pais":"Chile","ciudad":"Arica","descripcion":"Descripción suficientemente larga",
                 "clasificacion":"Historia","proyecto":"Historia y Sociedad","latitud":-18.4,"longitud":-70.3,
                 "urlVisita":"museo-x","codigo":"MUS-050","autorId":2,"rolAutor":"INVESTIGADOR"}""";

        mvc.perform(post("/api/museos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El enlace debe comenzar con https://"));
        verify(servicio, never()).crear(any());
    }

    @Test
    void crear_correcto_responde201() throws Exception {
        when(servicio.crear(any())).thenReturn(prado());
        String json = """
                {"nombre":"Museo X","pais":"Chile","ciudad":"Arica","descripcion":"Descripción suficientemente larga",
                 "clasificacion":"Historia","proyecto":"Historia y Sociedad","latitud":-18.4,"longitud":-70.3,
                 "urlVisita":"https://museo.cl/","codigo":"MUS-050","autorId":2,"rolAutor":"INVESTIGADOR"}""";

        mvc.perform(post("/api/museos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void eliminar_responde204() throws Exception {
        mvc.perform(delete("/api/museos/4")).andExpect(status().isNoContent());
        verify(servicio).eliminar(4L);
    }
}
