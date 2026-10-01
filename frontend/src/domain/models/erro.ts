/** Erro de negócio ou de comunicação devolvido pela API, já normalizado. */
export class ErroApi extends Error {
  constructor(
    readonly status: number,
    readonly codigo: string,
    mensagem: string,
    readonly campos: Record<string, string> = {},
  ) {
    super(mensagem);
    this.name = "ErroApi";
  }

  get regraDeNegocio(): boolean {
    return /^R[NF]-\d+$/.test(this.codigo);
  }
}

export function mensagemDeErro(erro: unknown): string {
  if (erro instanceof ErroApi) {
    return erro.regraDeNegocio
      ? `${erro.codigo}: ${erro.message}`
      : erro.message;
  }
  return "Não foi possível concluir a operação.";
}
