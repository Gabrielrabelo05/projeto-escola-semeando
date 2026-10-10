import type { CadastroGateway } from "@/domain/gateways";
import type { ConviteAcesso } from "@/domain/models/auth";
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
} from "@/domain/models/cadastro";
import type {
  AlunoDetalhe,
  AlunoResumo,
  AtividadeExtracurricular,
  StatusAluno,
  Turma,
  UUID,
} from "@/domain/models/comum";
import type { HttpClient } from "../http/HttpClient";

export class HttpCadastroGateway implements CadastroGateway {
  constructor(private readonly http: HttpClient) {}

  alunos(status?: StatusAluno): Promise<AlunoResumo[]> {
    return this.http.get("/alunos", { status });
  }

  aluno(id: UUID): Promise<AlunoDetalhe> {
    return this.http.get(`/alunos/${id}`);
  }

  inativarAluno(id: UUID): Promise<AlunoResumo> {
    return this.http.patch(`/alunos/${id}/inativar`);
  }

  atualizarAluno(id: UUID, dados: AlunoForm): Promise<AlunoDetalhe> {
    return this.http.put(`/alunos/${id}`, dados);
  }

  atualizarResponsavel(
    alunoId: UUID,
    dados: ResponsavelForm,
  ): Promise<AlunoDetalhe> {
    return this.http.put(`/alunos/${alunoId}/responsavel`, dados);
  }

  acessoResponsavel(alunoId: UUID): Promise<ConviteAcesso> {
    return this.http.post(`/alunos/${alunoId}/acesso-responsavel`);
  }

  definirAtividades(
    id: UUID,
    atividades: AtividadeExtracurricular[],
  ): Promise<AlunoDetalhe> {
    return this.http.put(`/alunos/${id}/atividades`, { atividades });
  }

  professores(incluirInativos = false): Promise<Professor[]> {
    return this.http.get("/professores", { incluirInativos });
  }

  salvarProfessor(dados: ProfessorForm, id?: UUID): Promise<Professor> {
    return id
      ? this.http.put(`/professores/${id}`, dados)
      : this.http.post("/professores", dados);
  }

  inativarProfessor(id: UUID): Promise<Professor> {
    return this.http.patch(`/professores/${id}/inativar`);
  }

  funcionarios(incluirInativos = false): Promise<Funcionario[]> {
    return this.http.get("/funcionarios", { incluirInativos });
  }

  salvarFuncionario(dados: FuncionarioForm, id?: UUID): Promise<Funcionario> {
    return id
      ? this.http.put(`/funcionarios/${id}`, dados)
      : this.http.post("/funcionarios", dados);
  }

  inativarFuncionario(id: UUID): Promise<Funcionario> {
    return this.http.patch(`/funcionarios/${id}/inativar`);
  }

  prestadores(incluirInativos = false): Promise<Prestador[]> {
    return this.http.get("/prestadores", { incluirInativos });
  }

  salvarPrestador(dados: PrestadorForm, id?: UUID): Promise<Prestador> {
    return id
      ? this.http.put(`/prestadores/${id}`, dados)
      : this.http.post("/prestadores", dados);
  }

  inativarPrestador(id: UUID): Promise<Prestador> {
    return this.http.patch(`/prestadores/${id}/inativar`);
  }

  turmas(anoLetivo?: number): Promise<Turma[]> {
    return this.http.get("/turmas", { anoLetivo });
  }

  salvarTurma(dados: TurmaForm, id?: UUID): Promise<Turma> {
    return id
      ? this.http.put(`/turmas/${id}`, dados)
      : this.http.post("/turmas", dados);
  }

  buscar(termo: string): Promise<ResultadoBusca[]> {
    return this.http.get("/busca", { termo });
  }

  auditoria(filtro: {
    entidade?: string;
    entidadeId?: string;
  }): Promise<RegistroAuditoria[]> {
    return this.http.get("/auditoria", filtro);
  }
}
