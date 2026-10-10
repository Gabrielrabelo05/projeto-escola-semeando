import { beforeEach, describe, expect, it } from "vitest";
import { FakeHttpClient } from "@/test/FakeHttpClient";
import { criarGateways } from "../di/container";

describe("gateways HTTP", () => {
  let http: FakeHttpClient;
  let gateways: ReturnType<typeof criarGateways>;

  beforeEach(() => {
    http = new FakeHttpClient();
    gateways = criarGateways(http);
  });

  it("matrícula: aprovar, rejeitar e enviar documento usam os endpoints corretos", async () => {
    await gateways.matricula.aprovar("m1");
    expect(http.ultima).toEqual({
      metodo: "POST",
      url: "/matriculas/m1/aprovar",
      corpo: undefined,
      params: undefined,
    });

    await gateways.matricula.rejeitar("m1", "Sem vagas");
    expect(http.ultima?.corpo).toEqual({ justificativa: "Sem vagas" });

    await gateways.matricula.enviarDocumento(
      "m1",
      "DOCUMENTO_ALUNO",
      new File(["x"], "historico.pdf"),
    );
    expect(http.ultima).toMatchObject({
      metodo: "UPLOAD",
      url: "/matriculas/m1/documentos/DOCUMENTO_ALUNO",
    });
  });

  it("contrato: geração envia os valores para validação da RN-02", async () => {
    await gateways.contrato.gerar("m1", 350, 11);
    expect(http.ultima).toMatchObject({
      metodo: "POST",
      url: "/contratos",
      corpo: { matriculaId: "m1", valorParcela: 350, numeroParcelas: 11 },
    });
  });

  it("cobrança: inadimplência e geração mensal", async () => {
    await gateways.cobranca.inadimplencia("2026-03");
    expect(http.ultima).toMatchObject({
      metodo: "GET",
      url: "/cobrancas/inadimplencia",
      params: { mes: "2026-03" },
    });
    await gateways.cobranca.gerarMensal("2026-04");
    expect(http.ultima?.corpo).toEqual({ mes: "2026-04" });
  });

  it("cadastros: salvar decide entre criação e edição", async () => {
    const dados = {
      nome: "Ana",
      cpf: "52998224725",
      email: null,
      telefone: null,
      rg: null,
      formacaoAcademica: null,
      disciplinas: [],
      cargaHorariaSemanal: 40,
    };
    await gateways.cadastro.salvarProfessor(dados);
    expect(http.ultima).toMatchObject({ metodo: "POST", url: "/professores" });
    await gateways.cadastro.salvarProfessor(dados, "p1");
    expect(http.ultima).toMatchObject({
      metodo: "PUT",
      url: "/professores/p1",
    });
  });

  it("obrigações: checklist envia o estado como parâmetro e exportações baixam arquivos", async () => {
    await gateways.obrigacao.marcarItem("o1", 2, true);
    expect(http.ultima).toMatchObject({
      metodo: "PUT",
      url: "/obrigacoes/o1/checklist/2",
      params: { concluido: true },
    });
    await gateways.obrigacao.exportarSistemaPresenca(2026, 3);
    expect(http.ultima).toMatchObject({
      metodo: "DOWNLOAD",
      url: "/obrigacoes/exportacoes/sistema-presenca",
      params: { ano: 2026, bimestre: 3 },
    });
  });
});
