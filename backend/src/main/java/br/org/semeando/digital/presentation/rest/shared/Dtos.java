package br.org.semeando.digital.presentation.rest.shared;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AtividadeExtracurricular;
import br.org.semeando.digital.domain.aluno.PessoaAutorizada;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.turma.Turma;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Representações compartilhadas entre controllers. */
public final class Dtos {

    private Dtos() {
    }

    public record AutorizadoDto(String nome, String documento, String parentesco, String telefone) {

        public static AutorizadoDto de(PessoaAutorizada a) {
            return new AutorizadoDto(a.nome(), a.documento(), a.parentesco(), a.telefone());
        }

        public PessoaAutorizada paraDominio() {
            return new PessoaAutorizada(nome, documento, parentesco, telefone);
        }
    }

    public record AlunoResumo(UUID id, String nome, LocalDate dataNascimento, String status) {

        public static AlunoResumo de(Aluno a) {
            return a == null ? null : new AlunoResumo(a.id(), a.nome(), a.dataNascimento(), a.status().name());
        }
    }

    public record AlunoDetalhe(UUID id, String nome, LocalDate dataNascimento, String status, String cpfResponsavel,
            String alergias, String tipoSanguineo, String necessidadesEspeciais, String hospitalEmergencia,
            List<AutorizadoDto> autorizados, String fotoUrl, ResponsavelDto responsavel,
            List<AtividadeExtracurricular> atividades) {

        public static AlunoDetalhe de(Aluno a, Responsavel r) {
            var med = a.informacoesMedicas();
            return new AlunoDetalhe(a.id(), a.nome(), a.dataNascimento(), a.status().name(),
                    a.cpfResponsavel().formatado(), med.alergias(), med.tipoSanguineo(), med.necessidadesEspeciais(),
                    a.hospitalEmergencia(), a.autorizados().stream().map(AutorizadoDto::de).toList(), a.fotoUrl(),
                    r == null ? null : ResponsavelDto.de(r), a.atividades().stream().sorted().toList());
        }
    }

    public record ResponsavelDto(UUID id, String nome, String cpf, String email, String telefone, String parentesco) {

        public static ResponsavelDto de(Responsavel r) {
            return new ResponsavelDto(r.id(), r.nome(), r.cpf().formatado(), r.contato().email(),
                    r.contato().telefone(), r.parentesco());
        }
    }

    public record TurmaDto(UUID id, String nome, String serie, String serieDescricao, String nivel, int anoLetivo,
            int capacidadeMaxima, UUID professorTitularId) {

        public static TurmaDto de(Turma t) {
            return t == null ? null : new TurmaDto(t.id(), t.nome(), t.serie().name(), t.serie().descricao(),
                    t.nivel().name(), t.anoLetivo(), t.capacidadeMaxima(), t.professorTitularId());
        }
    }

    public static BigDecimal valor(Dinheiro dinheiro) {
        return dinheiro == null ? null : dinheiro.valor();
    }
}
