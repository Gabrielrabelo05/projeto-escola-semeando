package br.org.semeando.digital.presentation.rest.cadastro;

import br.org.semeando.digital.application.cadastro.AlunosUseCase.AtualizarAlunoCommand;
import br.org.semeando.digital.application.cadastro.FuncionariosUseCase.DadosFuncionario;
import br.org.semeando.digital.application.cadastro.PrestadoresUseCase.DadosPrestador;
import br.org.semeando.digital.application.cadastro.ProfessoresUseCase.DadosProfessor;
import br.org.semeando.digital.domain.aluno.AtividadeExtracurricular;
import br.org.semeando.digital.domain.aluno.InformacoesMedicas;
import br.org.semeando.digital.domain.auditoria.RegistroAuditoria;
import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.Funcionario;
import br.org.semeando.digital.domain.pessoa.PrestadorServico;
import br.org.semeando.digital.domain.pessoa.Professor;
import br.org.semeando.digital.domain.turma.Serie;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.presentation.rest.shared.Dtos.AutorizadoDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CadastroDtos {

    private CadastroDtos() {
    }

    public record AtualizarAlunoRequest(@NotBlank String nome, @NotNull LocalDate dataNascimento, String alergias,
            String tipoSanguineo, String necessidadesEspeciais, String hospitalEmergencia,
            List<@Valid AutorizadoDto> autorizados, String fotoUrl) {

        AtualizarAlunoCommand paraComando() {
            return new AtualizarAlunoCommand(nome, dataNascimento,
                    new InformacoesMedicas(alergias, tipoSanguineo, necessidadesEspeciais), hospitalEmergencia,
                    autorizados == null ? List.of() : autorizados.stream().map(AutorizadoDto::paraDominio).toList(),
                    fotoUrl);
        }
    }

    public record AtividadesRequest(@NotNull Set<AtividadeExtracurricular> atividades) {
    }

    public record ProfessorRequest(@NotBlank String nome, @NotBlank String cpf, @Email String email, String telefone,
            String rg, String formacaoAcademica, List<String> disciplinas, @Min(0) int cargaHorariaSemanal) {

        DadosProfessor paraDados() {
            return new DadosProfessor(nome, cpf, new Contato(email, telefone), rg, formacaoAcademica, disciplinas,
                    cargaHorariaSemanal);
        }
    }

    public record ProfessorResponse(UUID id, String nome, String cpf, String email, String telefone, String rg,
            String formacaoAcademica, List<String> disciplinas, int cargaHorariaSemanal, boolean ativo) {

        static ProfessorResponse de(Professor p) {
            return new ProfessorResponse(p.id(), p.nome(), p.cpf().formatado(), p.contato().email(),
                    p.contato().telefone(), p.rg(), p.formacaoAcademica(), p.disciplinas(), p.cargaHorariaSemanal(),
                    p.ativo());
        }
    }

    public record FuncionarioRequest(@NotBlank String nome, @NotBlank String cpf, @Email String email,
            String telefone, @NotBlank String cargo, @NotBlank String setor, @NotNull Perfil perfilAcesso) {

        DadosFuncionario paraDados() {
            return new DadosFuncionario(nome, cpf, new Contato(email, telefone), cargo, setor, perfilAcesso);
        }
    }

    public record FuncionarioResponse(UUID id, String nome, String cpf, String email, String telefone, String cargo,
            String setor, Perfil perfilAcesso, boolean ativo) {

        static FuncionarioResponse de(Funcionario f) {
            return new FuncionarioResponse(f.id(), f.nome(), f.cpf().formatado(), f.contato().email(),
                    f.contato().telefone(), f.cargo(), f.setor(), f.perfilAcesso(), f.ativo());
        }
    }

    public record PrestadorRequest(@NotBlank String nome, @NotBlank String documento, @NotBlank String servicoPrestado,
            @NotNull LocalDate vigenciaInicio, @NotNull LocalDate vigenciaFim, List<String> documentosObrigatorios,
            @Email String email, String telefone) {

        DadosPrestador paraDados() {
            return new DadosPrestador(nome, documento, servicoPrestado, vigenciaInicio, vigenciaFim,
                    documentosObrigatorios, new Contato(email, telefone));
        }
    }

    public record PrestadorResponse(UUID id, String nome, String documento, boolean pessoaJuridica,
            String servicoPrestado, LocalDate vigenciaInicio, LocalDate vigenciaFim, boolean contratoVigente,
            List<String> documentosObrigatorios, String email, String telefone, boolean ativo) {

        static PrestadorResponse de(PrestadorServico p, LocalDate hoje) {
            return new PrestadorResponse(p.id(), p.nome(), p.documento(), p.pessoaJuridica(), p.servicoPrestado(),
                    p.vigenciaInicio(), p.vigenciaFim(), p.contratoVigente(hoje), p.documentosObrigatorios(),
                    p.contato().email(), p.contato().telefone(), p.ativo());
        }
    }

    public record TurmaRequest(@NotBlank String nome, @NotNull Serie serie, @Min(2000) int anoLetivo,
            @Min(1) int capacidadeMaxima, UUID professorTitularId) {
    }

    public record AuditoriaResponse(UUID id, String usuario, String acao, String entidade, String entidadeId,
            String detalhes, Instant ocorridoEm) {

        static AuditoriaResponse de(RegistroAuditoria r) {
            return new AuditoriaResponse(r.id(), r.usuario(), r.acao().name(), r.entidade(), r.entidadeId(),
                    r.detalhes(), r.ocorridoEm());
        }
    }
}
