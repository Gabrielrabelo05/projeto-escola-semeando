import type { ConviteAcesso } from "../models/auth";
import type {
  AlunoForm,
  Funcionario,
  FuncionarioForm,
  Prestador,
  PrestadorForm,
  Professor,
  ProfessorForm,
  RegistroAuditoria,
  ResponsavelForm,
  ResultadoBusca,
  TurmaForm,
} from "../models/cadastro";
import type {
  AlunoDetalhe,
  AlunoResumo,
  AtividadeExtracurricular,
  StatusAluno,
  Turma,
  UUID,
} from "../models/comum";

export interface CadastroGateway {
  alunos(status?: StatusAluno): Promise<AlunoResumo[]>;
  aluno(id: UUID): Promise<AlunoDetalhe>;
  inativarAluno(id: UUID): Promise<AlunoResumo>;
  atualizarAluno(id: UUID, dados: AlunoForm): Promise<AlunoDetalhe>;
  atualizarResponsavel(
    alunoId: UUID,
    dados: ResponsavelForm,
  ): Promise<AlunoDetalhe>;
  acessoResponsavel(alunoId: UUID): Promise<ConviteAcesso>;
  definirAtividades(
    id: UUID,
    atividades: AtividadeExtracurricular[],
  ): Promise<AlunoDetalhe>;

  professores(incluirInativos?: boolean): Promise<Professor[]>;
  salvarProfessor(dados: ProfessorForm, id?: UUID): Promise<Professor>;
  inativarProfessor(id: UUID): Promise<Professor>;

  funcionarios(incluirInativos?: boolean): Promise<Funcionario[]>;
  salvarFuncionario(dados: FuncionarioForm, id?: UUID): Promise<Funcionario>;
  inativarFuncionario(id: UUID): Promise<Funcionario>;

  prestadores(incluirInativos?: boolean): Promise<Prestador[]>;
  salvarPrestador(dados: PrestadorForm, id?: UUID): Promise<Prestador>;
  inativarPrestador(id: UUID): Promise<Prestador>;

  turmas(anoLetivo?: number): Promise<Turma[]>;
  salvarTurma(dados: TurmaForm, id?: UUID): Promise<Turma>;

  buscar(termo: string): Promise<ResultadoBusca[]>;
  auditoria(filtro: {
    entidade?: string;
    entidadeId?: string;
  }): Promise<RegistroAuditoria[]>;
}
