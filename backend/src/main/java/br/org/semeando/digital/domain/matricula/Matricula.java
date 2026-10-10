package br.org.semeando.digital.domain.matricula;

import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Matricula {

    private final UUID id;
    private final UUID alunoId;
    private UUID turmaId;
    private final NivelEnsino nivel;
    private final int anoLetivo;
    private final TipoMatricula tipo;
    private StatusMatricula status;
    private final LocalDate dataSolicitacao;
    private String justificativaRejeicao;
    private Farda farda;
    private final List<DocumentoMatricula> documentos;
    private UUID primeiraParcelaId;

    public Matricula(UUID id, UUID alunoId, UUID turmaId, NivelEnsino nivel, int anoLetivo, TipoMatricula tipo,
            StatusMatricula status, LocalDate dataSolicitacao, String justificativaRejeicao, Farda farda,
            List<DocumentoMatricula> documentos, UUID primeiraParcelaId) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.alunoId = Validacoes.obrigatorio(alunoId, "aluno");
        this.turmaId = Validacoes.obrigatorio(turmaId, "turma");
        this.nivel = Validacoes.obrigatorio(nivel, "nível de ensino");
        this.anoLetivo = Validacoes.positivo(anoLetivo, "ano letivo");
        this.tipo = Validacoes.obrigatorio(tipo, "tipo de matrícula");
        this.status = Validacoes.obrigatorio(status, "status");
        this.dataSolicitacao = Validacoes.obrigatorio(dataSolicitacao, "data da solicitação");
        this.justificativaRejeicao = justificativaRejeicao;
        this.farda = farda;
        this.documentos = new ArrayList<>(documentos);
        this.primeiraParcelaId = primeiraParcelaId;
    }

    /**
     * RF-01: pré-matrícula com o checklist de documentos (RN-05). Alunos vindos de outra instituição particular
     * também precisam apresentar a declaração de adimplência.
     */
    public static Matricula solicitar(UUID alunoId, UUID turmaId, NivelEnsino nivel, int anoLetivo,
            boolean vindoDeEscolaParticular, LocalDate hoje) {
        List<DocumentoMatricula> checklist = TipoDocumento.exigidos(vindoDeEscolaParticular).stream()
                .map(DocumentoMatricula::pendente)
                .toList();
        return new Matricula(UUID.randomUUID(), alunoId, turmaId, nivel, anoLetivo, TipoMatricula.NOVA,
                StatusMatricula.AGUARDANDO_ANALISE, hoje, null, null, checklist, null);
    }

    /** RF-08: renovação reaproveita documentos ainda vigentes e sinaliza apenas os vencidos ou novos. */
    public Matricula renovar(UUID novaTurmaId, NivelEnsino novoNivel, int novoAnoLetivo, LocalDate hoje) {
        if (novoAnoLetivo <= anoLetivo) {
            throw new DomainException("RENOVACAO_INVALIDA", "A renovação deve ser para um ano letivo posterior");
        }
        List<DocumentoMatricula> checklist = TipoDocumento.exigidos(false).stream()
                .map(tipoDoc -> documentos.stream()
                        .filter(doc -> doc.tipo() == tipoDoc && doc.vigenteEm(hoje))
                        .findFirst()
                        .orElse(DocumentoMatricula.pendente(tipoDoc)))
                .toList();
        return new Matricula(UUID.randomUUID(), alunoId, novaTurmaId, novoNivel, novoAnoLetivo,
                TipoMatricula.RENOVACAO, StatusMatricula.AGUARDANDO_ANALISE, hoje, null, farda, checklist, null);
    }

    /** RF-02 */
    public void aprovar() {
        exigirStatus(StatusMatricula.AGUARDANDO_ANALISE, "aprovar");
        this.status = StatusMatricula.APROVADA;
    }

    /** RF-02: rejeição sempre com justificativa registrada. */
    public void rejeitar(String justificativa) {
        exigirStatus(StatusMatricula.AGUARDANDO_ANALISE, "rejeitar");
        this.justificativaRejeicao = Validacoes.obrigatorio(justificativa, "justificativa da rejeição");
        this.status = StatusMatricula.REJEITADA;
    }

    /** Passo 13 do fluxo: após a confirmação do pagamento da primeira parcela a matrícula fica ATIVA. */
    public void ativar() {
        exigirStatus(StatusMatricula.APROVADA, "ativar");
        this.status = StatusMatricula.ATIVA;
    }

    /** RN-04: a transferência de turma gera registro histórico (a matrícula antiga é encerrada). */
    public void encerrarPorTransferencia() {
        exigirStatus(StatusMatricula.ATIVA, "transferir");
        this.status = StatusMatricula.TRANSFERIDA;
    }

    public void cancelar() {
        if (status == StatusMatricula.REJEITADA || status == StatusMatricula.CANCELADA) {
            throw new DomainException("STATUS_INVALIDO", "Matrícula já encerrada");
        }
        this.status = StatusMatricula.CANCELADA;
    }

    public void registrarFarda(Farda farda) {
        this.farda = Validacoes.obrigatorio(farda, "farda");
    }

    /** A primeira parcela (mensalidade ou taxa de associado) confirma a matrícula, como sinal do contrato. */
    public void vincularPrimeiraParcela(UUID cobrancaId) {
        this.primeiraParcelaId = Validacoes.obrigatorio(cobrancaId, "cobrança");
    }

    /** RF-06: registra a entrega de um documento do checklist. */
    public void registrarEntregaDocumento(TipoDocumento tipoDocumento, String arquivo, LocalDate data) {
        if (documentos.stream().noneMatch(doc -> doc.tipo() == tipoDocumento)) {
            throw new DomainException("RN-05",
                    tipoDocumento.descricao() + " não faz parte do checklist desta matrícula");
        }
        documentos.replaceAll(doc -> doc.tipo() == tipoDocumento
                ? new DocumentoMatricula(tipoDocumento, Validacoes.obrigatorio(arquivo, "arquivo"), data)
                : doc);
    }

    public List<TipoDocumento> pendencias(LocalDate hoje) {
        return documentos.stream().filter(doc -> !doc.vigenteEm(hoje)).map(DocumentoMatricula::tipo).toList();
    }

    public boolean ocupaVaga() {
        return status == StatusMatricula.APROVADA || status == StatusMatricula.ATIVA;
    }

    private void exigirStatus(StatusMatricula esperado, String operacao) {
        if (status != esperado) {
            throw new DomainException("STATUS_INVALIDO",
                    "Não é possível " + operacao + " uma matrícula com status " + status);
        }
    }

    public UUID id() {
        return id;
    }

    public UUID alunoId() {
        return alunoId;
    }

    public UUID turmaId() {
        return turmaId;
    }

    public NivelEnsino nivel() {
        return nivel;
    }

    public int anoLetivo() {
        return anoLetivo;
    }

    public TipoMatricula tipo() {
        return tipo;
    }

    public StatusMatricula status() {
        return status;
    }

    public LocalDate dataSolicitacao() {
        return dataSolicitacao;
    }

    public String justificativaRejeicao() {
        return justificativaRejeicao;
    }

    public Farda farda() {
        return farda;
    }

    public List<DocumentoMatricula> documentos() {
        return Collections.unmodifiableList(documentos);
    }

    public UUID primeiraParcelaId() {
        return primeiraParcelaId;
    }
}
