import type { ArquivoBaixado } from "@/domain/models/comum";
import type {
  HttpClient,
  OpcoesRequisicao,
  Parametros,
} from "@/infrastructure/http/HttpClient";

export interface Chamada {
  metodo: string;
  url: string;
  corpo?: unknown;
  params?: Parametros;
}

/** Dublê de HttpClient: registra as chamadas e devolve respostas programadas. */
export class FakeHttpClient implements HttpClient {
  readonly chamadas: Chamada[] = [];
  resposta: unknown = {};

  private registrar<T>(chamada: Chamada): Promise<T> {
    this.chamadas.push(chamada);
    return Promise.resolve(this.resposta as T);
  }

  get<T>(url: string, params?: Parametros) {
    return this.registrar<T>({ metodo: "GET", url, params });
  }

  post<T>(url: string, corpo?: unknown, opcoes?: OpcoesRequisicao) {
    return this.registrar<T>({
      metodo: "POST",
      url,
      corpo,
      params: opcoes?.params,
    });
  }

  put<T>(url: string, corpo?: unknown, opcoes?: OpcoesRequisicao) {
    return this.registrar<T>({
      metodo: "PUT",
      url,
      corpo,
      params: opcoes?.params,
    });
  }

  patch<T>(url: string, corpo?: unknown) {
    return this.registrar<T>({ metodo: "PATCH", url, corpo });
  }

  delete<T = void>(url: string) {
    return this.registrar<T>({ metodo: "DELETE", url });
  }

  upload<T>(url: string, campo: string, arquivo: File) {
    return this.registrar<T>({
      metodo: "UPLOAD",
      url,
      corpo: { campo, nome: arquivo.name },
    });
  }

  download(url: string, params?: Parametros): Promise<ArquivoBaixado> {
    this.chamadas.push({ metodo: "DOWNLOAD", url, params });
    return Promise.resolve({ nome: "arquivo", conteudo: new Blob() });
  }

  get ultima(): Chamada | undefined {
    return this.chamadas.at(-1);
  }
}
