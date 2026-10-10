import axios, {
  type AxiosError,
  type AxiosInstance,
  type AxiosResponse,
} from "axios";
import type { ArquivoBaixado } from "@/domain/models/comum";
import { ErroApi } from "@/domain/models/erro";
import type { HttpClient, OpcoesRequisicao, Parametros } from "./HttpClient";

interface CorpoErro {
  codigo?: string;
  mensagem?: string;
  campos?: Record<string, string>;
}

export class AxiosHttpClient implements HttpClient {
  private readonly http: AxiosInstance;

  /**
   * A sessão viaja no cookie HttpOnly (o JavaScript não lê o token). O header X-Requested-With é exigido pela
   * API em toda alteração feita com o cookie: outro site não consegue enviá-lo (proteção contra CSRF).
   */
  constructor(
    baseURL: string,
    private readonly aoExpirarSessao: () => void = () => {},
  ) {
    this.http = axios.create({
      baseURL,
      timeout: 20_000,
      withCredentials: true,
      headers: { "X-Requested-With": "XMLHttpRequest" },
    });
    this.http.interceptors.response.use(
      (resposta) => resposta,
      (erro: AxiosError<CorpoErro>) => Promise.reject(this.normalizar(erro)),
    );
  }

  async get<T>(url: string, params?: Parametros): Promise<T> {
    return this.dados(this.http.get<T>(url, { params: limpar(params) }));
  }

  async post<T>(
    url: string,
    corpo?: unknown,
    opcoes?: OpcoesRequisicao,
  ): Promise<T> {
    return this.dados(
      this.http.post<T>(url, corpo, {
        params: limpar(opcoes?.params),
        headers: opcoes?.headers,
      }),
    );
  }

  async put<T>(
    url: string,
    corpo?: unknown,
    opcoes?: OpcoesRequisicao,
  ): Promise<T> {
    return this.dados(
      this.http.put<T>(url, corpo, {
        params: limpar(opcoes?.params),
        headers: opcoes?.headers,
      }),
    );
  }

  async patch<T>(
    url: string,
    corpo?: unknown,
    opcoes?: OpcoesRequisicao,
  ): Promise<T> {
    return this.dados(
      this.http.patch<T>(url, corpo, {
        params: limpar(opcoes?.params),
        headers: opcoes?.headers,
      }),
    );
  }

  async delete<T = void>(url: string): Promise<T> {
    return this.dados(this.http.delete<T>(url));
  }

  async upload<T>(url: string, campo: string, arquivo: File): Promise<T> {
    const formulario = new FormData();
    formulario.append(campo, arquivo);
    return this.dados(this.http.post<T>(url, formulario));
  }

  async download(url: string, params?: Parametros): Promise<ArquivoBaixado> {
    const resposta = await this.http.get<Blob>(url, {
      params: limpar(params),
      responseType: "blob",
    });
    return {
      nome: nomeDoArquivo(resposta) ?? "arquivo",
      conteudo: resposta.data,
    };
  }

  private async dados<T>(requisicao: Promise<AxiosResponse<T>>): Promise<T> {
    return (await requisicao).data;
  }

  private normalizar(erro: AxiosError<CorpoErro>): ErroApi {
    const status = erro.response?.status ?? 0;
    if (status === 401) this.aoExpirarSessao();
    if (!erro.response) {
      return new ErroApi(
        0,
        "SEM_CONEXAO",
        "Não foi possível conectar ao servidor.",
      );
    }
    const corpo = erro.response.data ?? {};
    return new ErroApi(
      status,
      corpo.codigo ?? `HTTP_${status}`,
      corpo.mensagem ?? mensagemPadrao(status),
      corpo.campos ?? {},
    );
  }
}

function limpar(params?: Parametros): Parametros | undefined {
  if (!params) return undefined;
  return Object.fromEntries(
    Object.entries(params).filter(
      ([, v]) => v !== undefined && v !== null && v !== "",
    ),
  );
}

function nomeDoArquivo(resposta: AxiosResponse): string | null {
  const disposicao = String(resposta.headers["content-disposition"] ?? "");
  const encontrado = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(disposicao);
  return encontrado ? decodeURIComponent(encontrado[1]) : null;
}

function mensagemPadrao(status: number): string {
  if (status === 401) return "Sessão expirada. Entre novamente.";
  if (status === 403) return "Você não tem permissão para esta ação.";
  if (status === 404) return "Registro não encontrado.";
  return "Erro inesperado no servidor.";
}
