import type {
  AtivacaoDoisFatores,
  ConfiguracaoDoisFatores,
  ConviteAcesso,
  Credenciais,
  NovoUsuario,
  ResultadoLogin,
  Sessao,
  SituacaoDoisFatores,
  Usuario,
} from "../models/auth";
import type { UUID } from "../models/comum";

export interface AuthGateway {
  entrar(credenciais: Credenciais): Promise<ResultadoLogin>;
  /** Etapa do código no login (aplicativo ou código de recuperação). */
  confirmarCodigo(codigo: string): Promise<Sessao>;
  /** 1º acesso de quem é obrigado a usar 2FA: QR Code antes de abrir a sessão. */
  prepararDoisFatoresNoLogin(): Promise<ConfiguracaoDoisFatores>;
  ativarDoisFatoresNoLogin(codigo: string): Promise<AtivacaoDoisFatores>;
  situacaoDoisFatores(): Promise<SituacaoDoisFatores>;
  prepararDoisFatores(): Promise<ConfiguracaoDoisFatores>;
  ativarDoisFatores(codigo: string): Promise<AtivacaoDoisFatores>;
  /** `sessaoEncerrada`: perfil obrigado a usar 2FA precisa entrar de novo. */
  desativarDoisFatores(codigo: string): Promise<{ sessaoEncerrada: boolean }>;
  /** Usuário do cookie de sessão atual (falha com 401 se não houver sessão). */
  sessaoAtual(): Promise<Usuario>;
  sair(): Promise<void>;
  listarUsuarios(): Promise<Usuario[]>;
  criarUsuario(dados: NovoUsuario): Promise<ConviteAcesso>;
  novoLinkAcesso(id: UUID): Promise<ConviteAcesso>;
  inativarUsuario(id: UUID): Promise<Usuario>;
  consultarConvite(token: string): Promise<Usuario>;
  definirSenha(token: string, senha: string): Promise<Usuario>;
}
