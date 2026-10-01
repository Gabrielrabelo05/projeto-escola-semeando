import type { MatriculaGateway } from "@/domain/gateways";
import type { Turma, UUID } from "@/domain/models/comum";
import type { Contrato } from "@/domain/models/contrato";
import type {
  Farda,
  Matricula,
  OcupacaoTurma,
  PreMatricula,
  RelatorioMatriculas,
  StatusMatricula,
  TipoDocumento,
} from "@/domain/models/matricula";
import type { HttpClient } from "../http/HttpClient";

export class HttpMatriculaGateway implements MatriculaGateway {
  constructor(private readonly http: HttpClient) {}

  turmasAbertas(anoLetivo?: number): Promise<Turma[]> {
    return this.http.get("/publico/turmas", { anoLetivo });
  }

  solicitarPreMatricula(dados: PreMatricula): Promise<Matricula> {
    return this.http.post("/pre-matriculas", dados);
  }

  listar(filtro: {
    status?: StatusMatricula;
    anoLetivo?: number;
  }): Promise<Matricula[]> {
    return this.http.get("/matriculas", filtro);
  }

  detalhar(id: UUID): Promise<Matricula> {
    return this.http.get(`/matriculas/${id}`);
  }

  aprovar(id: UUID): Promise<Contrato> {
    return this.http.post(`/matriculas/${id}/aprovar`);
  }

  rejeitar(id: UUID, justificativa: string): Promise<Matricula> {
    return this.http.post(`/matriculas/${id}/rejeitar`, { justificativa });
  }

  registrarFarda(id: UUID, farda: Farda): Promise<Matricula> {
    return this.http.put(`/matriculas/${id}/farda`, farda);
  }

  enviarDocumento(
    id: UUID,
    tipo: TipoDocumento,
    arquivo: File,
  ): Promise<Matricula> {
    return this.http.upload(
      `/matriculas/${id}/documentos/${tipo}`,
      "arquivo",
      arquivo,
    );
  }

  notificarPendencias(id: UUID): Promise<void> {
    return this.http.post(`/matriculas/${id}/notificar-pendencias`);
  }

  transferir(id: UUID, turmaId: UUID): Promise<Matricula> {
    return this.http.post(`/matriculas/${id}/transferir`, { turmaId });
  }

  cancelar(id: UUID): Promise<Matricula> {
    return this.http.post(`/matriculas/${id}/cancelar`);
  }

  renovar(alunoId: UUID, turmaId: UUID): Promise<Matricula> {
    return this.http.post("/matriculas/renovacoes", { alunoId, turmaId });
  }

  historico(alunoId: UUID): Promise<Matricula[]> {
    return this.http.get(`/alunos/${alunoId}/matriculas`);
  }

  ocupacao(anoLetivo: number): Promise<OcupacaoTurma[]> {
    return this.http.get("/turmas/ocupacao", { anoLetivo });
  }

  relatorio(anoLetivo: number): Promise<RelatorioMatriculas> {
    return this.http.get("/relatorios/matriculas", { anoLetivo });
  }
}
