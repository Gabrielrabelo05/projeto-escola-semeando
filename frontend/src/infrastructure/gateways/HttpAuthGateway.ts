import type { AuthGateway } from "@/domain/gateways";
import type {
  ConviteAcesso,
  Credenciais,
  NovoUsuario,
  Sessao,
  Usuario,
} from "@/domain/models/auth";
import type { UUID } from "@/domain/models/comum";
import type { HttpClient } from "../http/HttpClient";

export class HttpAuthGateway implements AuthGateway {
  constructor(private readonly http: HttpClient) {}

  entrar(credenciais: Credenciais): Promise<Sessao> {
    return this.http.post("/auth/login", credenciais);
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
