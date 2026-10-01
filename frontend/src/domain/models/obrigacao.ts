import type { DataISO, UUID } from "./comum";

export type TipoObrigacao =
  | "SMTT"
  | "CENSO_ESCOLAR"
  | "SISTEMA_PRESENCA"
  | "OUTRA";
export type StatusObrigacao = "PENDENTE" | "EM_ANDAMENTO" | "CONCLUIDA";

export interface Obrigacao {
  id: UUID;
  tipo: TipoObrigacao;
  nome: string;
  anoReferencia: number;
  bimestre: number | null;
  prazo: DataISO;
  diasRestantes: number;
  atrasada: boolean;
  status: StatusObrigacao;
  responsavel: string;
  checklist: { descricao: string; concluido: boolean }[];
  comprovantes: {
    nomeArquivo: string;
    enviadoEm: DataISO;
    enviadoPor: string;
  }[];
  concluidaEm: DataISO | null;
}

export interface NovaObrigacao {
  tipo: TipoObrigacao;
  ano: number;
  bimestre?: number | null;
  nome?: string;
  prazo?: DataISO;
  responsavel: string;
  checklist?: string[];
}
