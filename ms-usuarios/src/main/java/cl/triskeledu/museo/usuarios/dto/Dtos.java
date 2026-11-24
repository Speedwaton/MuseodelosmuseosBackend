package cl.triskeledu.museo.usuarios.dto;

import cl.triskeledu.museo.usuarios.modelo.Rol;
import cl.triskeledu.museo.usuarios.modelo.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Datos que entran y salen de la API (JSON). Nunca se devuelve la contraseña.
public final class Dtos {
    private Dtos() {
    }

    // POST /api/usuarios/login
    public record LoginRequest(
            @NotBlank(message = "Ingresa tu correo") String correo,
            @NotBlank(message = "Ingresa tu contraseña") String clave,
            @NotNull(message = "Elige un rol") Rol rol) {
    }

    // POST /api/usuarios/registro (crea siempre una cuenta de Cliente)
    public record RegistroRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(min = 3, message = "Escribe al menos 3 caracteres") String nombre,
            @NotBlank(message = "El correo es obligatorio") @Email(message = "Ingresa un correo válido") String correo,
            @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe incluir letras y números") String clave,
            String avatar) {
    }

    // PUT /api/usuarios/{id}: datos del perfil que puede cambiar el usuario.
    public record PerfilRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(min = 3, message = "Escribe al menos 3 caracteres") String nombre,
            String avatar, boolean notificaciones, boolean textoGrande, String idioma) {
    }

    // PATCH /api/usuarios/{id}/activo
    public record ActivoRequest(boolean activo) {
    }

    // Respuesta: el usuario sin su contraseña.
    public record UsuarioResponse(Long id, String nombre, String correo, Rol rol, String avatar, boolean activo,
                                  boolean notificaciones, boolean textoGrande, String idioma) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNombre(), u.getCorreo(), u.getRol(), u.getAvatar(), u.isActivo(),
                    u.isNotificaciones(), u.isTextoGrande(), u.getIdioma());
        }
    }
}
