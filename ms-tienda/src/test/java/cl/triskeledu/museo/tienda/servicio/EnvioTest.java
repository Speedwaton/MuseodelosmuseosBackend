package cl.triskeledu.museo.tienda.servicio;

import cl.triskeledu.museo.tienda.servicio.Envio.Destino;
import cl.triskeledu.museo.tienda.servicio.Envio.Modalidad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

// Pruebas del cálculo de envío. Los valores esperados son los mismos que muestra la app.
class EnvioTest {

    @ParameterizedTest(name = "{0} g, ${1}, {2} {3} -> ${4}")
    @CsvSource({
            "400,  9900,  NACIONAL,      ESTANDAR, 4700",    // 3.500 + 1.200 × 1 kg
            "1200, 30000, NACIONAL,      ESTANDAR, 5900",    // 2 kg: se redondea hacia arriba
            "400,  9900,  NACIONAL,      EXPRESS,  7500",    // 4.700 × 1,6 = 7.520 -> 7.500
            "500,  129800, INTERNACIONAL, EXPRESS, 32800",   // (14.000 + 6.500) × 1,6
            "900,  15900, INTERNACIONAL, ESTANDAR, 20500",
    })
    void calcula_segunPesoDestinoYModalidad(int gramos, int subtotal, Destino destino, Modalidad modalidad, int esperado) {
        assertThat(Envio.calcular(gramos, subtotal, destino, modalidad)).isEqualTo(esperado);
    }

    @Test
    void cursos_sinPeso_noPaganEnvio() {
        assertThat(Envio.calcular(0, 89_000, Destino.INTERNACIONAL, Modalidad.EXPRESS)).isZero();
    }

    @Test
    void nacionalEstandar_esGratisDesde80Mil() {
        assertThat(Envio.calcular(250, 80_000, Destino.NACIONAL, Modalidad.ESTANDAR)).isZero();
        assertThat(Envio.calcular(250, 79_999, Destino.NACIONAL, Modalidad.ESTANDAR)).isEqualTo(4_700);
    }

    @Test
    void elEnvioGratis_noAplicaAExpressNiInternacional() {
        assertThat(Envio.calcular(250, 200_000, Destino.NACIONAL, Modalidad.EXPRESS)).isPositive();
        assertThat(Envio.calcular(250, 200_000, Destino.INTERNACIONAL, Modalidad.ESTANDAR)).isPositive();
    }
}
