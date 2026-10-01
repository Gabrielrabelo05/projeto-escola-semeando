import { describe, expect, it } from "vitest";
import { ErroApi, mensagemDeErro } from "./erro";

describe("ErroApi", () => {
  it("prefixa o código quando é uma regra de negócio numerada", () => {
    const erro = new ErroApi(
      422,
      "RN-02",
      "Valores divergentes da tabela oficial",
    );
    expect(erro.regraDeNegocio).toBe(true);
    expect(mensagemDeErro(erro)).toBe(
      "RN-02: Valores divergentes da tabela oficial",
    );
  });

  it("mantém a mensagem original para erros comuns", () => {
    expect(
      mensagemDeErro(new ErroApi(403, "ACESSO_NEGADO", "Sem permissão")),
    ).toBe("Sem permissão");
    expect(mensagemDeErro(new Error("x"))).toBe(
      "Não foi possível concluir a operação.",
    );
  });
});
