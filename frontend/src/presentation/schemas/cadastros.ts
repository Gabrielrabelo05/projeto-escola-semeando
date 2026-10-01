import { z } from "zod";
import { cpfValido } from "@/presentation/lib/formatadores";

const texto = (campo: string) =>
  z.string().trim().min(1, `${campo} é obrigatório`);
const emailOpcional = z.union([z.literal(""), z.email("E-mail inválido")]);
const lista = z.string().transform((v) =>
  v
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean),
);

export const professorSchema = z.object({
  nome: texto("Nome"),
  cpf: z.string().refine(cpfValido, "CPF inválido"),
  email: emailOpcional,
  telefone: z.string(),
  rg: z.string(),
  formacaoAcademica: z.string(),
  disciplinas: lista,
  cargaHorariaSemanal: z.coerce.number<string>().int().min(0),
});

export const funcionarioSchema = z.object({
  nome: texto("Nome"),
  cpf: z.string().refine(cpfValido, "CPF inválido"),
  email: emailOpcional,
  telefone: z.string(),
  cargo: texto("Cargo"),
  setor: texto("Setor"),
  perfilAcesso: z.enum([
    "ADMIN",
    "DIRETOR",
    "SECRETARIA",
    "FINANCEIRO",
    "PROFESSOR",
  ]),
});

export const prestadorSchema = z
  .object({
    nome: texto("Nome"),
    documento: z
      .string()
      .refine(
        (v) => [11, 14].includes(v.replace(/\D/g, "").length),
        "CPF ou CNPJ inválido",
      ),
    servicoPrestado: texto("Serviço"),
    vigenciaInicio: texto("Início"),
    vigenciaFim: texto("Fim"),
    documentosObrigatorios: lista,
    email: emailOpcional,
    telefone: z.string(),
  })
  .refine((p) => p.vigenciaFim >= p.vigenciaInicio, {
    path: ["vigenciaFim"],
    message: "Fim deve ser após o início",
  });

export const turmaSchema = z.object({
  nome: texto("Nome"),
  serie: z.enum([
    "MATERNAL_I",
    "MATERNAL_II",
    "INFANTIL_1",
    "INFANTIL_2",
    "PRIMEIRO_ANO",
    "SEGUNDO_ANO",
    "TERCEIRO_ANO",
    "QUARTO_ANO",
    "QUINTO_ANO",
  ]),
  anoLetivo: z.coerce.number<string>().int().min(2000),
  capacidadeMaxima: z.coerce.number<string>().int().min(1, "Mínimo 1"),
});
