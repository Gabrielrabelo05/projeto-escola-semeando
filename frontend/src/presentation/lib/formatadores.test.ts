import { describe, expect, it } from "vitest";
import { cpfValido, data, formatarCpf, moeda, rotulo } from "./formatadores";

describe("formatadores", () => {
  it("valida CPF com o mesmo algoritmo do backend", () => {
    expect(cpfValido("529.982.247-25")).toBe(true);
    expect(cpfValido("52998224724")).toBe(false);
    expect(cpfValido("111.111.111-11")).toBe(false);
    expect(cpfValido("123")).toBe(false);
  });

  it("aplica máscara de CPF progressivamente", () => {
    expect(formatarCpf("529")).toBe("529");
    expect(formatarCpf("5299822")).toBe("529.982.2");
    expect(formatarCpf("52998224725")).toBe("529.982.247-25");
  });

  it("formata moeda e datas no padrão brasileiro", () => {
    expect(moeda(4125).replace(/\s/g, " ")).toBe("R$ 4.125,00");
    expect(moeda(null)).toBe("—");
    expect(data("2026-03-31")).toBe("31/03/2026");
  });

  it("transforma enums em rótulos legíveis", () => {
    expect(rotulo("AGUARDANDO_ANALISE")).toBe("Aguardando analise");
    expect(rotulo(undefined)).toBe("—");
  });
});
