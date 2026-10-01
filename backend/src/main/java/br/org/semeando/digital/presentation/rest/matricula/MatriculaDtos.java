package br.org.semeando.digital.presentation.rest.matricula;

import br.org.semeando.digital.application.matricula.ConsultarMatriculasUseCase.MatriculaDetalhada;
import br.org.semeando.digital.application.matricula.ConsultarMatriculasUseCase.OcupacaoTurma;
import br.org.semeando.digital.application.matricula.ConsultarMatriculasUseCase.RelatorioMatriculas;
import br.org.semeando.digital.application.matricula.PreMatriculaCommand;
import br.org.semeando.digital.domain.aluno.InformacoesMedicas;
import br.org.semeando.digital.domain.matricula.DocumentoMatricula;
import br.org.semeando.digital.domain.matricula.Farda;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.TipoDocumento;
import br.org.semeando.digital.presentation.rest.shared.Dtos.AlunoResumo;
import br.org.semeando.digital.presentation.rest.shared.Dtos.AutorizadoDto;
import br.org.semeando.digital.presentation.rest.shared.Dtos.TurmaDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MatriculaDtos {

    private MatriculaDtos() {
    }

    public record PreMatriculaRequest(@NotNull @Valid AlunoRequest aluno, @NotNull @Valid ResponsavelRequest responsavel,
            @NotNull UUID turmaId) {

        PreMatriculaCommand paraComando() {
            return new PreMatriculaCommand(
                    new PreMatriculaCommand.DadosAluno(aluno.nome(), aluno.dataNascimento(),
                            new InformacoesMedicas(aluno.alergias(), aluno.tipoSanguineo(),
                                    aluno.necessidadesEspeciais()),
                            aluno.hospitalEmergencia(),
                            aluno.autorizados().stream().map(AutorizadoDto::paraDominio).toList()),
                    new PreMatriculaCommand.DadosResponsavel(responsavel.nome(), responsavel.cpf(), responsavel.email(),
                            responsavel.telefone(), responsavel.parentesco()),
                    turmaId);
        }
    }

    public record AlunoRequest(@NotBlank String nome, @NotNull @Past LocalDate dataNascimento, String alergias,
            String tipoSanguineo, String necessidadesEspeciais, @NotBlank String hospitalEmergencia,
            @NotEmpty List<@Valid AutorizadoDto> autorizados) {
    }

    public record ResponsavelRequest(@NotBlank String nome, @NotBlank String cpf, @NotBlank @Email String email,
            @NotBlank String telefone, @NotBlank String parentesco) {
    }

    public record RejeicaoRequest(@NotBlank String justificativa) {
    }

    public record FardaRequest(@NotBlank String blusa, @NotBlank String calca, @NotBlank String tenis) {

        Farda paraDominio() {
            return new Farda(blusa, calca, tenis);
        }
    }

    public record RenovacaoRequest(@NotNull UUID alunoId, @NotNull UUID turmaId) {
    }

    public record TransferenciaRequest(@NotNull UUID turmaId) {
    }

    public record DocumentoResponse(TipoDocumento tipo, String descricao, boolean entregue, LocalDate entregueEm,
            boolean pendente) {

        static DocumentoResponse de(DocumentoMatricula d, LocalDate hoje) {
            return new DocumentoResponse(d.tipo(), d.tipo().descricao(), d.entregue(), d.entregueEm(),
                    !d.vigenteEm(hoje));
        }
    }

    public record MatriculaResponse(UUID id, UUID alunoId, UUID turmaId, String nivel, int anoLetivo, String tipo,
            String status, LocalDate dataSolicitacao, String justificativaRejeicao, FardaRequest farda,
            List<DocumentoResponse> documentos, UUID cobrancaTaxaId, AlunoResumo aluno, TurmaDto turma) {

        static MatriculaResponse de(Matricula m, Clock clock) {
            return de(m, null, null, clock);
        }

        static MatriculaResponse de(MatriculaDetalhada d, Clock clock) {
            return de(d.matricula(), AlunoResumo.de(d.aluno()), TurmaDto.de(d.turma()), clock);
        }

        private static MatriculaResponse de(Matricula m, AlunoResumo aluno, TurmaDto turma, Clock clock) {
            LocalDate hoje = LocalDate.now(clock);
            FardaRequest farda = m.farda() == null ? null
                    : new FardaRequest(m.farda().blusa(), m.farda().calca(), m.farda().tenis());
            return new MatriculaResponse(m.id(), m.alunoId(), m.turmaId(), m.nivel().name(), m.anoLetivo(),
                    m.tipo().name(), m.status().name(), m.dataSolicitacao(), m.justificativaRejeicao(), farda,
                    m.documentos().stream().map(d -> DocumentoResponse.de(d, hoje)).toList(), m.cobrancaTaxaId(),
                    aluno, turma);
        }
    }

    public record OcupacaoResponse(TurmaDto turma, long ocupadas, long vagas) {

        static OcupacaoResponse de(OcupacaoTurma o) {
            return new OcupacaoResponse(TurmaDto.de(o.turma()), o.ocupadas(), o.vagas());
        }
    }

    public record RelatorioResponse(int anoLetivo, List<OcupacaoResponse> turmas, Map<String, Long> porNivel,
            Map<String, Long> porStatus) {

        static RelatorioResponse de(RelatorioMatriculas r) {
            Map<String, Long> niveis = new LinkedHashMap<>();
            r.porNivel().forEach((nivel, total) -> niveis.put(nivel.name(), total));
            Map<String, Long> status = new LinkedHashMap<>();
            r.porStatus().forEach((s, total) -> status.put(s.name(), total));
            return new RelatorioResponse(r.anoLetivo(), r.turmas().stream().map(OcupacaoResponse::de).toList(),
                    niveis, status);
        }
    }
}
