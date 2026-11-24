package cl.triskeledu.museo.museos.servicio;

import cl.triskeledu.museo.museos.dto.Dtos.MuseoRequest;
import cl.triskeledu.museo.museos.dto.Dtos.RevisionRequest;
import cl.triskeledu.museo.museos.dto.Dtos.RolAutor;
import cl.triskeledu.museo.museos.error.ApiException;
import cl.triskeledu.museo.museos.modelo.EstadoFicha;
import cl.triskeledu.museo.museos.modelo.Museo;
import cl.triskeledu.museo.museos.repositorio.MuseoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias de MuseoService con el repositorio simulado.
@ExtendWith(MockitoExtension.class)
class MuseoServiceTest {

    @Mock
    MuseoRepository repositorio;

    @InjectMocks
    MuseoService servicio;

    private static Museo museo(long id, String nombre, String pais, String proyecto, EstadoFicha estado, Long autor) {
        Museo m = new Museo();
        m.setId(id);
        m.setNombre(nombre);
        m.setPais(pais);
        m.setProyecto(proyecto);
        m.setEstado(estado);
        m.setAutorId(autor);
        m.setCodigo("MUS-%03d".formatted(id));
        return m;
    }

    private static MuseoRequest pedido(String codigo, long autor, RolAutor rol) {
        return new MuseoRequest("Museo de Valparaíso", "Chile", "Valparaíso", "Patrimonio del puerto de Valparaíso.",
                "Historia", "Historia y Sociedad", -33.04, -71.61, "https://www.mhnv.gob.cl/", "", codigo, null, autor, rol);
    }

    @Test
    void listar_filtraPorPaisYProyecto_sinImportarMayusculas() {
        when(repositorio.findAllByOrderByPaisAscNombreAsc()).thenReturn(List.of(
                museo(1, "Bellas Artes", "Chile", "Arte Iberoamericano", EstadoFicha.PUBLICADO, 2L),
                museo(2, "Precolombino", "Chile", "Patrimonio Precolombino", EstadoFicha.PUBLICADO, 2L),
                museo(4, "Prado", "España", "Arte Iberoamericano", EstadoFicha.PUBLICADO, 2L)));

        List<Museo> lista = servicio.listar(false, "chile", "arte iberoamericano");

        assertThat(lista).extracting(Museo::getNombre).containsExactly("Bellas Artes");
    }

    @Test
    void listar_soloPublicados_consultaPorEstado() {
        when(repositorio.findByEstadoOrderByPaisAscNombreAsc(EstadoFicha.PUBLICADO)).thenReturn(List.of());

        servicio.listar(true, null, null);

        verify(repositorio).findByEstadoOrderByPaisAscNombreAsc(EstadoFicha.PUBLICADO);
        verify(repositorio, never()).findAllByOrderByPaisAscNombreAsc();
    }

    @Test
    void crear_porInvestigador_quedaEnRevision_yCodigoEnMayusculas() {
        when(repositorio.findByCodigo("MUS-013")).thenReturn(Optional.empty());
        when(repositorio.save(any(Museo.class))).thenAnswer(i -> i.getArgument(0));

        Museo m = servicio.crear(pedido("mus-013", 2L, RolAutor.INVESTIGADOR));

        assertThat(m.getEstado()).isEqualTo(EstadoFicha.EN_REVISION);
        assertThat(m.getCodigo()).isEqualTo("MUS-013");
        assertThat(m.getAutorId()).isEqualTo(2L);
        assertThat(m.getImagenUrl()).isNull();   // URL vacía se guarda como null
    }

    @Test
    void crear_porAdministrador_quedaPublicada() {
        when(repositorio.findByCodigo("MUS-013")).thenReturn(Optional.empty());
        when(repositorio.save(any(Museo.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio.crear(pedido("MUS-013", 1L, RolAutor.ADMINISTRADOR)).getEstado()).isEqualTo(EstadoFicha.PUBLICADO);
    }

    @Test
    void crear_conCodigoRepetido_responde409() {
        when(repositorio.findByCodigo("MUS-004")).thenReturn(Optional.of(museo(4, "Prado", "España", "Arte", EstadoFicha.PUBLICADO, 2L)));

        assertThatThrownBy(() -> servicio.crear(pedido("MUS-004", 2L, RolAutor.INVESTIGADOR)))
                .hasMessage("Ese código ya es de «Prado».")
                .extracting(e -> ((ApiException) e).getEstado()).isEqualTo(HttpStatus.CONFLICT);
        verify(repositorio, never()).save(any());
    }

    @Test
    void actualizar_fichaAjena_porInvestigador_responde403() {
        when(repositorio.findById(5L)).thenReturn(Optional.of(museo(5, "Reina Sofía", "España", "Arte", EstadoFicha.PUBLICADO, 7L)));

        assertThatThrownBy(() -> servicio.actualizar(5L, pedido("MUS-005", 2L, RolAutor.INVESTIGADOR)))
                .hasMessage("Solo puedes corregir tus fichas.");
    }

    @Test
    void actualizar_fichaPropia_porInvestigador_vuelveARevision() {
        Museo observada = museo(13, "Valparaíso", "Chile", "Historia", EstadoFicha.OBSERVADO, 2L);
        observada.setObservacion("Falta la foto");
        when(repositorio.findById(13L)).thenReturn(Optional.of(observada));
        when(repositorio.findByCodigo("MUS-013")).thenReturn(Optional.of(observada));   // Su propio código: no es conflicto
        when(repositorio.save(any(Museo.class))).thenAnswer(i -> i.getArgument(0));

        Museo m = servicio.actualizar(13L, pedido("MUS-013", 2L, RolAutor.INVESTIGADOR));

        assertThat(m.getEstado()).isEqualTo(EstadoFicha.EN_REVISION);
        assertThat(m.getObservacion()).isNull();
    }

    @Test
    void revisar_publica_laFicha() {
        when(repositorio.findById(12L)).thenReturn(Optional.of(museo(12, "Memoria", "Chile", "Historia", EstadoFicha.EN_REVISION, 2L)));
        when(repositorio.save(any(Museo.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio.revisar(12L, new RevisionRequest(EstadoFicha.PUBLICADO, " ")).getEstado()).isEqualTo(EstadoFicha.PUBLICADO);
    }

    @Test
    void revisar_observarSinMotivo_responde400() {
        when(repositorio.findById(12L)).thenReturn(Optional.of(museo(12, "Memoria", "Chile", "Historia", EstadoFicha.EN_REVISION, 2L)));

        assertThatThrownBy(() -> servicio.revisar(12L, new RevisionRequest(EstadoFicha.OBSERVADO, null)))
                .extracting(e -> ((ApiException) e).getEstado()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void porCodigo_inexistente_responde404ConMensaje() {
        when(repositorio.findByCodigo("MUS-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.porCodigo("mus-999"))
                .hasMessage("El código mus-999 no corresponde a ninguna ficha registrada.");
    }
}
