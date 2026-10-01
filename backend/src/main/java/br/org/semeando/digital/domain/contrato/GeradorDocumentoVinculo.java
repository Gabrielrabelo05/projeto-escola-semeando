package br.org.semeando.digital.domain.contrato;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.IdentidadeInstitucional;
import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço de domínio que decide e monta o documento de vínculo correto (RN-01),
 * validando valores (RN-02 / RF-17) e dados obrigatórios (RN-06).
 */
public class GeradorDocumentoVinculo {

    private final TabelaValores tabela;

    public GeradorDocumentoVinculo(TabelaValores tabela) {
        this.tabela = tabela;
    }

    public DocumentoVinculo gerar(Aluno aluno, Responsavel responsavel, Matricula matricula, Dinheiro valorParcela,
            int numeroParcelas, String geradoPor, Instant agora) {
        TipoVinculo tipo = TipoVinculo.para(matricula.nivel());
        UUID id = UUID.randomUUID();
        return switch (tipo) {
            case FICHA_SOCIO -> {
                String conteudo = conteudoFichaSocio(aluno, responsavel);
                yield new FichaSocio(dadosIniciais(id, aluno, matricula, geradoPor, agora, conteudo),
                        responsavel.nome(), responsavel.cpf(), true, tabela.taxaAssociadoInfantil());
            }
            case CONTRATO_PRESTACAO_SERVICOS -> {
                tabela.validarValoresContrato(valorParcela, numeroParcelas);
                if (!aluno.possuiDadosDeSeguranca()) {
                    throw new RegraNegocioVioladaException("RN-06",
                            "Contrato não gerado: informe o hospital de emergência e os autorizados a buscar a criança");
                }
                String conteudo = conteudoContrato(aluno, responsavel, valorParcela, numeroParcelas);
                yield new ContratoPrestacaoServicos(dadosIniciais(id, aluno, matricula, geradoPor, agora, conteudo),
                        valorParcela, numeroParcelas, aluno.hospitalEmergencia(), aluno.autorizados());
            }
        };
    }

    /** Gera com os valores da tabela oficial (fluxo padrão de aprovação de matrícula). */
    public DocumentoVinculo gerarComTabelaOficial(Aluno aluno, Responsavel responsavel, Matricula matricula,
            String geradoPor, Instant agora) {
        return gerar(aluno, responsavel, matricula, tabela.mensalidadeFundamental(), tabela.parcelasFundamental(),
                geradoPor, agora);
    }

    private DadosVinculo dadosIniciais(UUID id, Aluno aluno, Matricula matricula, String geradoPor, Instant agora,
            String conteudo) {
        return new DadosVinculo(id, UUID.randomUUID(), 1, aluno.id(), matricula.id(), matricula.anoLetivo(), agora,
                geradoPor, conteudo, null, StatusDocumentoVinculo.AGUARDANDO_ASSINATURA, null);
    }

    private String conteudoContrato(Aluno aluno, Responsavel responsavel, Dinheiro valorParcela, int parcelas) {
        String autorizados = aluno.autorizados().stream()
                .map(a -> "  - " + a.nome() + " (" + a.parentesco() + ") doc. " + a.documento())
                .collect(Collectors.joining("\n"));
        return """
                CONTRATO DE PRESTAÇÃO DE SERVIÇOS EDUCACIONAIS
                CONTRATADA: %s
                CONTRATANTE: %s - CPF %s
                ALUNO(A): %s - nascido(a) em %s

                CLÁUSULA 1ª - VALORES: %d parcelas de %s, totalizando %s.
                CLÁUSULA 2ª - ENCARGOS: multa de 2%% e juros de 0,033%% ao dia sobre a parcela em atraso.
                CLÁUSULA 3ª - HOSPITAL DE EMERGÊNCIA: %s
                CLÁUSULA 4ª - PESSOAS AUTORIZADAS A BUSCAR A CRIANÇA:
                %s
                """.formatted(IdentidadeInstitucional.cabecalho(), responsavel.nome(), responsavel.cpf(), aluno.nome(),
                aluno.dataNascimento(), parcelas, valorParcela, valorParcela.multiplicar(parcelas),
                aluno.hospitalEmergencia(), autorizados);
    }

    private String conteudoFichaSocio(Aluno aluno, Responsavel responsavel) {
        return """
                FICHA DE SÓCIO - INSTITUTO EDUCACIONAL SEMEANDO
                INSTITUIÇÃO: %s
                SÓCIO(A): %s - CPF %s
                ALUNO(A) VINCULADO(A): %s
                TAXA DE ASSOCIADO MENSAL: %s
                O(A) sócio(a) declara conhecer e aceitar o Estatuto do Instituto Educacional Semeando.
                """.formatted(IdentidadeInstitucional.cabecalho(), responsavel.nome(), responsavel.cpf(), aluno.nome(),
                tabela.taxaAssociadoInfantil());
    }
}
