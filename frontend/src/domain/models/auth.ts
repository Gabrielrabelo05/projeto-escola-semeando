import type { Perfil, UUID } from "./comum";

export interface Usuario {
  id: UUID;
  nome: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
  /** Ainda não definiu a senha pelo link de acesso. */
  acessoPendente: boolean;
}

/** O token fica só no cookie HttpOnly; o front recebe apenas os dados do usuário. */
export interface Sessao {
  usuario: Usuario;
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
