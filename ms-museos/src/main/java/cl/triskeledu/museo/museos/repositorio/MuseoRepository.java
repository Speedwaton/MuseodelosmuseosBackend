package cl.triskeledu.museo.museos.repositorio;

import cl.triskeledu.museo.museos.modelo.EstadoFicha;
import cl.triskeledu.museo.museos.modelo.Museo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Consultas a la tabla museos.
public interface MuseoRepository extends JpaRepository<Museo, Long> {
    List<Museo> findAllByOrderByPaisAscNombreAsc();
    List<Museo> findByEstadoOrderByPaisAscNombreAsc(EstadoFicha estado);
    Optional<Museo> findByCodigo(String codigo);
}
