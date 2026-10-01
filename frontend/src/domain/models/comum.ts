export type UUID = string;
export type DataISO = string;

export type Perfil =
  | "ADMIN"
  | "DIRETOR"
  | "SECRETARIA"
  | "FINANCEIRO"
  | "PROFESSOR"
  | "RESPONSAVEL"
  | "PRESTADOR";

export type Serie =
  | "MATERNAL_I"
  | "MATERNAL_II"
  | "INFANTIL_1"
  | "INFANTIL_2"
  | "PRIMEIRO_ANO"
  | "SEGUNDO_ANO"
  | "TERCEIRO_ANO"
  | "QUARTO_ANO"
  | "QUINTO_ANO";

export type NivelEnsino = "EDUCACAO_INFANTIL" | "ENSINO_FUNDAMENTAL";

export const SERIES: { valor: Serie; descricao: string; nivel: NivelEnsino }[] =
  [
    {
      valor: "MATERNAL_I",
      descricao: "Maternal I",
      nivel: "EDUCACAO_INFANTIL",
    },
    {
      valor: "MATERNAL_II",
      descricao: "Maternal II",
      nivel: "EDUCACAO_INFANTIL",
    },
    {
      valor: "INFANTIL_1",
      descricao: "Infantil 1",
      nivel: "EDUCACAO_INFANTIL",
    },
    {
      valor: "INFANTIL_2",
      descricao: "Infantil 2",
      nivel: "EDUCACAO_INFANTIL",
    },
    { valor: "PRIMEIRO_ANO", descricao: "1º ano", nivel: "ENSINO_FUNDAMENTAL" },
    { valor: "SEGUNDO_ANO", descricao: "2º ano", nivel: "ENSINO_FUNDAMENTAL" },
    { valor: "TERCEIRO_ANO", descricao: "3º ano", nivel: "ENSINO_FUNDAMENTAL" },
    { valor: "QUARTO_ANO", descricao: "4º ano", nivel: "ENSINO_FUNDAMENTAL" },
    { valor: "QUINTO_ANO", descricao: "5º ano", nivel: "ENSINO_FUNDAMENTAL" },
  ];

export interface Turma {
  id: UUID;
  nome: string;
  serie: Serie;
  serieDescricao: string;
  nivel: NivelEnsino;
  anoLetivo: number;
  capacidadeMaxima: number;
  professorTitularId: UUID | null;
}

export interface Autorizado {
  nome: string;
  documento: string;
  parentesco: string;
  telefone?: string | null;
}

export interface AlunoResumo {
  id: UUID;
  nome: string;
  dataNascimento: DataISO;
  status: StatusAluno;
}

export type StatusAluno =
  | "PRE_MATRICULA"
  | "ATIVO"
  | "TRANSFERIDO"
  | "CANCELADO"
  | "INATIVO";

export interface Responsavel {
  id: UUID;
  nome: string;
  cpf: string;
  email: string | null;
  telefone: string | null;
  parentesco: string;
}

export interface AlunoDetalhe {
  id: UUID;
  nome: string;
  dataNascimento: DataISO;
  status: StatusAluno;
  cpfResponsavel: string;
  alergias: string | null;
  tipoSanguineo: string | null;
  necessidadesEspeciais: string | null;
  hospitalEmergencia: string | null;
  autorizados: Autorizado[];
  fotoUrl: string | null;
  responsavel: Responsavel | null;
}

export interface ArquivoBaixado {
  nome: string;
  conteudo: Blob;
}
