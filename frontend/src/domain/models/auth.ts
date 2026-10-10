import type { Perfil, UUID } from "./comum";

export interface Usuario {
  id: UUID;
  nome: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
  /** Ainda não definiu a senha pelo link de acesso. */
  acessoPendente: boolean;
  /** Entra com o código do aplicativo autenticador. */
  doisFatoresAtivo: boolean;
}

/** O token fica só no cookie HttpOnly; o front recebe apenas os dados do usuário. */
export interface Sessao {
  usuario: Usuario;
}

/**
 * Resultado da senha: entra direto, pede o código do aplicativo ou, no 1º acesso de quem é obrigado a usar
 * verificação em duas etapas (Administrador), pede para configurá-la.
 */
export type ResultadoLogin =
  | { etapa: "AUTENTICADO"; usuario: Usuario }
  | { etapa: "CODIGO" | "CONFIGURAR_DOIS_FATORES"; usuario?: undefined };

/** QR Code (`uri` otpauth://) e o segredo para digitar no aplicativo. */
export interface ConfiguracaoDoisFatores {
  segredo: string;
  uri: string;
}

/** Os códigos de recuperação aparecem só nesta resposta. */
export interface AtivacaoDoisFatores {
  usuario: Usuario;
  codigosRecuperacao: string[];
}

export interface SituacaoDoisFatores {
  ativo: boolean;
  obrigatorio: boolean;
  codigosRecuperacaoRestantes: number;
}

export interface Credenciais {
  email: string;
  senha: string;
}

/** O acesso é criado sem senha: a própria pessoa define a senha pelo link. */
export interface NovoUsuario {
  nome: string;
  email: string;
  perfil: Perfil;
  cpf?: string;
}

/** Link de uso único (7 dias) para definir ou redefinir a senha. */
export interface ConviteAcesso {
  usuario: Usuario;
  link: string;
  expiraEm: string;
}
