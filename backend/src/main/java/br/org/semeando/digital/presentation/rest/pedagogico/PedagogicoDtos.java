package br.org.semeando.digital.presentation.rest.pedagogico;

import br.org.semeando.digital.application.pedagogico.DiarioClasseUseCase.FrequenciaAluno;
import br.org.semeando.digital.domain.pedagogico.ConteudoPlano;
import br.org.semeando.digital.domain.pedagogico.DiarioClasse;
import br.org.semeando.digital.domain.pedagogico.EventoCalendario;
import br.org.semeando.digital.domain.pedagogico.FormacaoContinuada;
import br.org.semeando.digital.domain.pedagogico.Frequencia;
import br.org.semeando.digital.domain.pedagogico.PlanejamentoDiario;
import br.org.semeando.digital.domain.pedagogico.PlanoAula;
import br.org.semeando.digital.domain.pedagogico.Presenca;
import br.org.semeando.digital.domain.pedagogico.TipoEvento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class PedagogicoDtos {

    private PedagogicoDtos() {
    }

    public record PlanoAulaRequest(@NotNull UUID turmaId, @NotNull LocalDate data, @NotBlank String objetivo,
            @NotBlank String conteudo, @NotBlank String metodologia, String avaliacao, String recursos) {

        ConteudoPlano conteudoPlano() {
            return new ConteudoPlano(objetivo, conteudo, metodologia, avaliacao, recursos);
        }
    }

    public record ComentarioRequest(String comentario) {
    }

    public record PlanoAulaResponse(UUID id, UUID professorId, UUID turmaId, LocalDate data, String objetivo,
            String conteudo, String metodologia, String avaliacao, String recursos, String status,
            String comentarioCoordenacao) {

        static PlanoAulaResponse de(PlanoAula p) {
            var c = p.conteudo();
            return new PlanoAulaResponse(p.id(), p.professorId(), p.turmaId(), p.data(), c.objetivo(), c.conteudo(),
                    c.metodologia(), c.avaliacao(), c.recursos(), p.status().name(), p.comentarioCoordenacao());
        }
    }

    public record PresencaDto(@NotNull UUID alunoId, boolean presente) {

        Presenca paraDominio() {
            return new Presenca(alunoId, presente);
        }
    }

    public record DiarioRequest(@NotNull UUID turmaId, @NotNull LocalDate data, @NotBlank String conteudoMinistrado,
            @NotEmpty List<@Valid PresencaDto> presencas) {
    }

    public record DiarioResponse(UUID id, UUID turmaId, LocalDate data, String conteudoMinistrado,
            List<PresencaDto> presencas) {

        static DiarioResponse de(DiarioClasse d) {
            return new DiarioResponse(d.id(), d.turmaId(), d.data(), d.conteudoMinistrado(),
                    d.presencas().stream().map(p -> new PresencaDto(p.alunoId(), p.presente())).toList());
        }
    }

    public record FrequenciaResponse(UUID alunoId, String nomeAluno, int aulas, int presencas, int faltas,
            BigDecimal percentual, boolean alerta) {

        static FrequenciaResponse de(String nome, Frequencia f) {
            return new FrequenciaResponse(f.alunoId(), nome, f.aulas(), f.presencas(), f.faltas(), f.percentual(),
                    f.emAlerta());
        }

        static FrequenciaResponse de(FrequenciaAluno f) {
            return de(f.nomeAluno(), f.frequencia());
        }
    }

    public record PlanejamentoRequest(@NotNull UUID turmaId, @NotNull LocalDate data, @NotBlank String roteiro) {
    }

    public record PlanejamentoResponse(UUID id, UUID professorId, UUID turmaId, LocalDate data, String roteiro,
            String comentarioCoordenacao) {

        static PlanejamentoResponse de(PlanejamentoDiario p) {
            return new PlanejamentoResponse(p.id(), p.professorId(), p.turmaId(), p.data(), p.roteiro(),
                    p.comentarioCoordenacao());
        }
    }

    public record EventoRequest(@NotBlank String titulo, @NotNull TipoEvento tipo, @NotNull LocalDate inicio,
            LocalDate fim) {
    }

    public record EventoResponse(UUID id, String titulo, TipoEvento tipo, LocalDate inicio, LocalDate fim) {

        static EventoResponse de(EventoCalendario e) {
            return new EventoResponse(e.id(), e.titulo(), e.tipo(), e.inicio(), e.fim());
        }
    }

    public record FormacaoRequest(UUID professorId, @NotBlank String tema, @NotNull LocalDate data,
            @Min(1) int cargaHoraria, @NotBlank String instituicao) {
    }

    public record FormacaoResponse(UUID id, UUID professorId, String tema, LocalDate data, int cargaHoraria,
            String instituicao) {

        static FormacaoResponse de(FormacaoContinuada f) {
            return new FormacaoResponse(f.id(), f.professorId(), f.tema(), f.data(), f.cargaHoraria(), f.instituicao());
        }
    }
}
