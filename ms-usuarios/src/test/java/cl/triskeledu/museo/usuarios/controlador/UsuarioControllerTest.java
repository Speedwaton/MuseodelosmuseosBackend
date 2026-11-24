package cl.triskeledu.museo.usuarios.controlador;

import cl.triskeledu.museo.usuarios.dto.Dtos.LoginRequest;
import cl.triskeledu.museo.usuarios.error.ApiException;
import cl.triskeledu.museo.usuarios.modelo.Rol;
import cl.triskeledu.museo.usuarios.modelo.Usuario;
import cl.triskeledu.museo.usuarios.servicio.UsuarioService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas del controlador: peticiones HTTP simuladas (MockMvc) con el servicio simulado. No levanta el servidor ni la base.
@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    UsuarioService servicio;

    private Usuario admin() {
        Usuario u = new Usuario("Admin Demo", "admin@museo.demo", "sal$hash", Rol.ADMINISTRADOR, "🏛️");
        u.setId(1L);
        return u;
    }

    @Test
    void login_correcto_responde200_sinLaContrasena() throws Exception {
        when(servicio.iniciarSesion(any(LoginRequest.class))).thenReturn(admin());

        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@museo.demo\",\"clave\":\"Admin#2026\",\"rol\":\"ADMINISTRADOR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.claveHash").doesNotExist());   // La contraseña nunca sale de la API
    }

    @Test
    void login_incorrecto_responde401_conMensaje() throws Exception {
        when(servicio.iniciarSesion(any())).thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));

        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@museo.demo\",\"clave\":\"mala\",\"rol\":\"ADMINISTRADOR\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Correo o contraseña incorrectos."));
    }

    @Test
    void registro_conClaveCorta_responde400_sinLlamarAlServicio() throws Exception {
        mvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"ana@museo.demo\",\"clave\":\"corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La contraseña debe tener al menos 8 caracteres"));
        verify(servicio, never()).registrar(any());
    }

    @Test
    void login_conRolDesconocido_responde400() throws Exception {
        mvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"a@b.cl\",\"clave\":\"x\",\"rol\":\"PIRATA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El JSON enviado no es válido"));
    }

    @Test
    void listar_devuelveTodasLasCuentas() throws Exception {
        when(servicio.listar()).thenReturn(List.of(admin()));

        mvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].correo").value("admin@museo.demo"));
    }

    @Test
    void obtener_inexistente_responde404() throws Exception {
        when(servicio.obtener(99L)).thenThrow(new ApiException(HttpStatus.NOT_FOUND, "No existe el usuario 99."));

        mvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe el usuario 99."));
    }
}
