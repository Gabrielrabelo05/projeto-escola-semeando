package br.org.semeando.digital.domain.pedagogico;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CalculadoraFrequencia {

    private CalculadoraFrequencia() {
    }

    public static Frequencia doAluno(UUID alunoId, Collection<DiarioClasse> diarios) {
        int aulas = 0;
        int presencas = 0;
        for (DiarioClasse diario : diarios) {
            for (Presenca presenca : diario.presencas()) {
                if (presenca.alunoId().equals(alunoId)) {
                    aulas++;
                    presencas += presenca.presente() ? 1 : 0;
                }
            }
        }
        return new Frequencia(alunoId, aulas, presencas);
    }

    /** RF-45: visão consolidada da turma. */
    public static List<Frequencia> daTurma(Collection<DiarioClasse> diarios) {
        Map<UUID, int[]> contagem = new LinkedHashMap<>();
        diarios.forEach(diario -> diario.presencas().forEach(presenca -> {
            int[] valores = contagem.computeIfAbsent(presenca.alunoId(), id -> new int[2]);
            valores[0]++;
            valores[1] += presenca.presente() ? 1 : 0;
        }));
        return contagem.entrySet().stream()
                .map(entrada -> new Frequencia(entrada.getKey(), entrada.getValue()[0], entrada.getValue()[1]))
                .toList();
    }
}
