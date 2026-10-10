import type { AuthGateway } from "@/domain/gateways";
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
} from "@/domain/models/auth";
import type { UUID } from "@/domain/models/comum";
import type { HttpClient } from "../http/HttpClient";

export class HttpAuthGateway implements AuthGateway {
  constructor(private readonly http: HttpClient) {}

  entrar(credenciais: Credenciais): Promise<ResultadoLogin> {
    return this.http.post("/auth/login", credenciais);
  }

  confirmarCodigo(codigo: string): Promise<Sessao> {
    return this.http.post("/auth/desafio/codigo", { codigo });
  }

  prepararDoisFatoresNoLogin(): Promise<ConfiguracaoDoisFatores> {
    return this.http.post("/auth/desafio/configuracao");
  }

  ativarDoisFatoresNoLogin(codigo: string): Promise<AtivacaoDoisFatores> {
    return this.http.post("/auth/desafio/ativacao", { codigo });
  }

  situacaoDoisFatores(): Promise<SituacaoDoisFatores> {
    return this.http.get("/auth/dois-fatores");
  }

  prepararDoisFatores(): Promise<ConfiguracaoDoisFatores> {
    return this.http.post("/auth/dois-fatores/configuracao");
  }

  ativarDoisFatores(codigo: string): Promise<AtivacaoDoisFatores> {
    return this.http.post("/auth/dois-fatores/ativacao", { codigo });
  }

  desativarDoisFatores(codigo: string): Promise<{ sessaoEncerrada: boolean }> {
    return this.http.post("/auth/dois-fatores/desativacao", { codigo });
  }

  sessaoAtual(): Promise<Usuario> {
    return this.http.get("/auth/sessao");
  }

  sair(): Promise<void> {
    return this.http.post("/auth/sair");
  }

  listarUsuarios(): Promise<Usuario[]> {
    return this.http.get("/usuarios");
  }

  criarUsuario(dados: NovoUsuario): Promise<ConviteAcesso> {
    return this.http.post("/usuarios", dados);
  }

  novoLinkAcesso(id: UUID): Promise<ConviteAcesso> {
    return this.http.post(`/usuarios/${id}/link-acesso`);
  }

  inativarUsuario(id: UUID): Promise<Usuario> {
    return this.http.patch(`/usuarios/${id}/inativar`);
  }

  consultarConvite(token: string): Promise<Usuario> {
    return this.http.get(`/publico/convites/${encodeURIComponent(token)}`);
  }

  definirSenha(token: string, senha: string): Promise<Usuario> {
    return this.http.post(
      `/publico/convites/${encodeURIComponent(token)}/senha`,
      { senha },
    );
  }
}
