package br.org.semeando.digital.infrastructure.config;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.cobranca.EmissorCobranca;
import br.org.semeando.digital.application.matricula.MatriculaContexto;
import br.org.semeando.digital.application.pedagogico.ProfessorLogado;
import br.org.semeando.digital.application.port.CoraGateway;
import br.org.semeando.digital.application.port.NotificacaoGateway;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.auditoria.AuditoriaRepository;
import br.org.semeando.digital.domain.cobranca.CobrancaRepository;
import br.org.semeando.digital.domain.contrato.GeradorDocumentoVinculo;
import br.org.semeando.digital.domain.contrato.TabelaValores;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.notificacao.NotificacaoInternaRepository;
import br.org.semeando.digital.domain.pessoa.ProfessorRepository;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.IdentidadeInstitucional;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import br.org.semeando.digital.infrastructure.gateway.cora.CoraFakeGateway;
import br.org.semeando.digital.infrastructure.gateway.cora.CoraHttpGateway;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Composição dos objetos de domínio e aplicação que não são casos de uso. */
@Configuration
@EnableConfigurationProperties(SemeandoProperties.class)
public class AplicacaoConfig {

    public static final ZoneId FUSO_ESCOLA = ZoneId.of("America/Fortaleza");

    @Bean
    Clock clock() {
        return Clock.system(FUSO_ESCOLA);
    }

    /** RN-09: a aplicação não sobe se a identidade institucional configurada divergir da oficial. */
    @Bean
    TabelaValores tabelaValores(SemeandoProperties props) {
        IdentidadeInstitucional.validar(props.instituicao().nome(), props.instituicao().cnpj());
        var v = props.valores();
        return new TabelaValores(Dinheiro.de(v.mensalidadeFundamental()), v.parcelasFundamental(),
                Dinheiro.de(v.taxaAssociadoInfantil()), v.parcelasAssociado(), Dinheiro.de(v.taxaMatricula()),
                v.diaVencimento());
    }

    @Bean
    GeradorDocumentoVinculo geradorDocumentoVinculo(TabelaValores tabela) {
        return new GeradorDocumentoVinculo(tabela);
    }

    @Bean
    Auditor auditor(AuditoriaRepository repository, Clock clock) {
        return new Auditor(repository, clock);
    }

    @Bean
    Notificador notificador(NotificacaoGateway gateway, NotificacaoInternaRepository internas, Clock clock) {
        return new Notificador(gateway, internas, clock);
    }

    @Bean
    EmissorCobranca emissorCobranca(CoraGateway cora, CobrancaRepository cobrancas) {
        return new EmissorCobranca(cora, cobrancas);
    }

    @Bean
    MatriculaContexto matriculaContexto(MatriculaRepository matriculas, AlunoRepository alunos,
            TurmaRepository turmas, ResponsavelRepository responsaveis) {
        return new MatriculaContexto(matriculas, alunos, turmas, responsaveis);
    }

    @Bean
    ProfessorLogado professorLogado(ProfessorRepository professores) {
        return new ProfessorLogado(professores);
    }

    @Bean
    CoraGateway coraGateway(SemeandoProperties props) {
        var cora = props.cora();
        if (!"http".equalsIgnoreCase(cora.modo())) {
            return new CoraFakeGateway();
        }
        return new CoraHttpGateway(RestClient.builder()
                .baseUrl(cora.url())
                .defaultHeader("Authorization", "Bearer " + cora.token())
                .build());
    }
}
