package br.org.semeando.digital.domain.obrigacao;

import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** RF-47 a RF-54: obrigação regulatória com prazo, checklist, comprovantes e alertas. */
public class ObrigacaoRegulatoria {

    private final UUID id;
    private final TipoObrigacao tipo;
    private final String nome;
    private final int anoReferencia;
    private final Integer bimestre;
    private final LocalDate prazo;
    private StatusObrigacao status;
    private String responsavel;
    private final List<ItemChecklist> checklist;
    private final List<Comprovante> comprovantes;
    private final Set<NivelAlerta> alertasEnviados;
    private LocalDate concluidaEm;

    public ObrigacaoRegulatoria(UUID id, TipoObrigacao tipo, String nome, int anoReferencia, Integer bimestre,
            LocalDate prazo, StatusObrigacao status, String responsavel, List<ItemChecklist> checklist,
            List<Comprovante> comprovantes, Set<NivelAlerta> alertasEnviados, LocalDate concluidaEm) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.tipo = Validacoes.obrigatorio(tipo, "tipo");
        this.nome = Validacoes.obrigatorio(nome, "nome");
        this.anoReferencia = anoReferencia;
        this.bimestre = bimestre;
        this.prazo = Validacoes.obrigatorio(prazo, "prazo");
        this.status = Validacoes.obrigatorio(status, "status");
        this.responsavel = Validacoes.obrigatorio(responsavel, "responsável");
        this.checklist = new ArrayList<>(checklist);
        this.comprovantes = new ArrayList<>(comprovantes);
        this.alertasEnviados = alertasEnviados.isEmpty() ? EnumSet.noneOf(NivelAlerta.class)
                : EnumSet.copyOf(alertasEnviados);
        this.concluidaEm = concluidaEm;
    }

    /** O prazo das obrigações legais é calculado pelo sistema e não pode ser alterado (RN-10). */
    public static ObrigacaoRegulatoria criar(TipoObrigacao tipo, int ano, Integer bimestre, String responsavel) {
        if (!tipo.prazoFixo()) {
            throw new DomainException("PRAZO_MANUAL", "Use criarPersonalizada para obrigações do tipo OUTRA");
        }
        String nome = tipo.descricao() + " " + ano + (bimestre == null ? "" : " - " + bimestre + "º bimestre");
        return nova(tipo, nome, ano, bimestre, tipo.prazo(ano, bimestre), responsavel, tipo.checklistPadrao());
    }

    public static ObrigacaoRegulatoria criarPersonalizada(String nome, LocalDate prazo, String responsavel,
            List<String> checklist) {
        return nova(TipoObrigacao.OUTRA, nome, prazo.getYear(), null, prazo, responsavel, checklist);
    }

    private static ObrigacaoRegulatoria nova(TipoObrigacao tipo, String nome, int ano, Integer bimestre,
            LocalDate prazo, String responsavel, List<String> checklist) {
        List<ItemChecklist> itens = checklist.stream().map(item -> new ItemChecklist(item, false)).toList();
        return new ObrigacaoRegulatoria(UUID.randomUUID(), tipo, nome, ano, bimestre, prazo,
                StatusObrigacao.PENDENTE, responsavel, itens, List.of(), Set.of(), null);
    }

    public void marcarItem(int indice, boolean concluido) {
        exigirAberta();
        if (indice < 0 || indice >= checklist.size()) {
            throw new DomainException("ITEM_INVALIDO", "Item de checklist inexistente");
        }
        checklist.set(indice, new ItemChecklist(checklist.get(indice).descricao(), concluido));
        status = StatusObrigacao.EM_ANDAMENTO;
    }

    public void anexarComprovante(Comprovante comprovante) {
        exigirAberta();
        comprovantes.add(Validacoes.obrigatorio(comprovante, "comprovante"));
        status = StatusObrigacao.EM_ANDAMENTO;
    }

    /** A conclusão exige ao menos um comprovante como evidência digital (RF-53). */
    public void concluir(LocalDate hoje) {
        exigirAberta();
        if (comprovantes.isEmpty()) {
            throw new DomainException("COMPROVANTE_OBRIGATORIO",
                    "Anexe o comprovante de entrega antes de concluir a obrigação");
        }
        status = StatusObrigacao.CONCLUIDA;
        concluidaEm = hoje;
    }

    public void alterarResponsavel(String responsavel) {
        this.responsavel = Validacoes.obrigatorio(responsavel, "responsável");
    }

    /** RF-52 / RN-10: devolve o alerta devido hoje e que ainda não foi enviado. */
    public Optional<NivelAlerta> alertaDevido(LocalDate hoje) {
        if (status == StatusObrigacao.CONCLUIDA) {
            return Optional.empty();
        }
        long dias = diasRestantes(hoje);
        NivelAlerta nivel = dias < 0 ? NivelAlerta.VENCIDA
                : dias <= NivelAlerta.QUINZE_DIAS.dias() ? NivelAlerta.QUINZE_DIAS
                        : dias <= NivelAlerta.TRINTA_DIAS.dias() ? NivelAlerta.TRINTA_DIAS : null;
        return Optional.ofNullable(nivel).filter(n -> !alertasEnviados.contains(n));
    }

    public void registrarAlertaEnviado(NivelAlerta nivel) {
        alertasEnviados.add(nivel);
    }

    public long diasRestantes(LocalDate hoje) {
        return ChronoUnit.DAYS.between(hoje, prazo);
    }

    public boolean atrasada(LocalDate hoje) {
        return status != StatusObrigacao.CONCLUIDA && hoje.isAfter(prazo);
    }

    private void exigirAberta() {
        if (status == StatusObrigacao.CONCLUIDA) {
            throw new DomainException("STATUS_INVALIDO", "Obrigação já concluída");
        }
    }

    public UUID id() {
        return id;
    }

    public TipoObrigacao tipo() {
        return tipo;
    }

    public String nome() {
        return nome;
    }

    public int anoReferencia() {
        return anoReferencia;
    }

    public Integer bimestre() {
        return bimestre;
    }

    public LocalDate prazo() {
        return prazo;
    }

    public StatusObrigacao status() {
        return status;
    }

    public String responsavel() {
        return responsavel;
    }

    public List<ItemChecklist> checklist() {
        return Collections.unmodifiableList(checklist);
    }

    public List<Comprovante> comprovantes() {
        return Collections.unmodifiableList(comprovantes);
    }

    public Set<NivelAlerta> alertasEnviados() {
        return Collections.unmodifiableSet(alertasEnviados);
    }

    public LocalDate concluidaEm() {
        return concluidaEm;
    }
}
