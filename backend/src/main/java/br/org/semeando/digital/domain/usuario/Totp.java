package br.org.semeando.digital.domain.usuario;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.OptionalLong;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Senha de uso único baseada em tempo (RFC 6238, a mesma do Google Authenticator, Microsoft Authenticator e Authy):
 * HMAC-SHA1, 6 dígitos, passos de 30 segundos. Aceita um passo de tolerância para cada lado (relógio do celular).
 */
public final class Totp {

    public static final int DIGITOS = 6;
    public static final long PASSO_SEGUNDOS = 30;
    private static final int TOLERANCIA = 1;
    private static final String ALFABETO_BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private Totp() {
    }

    /** Segredo em Base32 (formato que os aplicativos autenticadores leem no QR Code). */
    public static String base32(byte[] bytes) {
        StringBuilder texto = new StringBuilder();
        int buffer = 0;
        int bits = 0;
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xff);
            bits += 8;
            while (bits >= 5) {
                texto.append(ALFABETO_BASE32.charAt((buffer >> (bits - 5)) & 31));
                bits -= 5;
            }
        }
        if (bits > 0) {
            texto.append(ALFABETO_BASE32.charAt((buffer << (5 - bits)) & 31));
        }
        return texto.toString();
    }

    static byte[] decodificarBase32(String texto) {
        String limpo = texto.replace("=", "").replace(" ", "").toUpperCase();
        ByteBuffer saida = ByteBuffer.allocate(limpo.length() * 5 / 8);
        int buffer = 0;
        int bits = 0;
        for (char c : limpo.toCharArray()) {
            int valor = ALFABETO_BASE32.indexOf(c);
            if (valor < 0) {
                throw new IllegalArgumentException("Segredo Base32 inválido");
            }
            buffer = (buffer << 5) | valor;
            bits += 5;
            if (bits >= 8) {
                saida.put((byte) (buffer >> (bits - 8)));
                bits -= 8;
            }
        }
        return saida.array();
    }

    public static long passo(Instant instante) {
        return Math.floorDiv(instante.getEpochSecond(), PASSO_SEGUNDOS);
    }

    public static String codigo(String segredo, long passo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(decodificarBase32(segredo), "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(passo).array());
            int deslocamento = hash[hash.length - 1] & 0xf;
            int binario = ((hash[deslocamento] & 0x7f) << 24) | ((hash[deslocamento + 1] & 0xff) << 16)
                    | ((hash[deslocamento + 2] & 0xff) << 8) | (hash[deslocamento + 3] & 0xff);
            return String.format("%0" + DIGITOS + "d", binario % 1_000_000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA1 indisponível", e);
        }
    }

    /** Passo em que o código confere (para impedir reuso), ou vazio. Comparação em tempo constante. */
    public static OptionalLong passoQueConfere(String segredo, String codigo, Instant instante) {
        if (codigo == null || !codigo.matches("\\d{" + DIGITOS + "}")) {
            return OptionalLong.empty();
        }
        long atual = passo(instante);
        OptionalLong encontrado = OptionalLong.empty();
        for (long p = atual - TOLERANCIA; p <= atual + TOLERANCIA; p++) {
            if (MessageDigest.isEqual(codigo(segredo, p).getBytes(), codigo.getBytes()) && encontrado.isEmpty()) {
                encontrado = OptionalLong.of(p);
            }
        }
        return encontrado;
    }

    /** URI {@code otpauth://} que vira o QR Code lido pelo aplicativo autenticador. */
    public static String uri(String emissor, String conta, String segredo) {
        String rotulo = codificar(emissor) + ":" + codificar(conta);
        return "otpauth://totp/" + rotulo + "?secret=" + segredo + "&issuer=" + codificar(emissor)
                + "&algorithm=SHA1&digits=" + DIGITOS + "&period=" + PASSO_SEGUNDOS;
    }

    private static String codificar(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
