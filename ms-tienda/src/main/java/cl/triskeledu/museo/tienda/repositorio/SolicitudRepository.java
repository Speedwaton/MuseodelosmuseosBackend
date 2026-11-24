package cl.triskeledu.museo.tienda.repositorio;

import cl.triskeledu.museo.tienda.modelo.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Consultas a la tabla solicitudes (las más nuevas primero).
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findAllByOrderByFechaDesc();
    List<Solicitud> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
