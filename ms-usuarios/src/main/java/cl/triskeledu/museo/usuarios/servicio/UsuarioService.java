package cl.triskeledu.museo.usuarios.servicio;

import cl.triskeledu.museo.usuarios.dto.Dtos.LoginRequest;
import cl.triskeledu.museo.usuarios.dto.Dtos.PerfilRequest;
import cl.triskeledu.museo.usuarios.dto.Dtos.RegistroRequest;
import cl.triskeledu.museo.usuarios.error.ApiException;
import cl.triskeledu.museo.usuarios.modelo.Rol;
import cl.triskeledu.museo.usuarios.modelo.Usuario;
import cl.triskeledu.museo.usuarios.repositorio.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

// Reglas de negocio de los usuarios. El controlador solo recibe y responde; aquí se decide.
@Service
public class UsuarioService {
    private final UsuarioRepository repositorio;

    // El repositorio llega por el constructor (inyección de dependencias), así las pruebas pueden simularlo.
    public UsuarioService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    // Revisa correo, contraseña, rol y si la cuenta está activa.
    public Usuario iniciarSesion(LoginRequest datos) {
        Usuario u = repositorio.findByCorreo(normalizar(datos.correo()))
                .filter(x -> Seguridad.verificar(datos.clave(), x.getClaveHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));
        if (!u.isActivo()) throw new ApiException(HttpStatus.FORBIDDEN, "El Administrador desactivó esta cuenta.");
        if (u.getRol() != datos.rol()) throw new ApiException(HttpStatus.FORBIDDEN, "Esta cuenta es de rol " + etiqueta(u.getRol()) + ".");
        return u;
    }

    // Crea una cuenta de Cliente si el correo no existe.
    public Usuario registrar(RegistroRequest datos) {
        String correo = normalizar(datos.correo());
        if (repositorio.existsByCorreo(correo)) throw new ApiException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        String avatar = datos.avatar() == null || datos.avatar().isBlank() ? "🎨" : datos.avatar();
        return repositorio.save(new Usuario(datos.nombre().trim(), correo, Seguridad.hash(datos.clave()), Rol.CLIENTE, avatar));
    }

    public List<Usuario> listar() {
        return repositorio.findAllByOrderByRolAscNombreAsc();
    }

    public Usuario obtener(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe el usuario " + id + "."));
    }

    // Guarda los cambios del perfil (nombre, avatar y preferencias).
    public Usuario actualizarPerfil(Long id, PerfilRequest datos) {
        Usuario u = obtener(id);
        u.setNombre(datos.nombre().trim());
        if (datos.avatar() != null && !datos.avatar().isBlank()) u.setAvatar(datos.avatar());
        u.setNotificaciones(datos.notificaciones());
        u.setTextoGrande(datos.textoGrande());
        if (datos.idioma() != null && !datos.idioma().isBlank()) u.setIdioma(datos.idioma());
        return repositorio.save(u);
    }

    // El Administrador activa o desactiva una cuenta.
    public Usuario cambiarActivo(Long id, boolean activo) {
        Usuario u = obtener(id);
        u.setActivo(activo);
        return repositorio.save(u);
    }

    private static String normalizar(String correo) {
        return correo.trim().toLowerCase();
    }

    private static String etiqueta(Rol rol) {
        return switch (rol) {
            case ADMINISTRADOR -> "Administrador";
            case INVESTIGADOR -> "Investigador";
            case CLIENTE -> "Cliente";
        };
    }
}
