package cl.triskeledu.museo.tienda.servicio;

// Cálculo del costo de envío con reglas inventadas (no hay courier real), las mismas que mostraba la app:
// nacional $3.500 + $1.200 por kg, internacional $14.000 + $6.500 por kg,
// express cuesta 1,6 veces más y el nacional estándar es gratis desde $80.000.
public final class Envio {

    public enum Destino {
        NACIONAL("Nacional (Chile)", 3_500, 1_200), INTERNACIONAL("Internacional", 14_000, 6_500);

        public final String etiqueta;
        final int base;
        final int porKilo;

        Destino(String etiqueta, int base, int porKilo) {
            this.etiqueta = etiqueta;
            this.base = base;
            this.porKilo = porKilo;
        }
    }

    public enum Modalidad {
        ESTANDAR("Estándar", 1.0), EXPRESS("Express", 1.6);

        public final String etiqueta;
        final double factor;

        Modalidad(String etiqueta, double factor) {
            this.etiqueta = etiqueta;
            this.factor = factor;
        }
    }

    public static final int GRATIS_DESDE = 80_000;

    private Envio() {
    }

    // Devuelve el costo redondeado a la centena. Los productos sin peso (cursos) no pagan envío.
    public static int calcular(int gramos, int subtotal, Destino destino, Modalidad modalidad) {
        if (gramos <= 0) return 0;
        if (destino == Destino.NACIONAL && modalidad == Modalidad.ESTANDAR && subtotal >= GRATIS_DESDE) return 0;
        int kilos = (int) Math.ceil(gramos / 1000.0);
        double costo = (destino.base + destino.porKilo * kilos) * modalidad.factor;
        return (int) Math.round(costo / 100) * 100;
    }
}
