import type { DataISO, UUID } from "./comum";

export type StatusPlanoAula =
  | "RASCUNHO"
  | "SUBMETIDO"
  | "APROVADO"
  | "DEVOLVIDO";

export interface PlanoAula {
  id: UUID;
  professorId: UUID;
  turmaId: UUID;
  data: DataISO;
  objetivo: string;
  conteudo: string;
  metodologia: string;
  avaliacao: string | null;
  recursos: string | null;
  status: StatusPlanoAula;
  comentarioCoordenacao: string | null;
}

export type PlanoAulaForm = Pick<
  PlanoAula,
  | "turmaId"
  | "data"
  | "objetivo"
  | "conteudo"
  | "metodologia"
  | "avaliacao"
  | "recursos"
>;

export interface Presenca {
  alunoId: UUID;
  presente: boolean;
}

export interface DiarioClasse {
  id: UUID;
  turmaId: UUID;
  data: DataISO;
  conteudoMinistrado: string;
  presencas: Presenca[];
}

export type DiarioForm = Omit<DiarioClasse, "id">;

export interface Frequencia {
  alunoId: UUID;
  nomeAluno: string | null;
  aulas: number;
  presencas: number;
  faltas: number;
  percentual: number;
  alerta: boolean;
}

export interface Planejamento {
  id: UUID;
  professorId: UUID;
  turmaId: UUID;
  data: DataISO;
  roteiro: string;
  comentarioCoordenacao: string | null;
}

export type TipoEvento =
  | "INICIO_ANO_LETIVO"
  | "FIM_ANO_LETIVO"
  | "FERIADO"
  | "EVENTO"
  | "REUNIAO_PAIS"
  | "FIM_BIMESTRE";

export interface EventoCalendario {
  id: UUID;
  titulo: string;
  tipo: TipoEvento;
  inicio: DataISO;
  fim: DataISO;
}

export type EventoForm = Omit<EventoCalendario, "id">;

export interface Formacao {
  id: UUID;
  professorId: UUID;
  tema: string;
  data: DataISO;
  cargaHoraria: number;
  instituicao: string;
}

export type FormacaoForm = Omit<Formacao, "id" | "professorId"> & {
  professorId?: UUID;
};
