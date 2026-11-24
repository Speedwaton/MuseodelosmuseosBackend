package cl.triskeledu.museo.usuarios.repositorio;

import cl.triskeledu.museo.usuarios.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Consultas a la tabla usuarios. Spring Data crea el SQL a partir del nombre de cada método.
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    List<Usuario> findAllByOrderByRolAscNombreAsc();
}
