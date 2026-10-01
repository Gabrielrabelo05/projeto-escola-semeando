import type { Credenciais, NovoUsuario, Sessao, Usuario } from "../models/auth";
import type { UUID } from "../models/comum";

export interface AuthGateway {
  entrar(credenciais: Credenciais): Promise<Sessao>;
  listarUsuarios(): Promise<Usuario[]>;
  criarUsuario(dados: NovoUsuario): Promise<Usuario>;
  inativarUsuario(id: UUID): Promise<Usuario>;
}
