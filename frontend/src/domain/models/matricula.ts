import type { AlunoResumo, Autorizado, DataISO, Turma, UUID } from "./comum";

export type StatusMatricula =
  | "AGUARDANDO_ANALISE"
  | "REJEITADA"
  | "APROVADA"
  | "ATIVA"
  | "TRANSFERIDA"
  | "CANCELADA"
  | "CONCLUIDA";

/** Checklist do Anexo didático da escola. */
export type TipoDocumento =
  | "DOCUMENTO_RESPONSAVEL"
  | "DOCUMENTO_ALUNO"
  | "CARTEIRA_VACINACAO"
  | "COMPROVANTE_RESIDENCIA"
  | "DECLARACAO_ADIMPLENCIA"
  | "FOTOS_3X4"
  | "DOCUMENTO_AUTORIZADOS";

export interface DocumentoMatricula {
  tipo: TipoDocumento;
  descricao: string;
  entregue: boolean;
  entregueEm: DataISO | null;
  pendente: boolean;
}

export interface Farda {
  blusa: string;
  calca: string;
  tenis: string;
}

export interface Matricula {
  id: UUID;
  alunoId: UUID;
  turmaId: UUID;
  nivel: string;
  anoLetivo: number;
  tipo: "NOVA" | "RENOVACAO";
  status: StatusMatricula;
  dataSolicitacao: DataISO;
  justificativaRejeicao: string | null;
  farda: Farda | null;
  documentos: DocumentoMatricula[];
  /** Primeira parcela: o pagamento confirma a matrícula (sinal do contrato). */
  primeiraParcelaId: UUID | null;
  aluno: AlunoResumo | null;
  turma: Turma | null;
}

export interface PreMatricula {
  aluno: {
    nome: string;
    dataNascimento: DataISO;
    alergias?: string;
    tipoSanguineo?: string;
    necessidadesEspeciais?: string;
    hospitalEmergencia: string;
    autorizados: Autorizado[];
  };
  responsavel: {
    nome: string;
    cpf: string;
    email: string;
    telefone: string;
    parentesco: string;
  };
  turmaId: UUID;
  /** Inclui a declaração de adimplência no checklist. */
  vindoDeEscolaParticular?: boolean;
}

export interface OcupacaoTurma {
  turma: Turma;
  ocupadas: number;
  vagas: number;
}

export interface RelatorioMatriculas {
  anoLetivo: number;
  turmas: OcupacaoTurma[];
  porNivel: Record<string, number>;
  porStatus: Record<string, number>;
}
