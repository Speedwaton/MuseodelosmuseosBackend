package cl.triskeledu.museo.usuarios.controlador;

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
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    // POST /api/usuarios/login -> 200 con el usuario, 401 si los datos no coinciden, 403 si está inactivo o es otro rol.
    @PostMapping("/login")
    public UsuarioResponse login(@Valid @RequestBody LoginRequest datos) {
        return UsuarioResponse.de(servicio.iniciarSesion(datos));
    }

    // POST /api/usuarios/registro -> 201 con la cuenta nueva, 409 si el correo ya existe.
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest datos) {
        return UsuarioResponse.de(servicio.registrar(datos));
    }

    // GET /api/usuarios -> todas las cuentas (pantalla de cuentas del Administrador).
    @GetMapping
    public List<UsuarioResponse> listar() {
        return servicio.listar().stream().map(UsuarioResponse::de).toList();
    }

    // GET /api/usuarios/{id} -> 200 o 404.
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return UsuarioResponse.de(servicio.obtener(id));
    }

    // PUT /api/usuarios/{id} -> actualiza el perfil.
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody PerfilRequest datos) {
        return UsuarioResponse.de(servicio.actualizarPerfil(id, datos));
    }

    // PATCH /api/usuarios/{id}/activo -> activa o desactiva la cuenta.
    @PatchMapping("/{id}/activo")
    public UsuarioResponse cambiarActivo(@PathVariable Long id, @RequestBody ActivoRequest datos) {
        return UsuarioResponse.de(servicio.cambiarActivo(id, datos.activo()));
    }
}
