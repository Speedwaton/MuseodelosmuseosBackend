package cl.triskeledu.museo.usuarios.servicio;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Pruebas del cifrado de contraseñas.
class SeguridadTest {

    @Test
    void hash_noGuardaLaClaveReal_yLaVerificaBien() {
        String guardado = Seguridad.hash("Cliente#2026");

        assertThat(guardado).doesNotContain("Cliente#2026").contains("$");
        assertThat(Seguridad.verificar("Cliente#2026", guardado)).isTrue();
        assertThat(Seguridad.verificar("otraClave1", guardado)).isFalse();
    }

    @Test
    void hash_usaUnaSalDistintaCadaVez() {
        // La misma contraseña cifrada dos veces da resultados distintos (por la sal aleatoria).
        assertThat(Seguridad.hash("Clave1234")).isNotEqualTo(Seguridad.hash("Clave1234"));
    }

    @Test
    void verificar_conFormatoInvalido_respondeFalse() {
        assertThat(Seguridad.verificar("Clave1234", "texto-sin-formato")).isFalse();
    }

    @Test
    void verificar_aceptaLosHashesDeData_sql() {
        // Hash de "Cliente#2026" guardado en data.sql: el backend debe poder validarlo.
        String deData = "e61e30ab935598f0253bb3c0547dea54$04209b41084f994c9c8189361a195895eb16b3dee2de5debca3371d2ea9f383a";
        assertThat(Seguridad.verificar("Cliente#2026", deData)).isTrue();
    }
}
