package br.org.semeando.digital.domain.pedagogico;

import java.util.UUID;

public record Presenca(UUID alunoId, boolean presente) {
}
