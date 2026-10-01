import type { AlunoResumo, Autorizado, DataISO, Turma, UUID } from "./comum";

export type StatusMatricula =
  | "AGUARDANDO_ANALISE"
  | "REJEITADA"
  | "APROVADA"
  | "ATIVA"
  | "TRANSFERIDA"
  | "CANCELADA"
  | "CONCLUIDA";

export type TipoDocumento =
  | "CERTIDAO_NASCIMENTO"
  | "DOCUMENTO_RESPONSAVEL"
  | "COMPROVANTE_RESIDENCIA"
  | "CARTAO_VACINACAO"
  | "FOTO_3X4"
  | "CARTAO_SUS"
  | "LAUDO_MEDICO"
  | "DECLARACAO_TRANSFERENCIA"
  | "HISTORICO_ESCOLAR";

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
  cobrancaTaxaId: UUID | null;
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
