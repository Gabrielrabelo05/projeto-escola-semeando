import type { Turma, UUID } from "../models/comum";
import type { Contrato } from "../models/contrato";
import type {
  Farda,
  Matricula,
  OcupacaoTurma,
  PreMatricula,
  RelatorioMatriculas,
  StatusMatricula,
  TipoDocumento,
} from "../models/matricula";

export interface MatriculaGateway {
  turmasAbertas(anoLetivo?: number): Promise<Turma[]>;
  solicitarPreMatricula(dados: PreMatricula): Promise<Matricula>;
  listar(filtro: {
    status?: StatusMatricula;
    anoLetivo?: number;
  }): Promise<Matricula[]>;
  detalhar(id: UUID): Promise<Matricula>;
  aprovar(id: UUID): Promise<Contrato>;
  rejeitar(id: UUID, justificativa: string): Promise<Matricula>;
  registrarFarda(id: UUID, farda: Farda): Promise<Matricula>;
  enviarDocumento(
    id: UUID,
    tipo: TipoDocumento,
    arquivo: File,
  ): Promise<Matricula>;
  notificarPendencias(id: UUID): Promise<void>;
  transferir(id: UUID, turmaId: UUID): Promise<Matricula>;
  cancelar(id: UUID): Promise<Matricula>;
  renovar(alunoId: UUID, turmaId: UUID): Promise<Matricula>;
  historico(alunoId: UUID): Promise<Matricula[]>;
  ocupacao(anoLetivo: number): Promise<OcupacaoTurma[]>;
  relatorio(anoLetivo: number): Promise<RelatorioMatriculas>;
}
