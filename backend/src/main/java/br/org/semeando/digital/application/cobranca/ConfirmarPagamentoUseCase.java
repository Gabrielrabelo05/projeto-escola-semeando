package br.org.semeando.digital.application.cobranca;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.auth.EmissorConvite;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.CobrancaRepository;
import br.org.semeando.digital.domain.cobranca.FormaPagamento;
import br.org.semeando.digital.domain.cobranca.Pagamento;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.IdentidadeInstitucional;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.UsuarioRepository;
import java.time.LocalDate;
import java.util.UUID;

/**
 * RF-27 (pagamento manual), RF-28 (baixa automática via webhook do Cora) e RF-30 (recibo).
 * Quando a primeira parcela é paga, a matrícula e o aluno passam a ATIVO (passo 13 do fluxo).
 */
@CasoDeUso
public class ConfirmarPagamentoUseCase {

    private final CobrancaRepository cobrancas;
    private final MatriculaRepository matriculas;
    private final AlunoRepository alunos;
    private final ResponsavelRepository responsaveis;
    private final Notificador notificador;
    private final EmissorConvite convites;
    private final UsuarioRepository usuarios;
    private final Auditor auditor;

    public ConfirmarPagamentoUseCase(CobrancaRepository cobrancas, MatriculaRepository matriculas,
            AlunoRepository alunos, ResponsavelRepository responsaveis, Notificador notificador,
            EmissorConvite convites, UsuarioRepository usuarios, Auditor auditor) {
        this.cobrancas = cobrancas;
        this.matriculas = matriculas;
        this.alunos = alunos;
        this.responsaveis = responsaveis;
        this.notificador = notificador;
        this.convites = convites;
        this.usuarios = usuarios;
        this.auditor = auditor;
    }

    public Cobranca registrarManual(UUID cobrancaId, LocalDate pagoEm, Dinheiro valorPago, FormaPagamento forma,
            Ator ator) {
        Cobranca cobranca = cobrancas.buscarPorId(cobrancaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cobrança", cobrancaId));
        return confirmar(cobranca, new Pagamento(pagoEm, valorPago, forma, ator.nome()), ator);
    }

    public Cobranca processarWebhook(String idCora, LocalDate pagoEm, Dinheiro valorPago) {
        Cobranca cobranca = cobrancas.buscarPorIdCora(idCora)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Boleto Cora", idCora));
        return confirmar(cobranca, new Pagamento(pagoEm, valorPago, FormaPagamento.BOLETO, "webhook-cora"),
                Ator.sistema());
    }

    private Cobranca confirmar(Cobranca cobranca, Pagamento pagamento, Ator ator) {
        if (!cobranca.confirmarPagamento(pagamento)) {
            return cobranca;
        }
        cobrancas.salvar(cobranca);
        auditor.registrar(ator, AcaoAuditada.PAGAMENTO, "Cobranca", cobranca.id(),
                pagamento.forma() + " " + pagamento.valorPago());
        Aluno aluno = alunos.buscarPorId(cobranca.alunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", cobranca.alunoId()));
        ativarMatriculaSePrimeiraParcela(cobranca, aluno);
        enviarRecibo(cobranca, aluno);
        return cobranca;
    }

    private void ativarMatriculaSePrimeiraParcela(Cobranca cobranca, Aluno aluno) {
        matriculas.buscarPorPrimeiraParcela(cobranca.id()).ifPresent(matricula -> {
            matricula.ativar();
            matriculas.salvar(matricula);
            aluno.ativar();
            alunos.salvar(aluno);
            responsaveis.buscarPorId(aluno.responsavelId()).ifPresent(responsavel -> {
                boolean convidado = convidarParaOPortal(responsavel);
                notificador.responsavel(responsavel, "Matrícula confirmada",
                        "A matrícula de " + aluno.nome() + " está ATIVA. Acesse o portal do responsável com o seu "
                                + "e-mail para acompanhar boletos, presença, documentos e lista de materiais."
                                + (convidado ? " Você vai receber um link para criar a sua senha." : ""));
            });
        });
    }

    /** Responsável sem acesso recebe o link para criar a senha do portal (passo 15 do fluxo). */
    private boolean convidarParaOPortal(Responsavel responsavel) {
        String email = responsavel.contato().email();
        if (email == null || email.isBlank() || usuarios.buscarPorEmail(email.trim().toLowerCase()).isPresent()) {
            return false;
        }
        convites.convidar(responsavel.nome(), email, Perfil.RESPONSAVEL, responsavel.cpf());
        return true;
    }

    /** RF-30: recibo enviado por e-mail após a confirmação do pagamento. */
    private void enviarRecibo(Cobranca cobranca, Aluno aluno) {
        responsaveis.buscarPorId(aluno.responsavelId()).ifPresent(responsavel -> notificador.responsavel(
                responsavel, "Recibo de pagamento",
                """
                RECIBO - %s
                Recebemos de %s (CPF %s) o valor de %s referente a %s %s do(a) aluno(a) %s.
                Pagamento em %s via %s.
                """.formatted(IdentidadeInstitucional.cabecalho(), responsavel.nome(), responsavel.cpf(),
                        cobranca.pagamento().valorPago(), cobranca.tipo().descricao(), cobranca.mesReferencia(),
                        aluno.nome(),
                        cobranca.pagamento().pagoEm(), cobranca.pagamento().forma())));
    }
}
