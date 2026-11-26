package cl.triskeledu.museo.usuarios.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import cl.triskeledu.museo.usuarios.dto.Dtos.ActivoRequest;
import cl.triskeledu.museo.usuarios.dto.Dtos.LoginRequest;
import cl.triskeledu.museo.usuarios.dto.Dtos.PerfilRequest;
import cl.triskeledu.museo.usuarios.dto.Dtos.RegistroRequest;
import cl.triskeledu.museo.usuarios.dto.Dtos.UsuarioResponse;
import cl.triskeledu.museo.usuarios.servicio.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Endpoints REST de usuarios: http://localhost:8081/api/usuarios
@Tag(name = "Usuarios", description = "Registro, inicio de sesión, perfiles y cuentas")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    // POST /api/usuarios/login -> 200 con el usuario, 401 si los datos no coinciden, 403 si está inactivo o es otro rol.
    @Operation(summary = "Iniciar sesión con correo, contraseña y rol")
    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest datos) {
        return UsuarioResponse.de(servicio.iniciarSesion(datos));
    }

    // POST /api/usuarios/registro -> 201 con la cuenta nueva, 409 si el correo ya existe.
    @Operation(summary = "Registrar una cuenta de Cliente")
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest datos) {
        return UsuarioResponse.de(servicio.registrar(datos));
    }

    // GET /api/usuarios -> todas las cuentas (pantalla de cuentas del Administrador).
    @Operation(summary = "Listar todas las cuentas")
    @GetMapping
    public List<UsuarioResponse> listar() {
        return servicio.listar().stream().map(UsuarioResponse::de).toList();
    }

    // GET /api/usuarios/{id} -> 200 o 404.
    @Operation(summary = "Obtener una cuenta por su id")
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return UsuarioResponse.de(servicio.obtener(id));
    }

    // PUT /api/usuarios/{id} -> actualiza el perfil.
    @Operation(summary = "Actualizar el perfil (nombre, avatar y preferencias)")
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody PerfilRequest datos) {
        return UsuarioResponse.de(servicio.actualizarPerfil(id, datos));
    }

    // PATCH /api/usuarios/{id}/activo -> activa o desactiva la cuenta.
    @Operation(summary = "Activar o desactivar una cuenta")
    @PatchMapping("/{id}/activo")
    public UsuarioResponse cambiarActivo(@PathVariable Long id, @RequestBody ActivoRequest datos) {
        return UsuarioResponse.de(servicio.cambiarActivo(id, datos.activo()));
    }
}
