package cl.triskeledu.museo.usuarios.servicio;

import cl.triskeledu.museo.usuarios.dto.Dtos.LoginRequest;
import cl.triskeledu.museo.usuarios.dto.Dtos.PerfilRequest;
import cl.triskeledu.museo.usuarios.dto.Dtos.RegistroRequest;
import cl.triskeledu.museo.usuarios.error.ApiException;
import cl.triskeledu.museo.usuarios.modelo.Rol;
import cl.triskeledu.museo.usuarios.modelo.Usuario;
import cl.triskeledu.museo.usuarios.repositorio.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Pruebas unitarias de UsuarioService con un repositorio simulado (Mockito): no se usa la base de datos.
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository repositorio;      // Repositorio falso

    @InjectMocks
    UsuarioService servicio;            // Servicio real que usa el repositorio falso

    private Usuario cliente(boolean activo) {
        Usuario u = new Usuario("Cliente Demo", "cliente@museo.demo", Seguridad.hash("Cliente#2026"), Rol.CLIENTE, "🎨");
        u.setId(3L);
        u.setActivo(activo);
        return u;
    }

    @Test
    void iniciarSesion_conDatosCorrectos_devuelveElUsuario() {
        when(repositorio.findByCorreo("cliente@museo.demo")).thenReturn(Optional.of(cliente(true)));   // Preparar

        Usuario u = servicio.iniciarSesion(new LoginRequest("  Cliente@Museo.demo ", "Cliente#2026", Rol.CLIENTE));  // Actuar

        assertThat(u.getId()).isEqualTo(3L);                                                          // Verificar
    }

    @Test
    void iniciarSesion_conClaveIncorrecta_responde401() {
        when(repositorio.findByCorreo("cliente@museo.demo")).thenReturn(Optional.of(cliente(true)));

        assertThatThrownBy(() -> servicio.iniciarSesion(new LoginRequest("cliente@museo.demo", "mala", Rol.CLIENTE)))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getEstado()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void iniciarSesion_conCorreoInexistente_responde401() {
        when(repositorio.findByCorreo("nadie@museo.demo")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.iniciarSesion(new LoginRequest("nadie@museo.demo", "Clave1234", Rol.CLIENTE)))
                .hasMessage("Correo o contraseña incorrectos.");
    }

    @Test
    void iniciarSesion_conCuentaDesactivada_responde403() {
        when(repositorio.findByCorreo("cliente@museo.demo")).thenReturn(Optional.of(cliente(false)));

        assertThatThrownBy(() -> servicio.iniciarSesion(new LoginRequest("cliente@museo.demo", "Cliente#2026", Rol.CLIENTE)))
                .hasMessage("El Administrador desactivó esta cuenta.");
    }

    @Test
    void iniciarSesion_conOtroRol_avisaElRolCorrecto() {
        when(repositorio.findByCorreo("cliente@museo.demo")).thenReturn(Optional.of(cliente(true)));

        assertThatThrownBy(() -> servicio.iniciarSesion(new LoginRequest("cliente@museo.demo", "Cliente#2026", Rol.ADMINISTRADOR)))
                .hasMessage("Esta cuenta es de rol Cliente.");
    }

    @Test
    void registrar_creaUnClienteConLaClaveCifrada() {
        when(repositorio.existsByCorreo("ana@museo.demo")).thenReturn(false);
        when(repositorio.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        servicio.registrar(new RegistroRequest(" Ana ", "ANA@museo.demo", "Clave1234", ""));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(repositorio).save(guardado.capture());
        assertThat(guardado.getValue().getRol()).isEqualTo(Rol.CLIENTE);         // Siempre Cliente
        assertThat(guardado.getValue().getCorreo()).isEqualTo("ana@museo.demo");  // Correo en minúsculas
        assertThat(guardado.getValue().getNombre()).isEqualTo("Ana");
        assertThat(guardado.getValue().getAvatar()).isEqualTo("🎨");               // Avatar por defecto
        assertThat(Seguridad.verificar("Clave1234", guardado.getValue().getClaveHash())).isTrue();
    }

    @Test
    void registrar_conCorreoRepetido_responde409_yNoGuarda() {
        when(repositorio.existsByCorreo("cliente@museo.demo")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(new RegistroRequest("Otra", "cliente@museo.demo", "Clave1234", "⭐")))
                .extracting(e -> ((ApiException) e).getEstado()).isEqualTo(HttpStatus.CONFLICT);
        verify(repositorio, never()).save(any());
    }

    @Test
    void obtener_siNoExiste_responde404() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener(99L))
                .extracting(e -> ((ApiException) e).getEstado()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void actualizarPerfil_cambiaNombreYPreferencias() {
        when(repositorio.findById(3L)).thenReturn(Optional.of(cliente(true)));
        when(repositorio.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario u = servicio.actualizarPerfil(3L, new PerfilRequest(" Cliente Nuevo ", "⭐", false, true, "pt"));

        assertThat(u.getNombre()).isEqualTo("Cliente Nuevo");
        assertThat(u.getAvatar()).isEqualTo("⭐");
        assertThat(u.isNotificaciones()).isFalse();
        assertThat(u.isTextoGrande()).isTrue();
        assertThat(u.getIdioma()).isEqualTo("pt");
    }

    @Test
    void cambiarActivo_desactivaLaCuenta() {
        when(repositorio.findById(3L)).thenReturn(Optional.of(cliente(true)));
        when(repositorio.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(servicio.cambiarActivo(3L, false).isActivo()).isFalse();
    }
}
