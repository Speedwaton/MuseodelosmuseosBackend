package cl.triskeledu.museo.museos.servicio;

import cl.triskeledu.museo.museos.dto.Dtos.MuseoRequest;
import cl.triskeledu.museo.museos.dto.Dtos.RevisionRequest;
import cl.triskeledu.museo.museos.dto.Dtos.RolAutor;
import cl.triskeledu.museo.museos.error.ApiException;
import cl.triskeledu.museo.museos.modelo.EstadoFicha;
import cl.triskeledu.museo.museos.modelo.Museo;
import cl.triskeledu.museo.museos.repositorio.MuseoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

// Reglas de negocio de las fichas de museos.
@Service
public class MuseoService {
    private final MuseoRepository repositorio;

    public MuseoService(MuseoRepository repositorio) {
        this.repositorio = repositorio;
    }

    // Lista los museos. Con soloPublicados = true devuelve solo los que ve un Cliente.
    // pais y proyecto son filtros opcionales del mapa.
    public List<Museo> listar(boolean soloPublicados, String pais, String proyecto) {
        List<Museo> lista = soloPublicados
                ? repositorio.findByEstadoOrderByPaisAscNombreAsc(EstadoFicha.PUBLICADO)
                : repositorio.findAllByOrderByPaisAscNombreAsc();
        return lista.stream()
                .filter(m -> pais == null || pais.isBlank() || m.getPais().equalsIgnoreCase(pais))
                .filter(m -> proyecto == null || proyecto.isBlank() || m.getProyecto().equalsIgnoreCase(proyecto))
                .toList();
    }

    public Museo obtener(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe el museo " + id + "."));
    }

    // Busca la ficha que corresponde a un código QR (por ejemplo MUS-004).
    public Museo porCodigo(String codigo) {
        return repositorio.findByCodigo(codigo.trim().toUpperCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El código " + codigo + " no corresponde a ninguna ficha registrada."));
    }

    // Crea una ficha. La del Administrador queda publicada; la del Investigador queda en revisión.
    public Museo crear(MuseoRequest datos) {
        validarCodigoLibre(datos.codigo(), null);
        Museo m = new Museo();
        copiar(datos, m);
        m.setAutorId(datos.autorId());
        m.setEstado(datos.rolAutor() == RolAutor.ADMINISTRADOR ? EstadoFicha.PUBLICADO : EstadoFicha.EN_REVISION);
        return repositorio.save(m);
    }

    // Corrige una ficha. El Investigador solo puede corregir las suyas, y al hacerlo vuelve a revisión.
    public Museo actualizar(Long id, MuseoRequest datos) {
        Museo m = obtener(id);
        if (datos.rolAutor() == RolAutor.INVESTIGADOR && !datos.autorId().equals(m.getAutorId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo puedes corregir tus fichas.");
        }
        validarCodigoLibre(datos.codigo(), id);
        copiar(datos, m);
        if (datos.rolAutor() == RolAutor.INVESTIGADOR) {
            m.setEstado(EstadoFicha.EN_REVISION);
            m.setObservacion(null);
        }
        return repositorio.save(m);
    }

    // El Administrador publica u observa la ficha. Para observar hay que explicar el motivo.
    public Museo revisar(Long id, RevisionRequest datos) {
        Museo m = obtener(id);
        String obs = datos.observacion() == null || datos.observacion().isBlank() ? null : datos.observacion().trim();
        if (datos.estado() == EstadoFicha.OBSERVADO && obs == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Escribe la observación para el Investigador.");
        }
        m.setEstado(datos.estado());
        m.setObservacion(obs);
        return repositorio.save(m);
    }

    public void eliminar(Long id) {
        repositorio.delete(obtener(id));
    }

    // El código QR no se puede repetir entre fichas.
    private void validarCodigoLibre(String codigo, Long idActual) {
        repositorio.findByCodigo(codigo.trim().toUpperCase())
                .filter(otro -> !otro.getId().equals(idActual))
                .ifPresent(otro -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Ese código ya es de «" + otro.getNombre() + "».");
                });
    }

    private static void copiar(MuseoRequest d, Museo m) {
        m.setNombre(d.nombre().trim());
        m.setPais(d.pais().trim());
        m.setCiudad(d.ciudad().trim());
        m.setDescripcion(d.descripcion().trim());
        m.setClasificacion(d.clasificacion().trim());
        m.setProyecto(d.proyecto().trim());
        m.setLatitud(d.latitud());
        m.setLongitud(d.longitud());
        m.setUrlVisita(d.urlVisita().trim());
        m.setImagenUrl(d.imagenUrl() == null || d.imagenUrl().isBlank() ? null : d.imagenUrl().trim());
        m.setCodigo(d.codigo().trim().toUpperCase());
        if (d.fotoRuta() != null) m.setFotoRuta(d.fotoRuta());
    }
}
