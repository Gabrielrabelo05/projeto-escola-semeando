import type {
  ConviteAcesso,
  Credenciais,
  NovoUsuario,
  Sessao,
  Usuario,
} from "../models/auth";
import type { UUID } from "../models/comum";

export interface AuthGateway {
  entrar(credenciais: Credenciais): Promise<Sessao>;
  listarUsuarios(): Promise<Usuario[]>;
  criarUsuario(dados: NovoUsuario): Promise<ConviteAcesso>;
  novoLinkAcesso(id: UUID): Promise<ConviteAcesso>;
  inativarUsuario(id: UUID): Promise<Usuario>;
  consultarConvite(token: string): Promise<Usuario>;
  definirSenha(token: string, senha: string): Promise<Usuario>;
}
