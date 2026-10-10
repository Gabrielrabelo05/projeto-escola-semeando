import type { Autorizado, DataISO, Perfil, Serie, UUID } from "./comum";

export interface Professor {
  id: UUID;
  nome: string;
  cpf: string;
  email: string | null;
  telefone: string | null;
  rg: string | null;
  formacaoAcademica: string | null;
  disciplinas: string[];
  cargaHorariaSemanal: number;
  ativo: boolean;
}

export type ProfessorForm = Omit<Professor, "id" | "ativo">;

export interface Funcionario {
  id: UUID;
  nome: string;
  cpf: string;
  email: string | null;
  telefone: string | null;
  cargo: string;
  setor: string;
  perfilAcesso: Perfil;
  ativo: boolean;
}

export type FuncionarioForm = Omit<Funcionario, "id" | "ativo">;

export interface Prestador {
  id: UUID;
  nome: string;
  documento: string;
  pessoaJuridica: boolean;
  servicoPrestado: string;
  vigenciaInicio: DataISO;
  vigenciaFim: DataISO;
  contratoVigente: boolean;
  documentosObrigatorios: string[];
  email: string | null;
  telefone: string | null;
  ativo: boolean;
}

export type PrestadorForm = Omit<
  Prestador,
  "id" | "ativo" | "pessoaJuridica" | "contratoVigente"
>;

export interface TurmaForm {
  nome: string;
  serie: Serie;
  anoLetivo: number;
  capacidadeMaxima: number;
  professorTitularId?: UUID | null;
}

export interface ResultadoBusca {
  tipo: "ALUNO" | "PROFESSOR" | "FUNCIONARIO";
  id: UUID;
  nome: string;
  detalhe: string;
}

export interface RegistroAuditoria {
  id: UUID;
  usuario: string;
  acao: string;
  entidade: string;
  entidadeId: string;
  detalhes: string | null;
  ocorridoEm: DataISO;
}

/** Edição da ficha do aluno (RF-31). */
export interface AlunoForm {
  nome: string;
  dataNascimento: string;
  alergias?: string;
  tipoSanguineo?: string;
  necessidadesEspeciais?: string;
  hospitalEmergencia?: string;
  autorizados: Autorizado[];
  fotoUrl?: string | null;
}

/** Correção dos dados do responsável financeiro (o CPF não muda). */
export interface ResponsavelForm {
  nome: string;
  email: string;
  telefone: string;
  parentesco: string;
}
