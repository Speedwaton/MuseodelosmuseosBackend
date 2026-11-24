package cl.triskeledu.museo.usuarios.servicio;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

// Cifra y verifica contraseñas con PBKDF2 + sal (el mismo formato "sal$hash" que usaba la app).
public final class Seguridad {
    private static final HexFormat HEX = HexFormat.of();

    private Seguridad() {
    }

    // Cifra una contraseña nueva con una sal aleatoria.
    public static String hash(String clave) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return HEX.formatHex(sal) + "$" + HEX.formatHex(derivar(clave, sal));
    }

    // Compara la contraseña escrita con la guardada. Si el formato no es válido, responde false.
    public static boolean verificar(String clave, String guardado) {
        try {
            String[] partes = guardado.split("\\$");
            byte[] esperado = HEX.parseHex(partes[1]);
            return MessageDigest.isEqual(derivar(clave, HEX.parseHex(partes[0])), esperado);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] derivar(String clave, byte[] sal) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, 10_000, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña", e);
        }
    }
}
