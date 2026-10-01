package br.org.semeando.digital.domain.shared;

import java.util.Collection;

/** Guard clauses usadas pelas entidades para manter suas invariantes. */
public final class Validacoes {

    private Validacoes() {
    }

    public static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DomainException("CAMPO_OBRIGATORIO", campo + " é obrigatório");
        }
        return valor.trim();
    }

    public static <T> T obrigatorio(T valor, String campo) {
        if (valor == null) {
            throw new DomainException("CAMPO_OBRIGATORIO", campo + " é obrigatório");
        }
        return valor;
    }

    public static <T extends Collection<?>> T naoVazio(T valores, String campo) {
        if (valores == null || valores.isEmpty()) {
            throw new DomainException("CAMPO_OBRIGATORIO", campo + " deve ter ao menos um item");
        }
        return valores;
    }

    public static int positivo(int valor, String campo) {
        if (valor <= 0) {
            throw new DomainException("VALOR_INVALIDO", campo + " deve ser maior que zero");
        }
        return valor;
    }
}
