import type { AlunoResumo, Turma, UUID } from "./comum";

export interface Filho {
  aluno: AlunoResumo;
  matriculaId: UUID | null;
  statusMatricula: string | null;
  turma: Turma | null;
  aulas: number;
  faltas: number;
  percentualPresenca: number;
  alertaFrequencia: boolean;
}

export interface Notificacao {
  id: UUID;
  perfilDestino: string;
  titulo: string;
  mensagem: string;
  criadaEm: string;
  lida: boolean;
}
