import type { Perfil, UUID } from "./comum";

export interface Usuario {
  id: UUID;
  nome: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
}

export interface Sessao {
  token: string;
  usuario: Usuario;
}

export interface Credenciais {
  email: string;
  senha: string;
}

export interface NovoUsuario {
  nome: string;
  email: string;
  senha: string;
  perfil: Perfil;
  cpf?: string;
}
