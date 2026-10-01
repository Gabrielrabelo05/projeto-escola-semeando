import { describe, expect, it } from "vitest";
import { preMatriculaSchema } from "./preMatricula";

const valido = {
  aluno: {
    nome: "Lucas",
    dataNascimento: "2019-05-01",
    hospitalEmergencia: "Hospital Sintético",
    autorizados: [{ nome: "Avó", documento: "RG 1", parentesco: "Avó" }],
  },
  responsavel: {
    nome: "Maria",
    cpf: "529.982.247-25",
    email: "maria@exemplo.dev",
    telefone: "98900000000",
    parentesco: "Mãe",
  },
  turmaId: "t1",
};

describe("preMatriculaSchema", () => {
  it("aceita uma pré-matrícula completa", () => {
    expect(preMatriculaSchema.safeParse(valido).success).toBe(true);
  });

  it("RN-06: exige hospital de emergência e ao menos um autorizado", () => {
    const resultado = preMatriculaSchema.safeParse({
      ...valido,
      aluno: { ...valido.aluno, hospitalEmergencia: "", autorizados: [] },
    });
    expect(resultado.success).toBe(false);
    const caminhos = resultado.error?.issues.map((i) => i.path.join("."));
    expect(caminhos).toContain("aluno.hospitalEmergencia");
    expect(caminhos).toContain("aluno.autorizados");
  });

  it("rejeita CPF inválido do responsável", () => {
    const resultado = preMatriculaSchema.safeParse({
      ...valido,
      responsavel: { ...valido.responsavel, cpf: "111.111.111-11" },
    });
    expect(resultado.success).toBe(false);
  });
});
