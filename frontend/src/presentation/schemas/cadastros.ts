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

/** RF-31: edição da ficha do aluno (hospital e autorizados continuam obrigatórios pela RN-06). */
export const alunoSchema = z.object({
  nome: texto("Nome"),
  dataNascimento: texto("Data de nascimento").refine(
    (valor) => new Date(valor) < new Date(),
    "A data de nascimento deve estar no passado",
  ),
  alergias: z.string(),
  tipoSanguineo: z.string(),
  necessidadesEspeciais: z.string(),
  hospitalEmergencia: texto("Hospital de emergência"),
  autorizados: z
    .array(
      z.object({
        nome: texto("Nome"),
        documento: texto("Documento"),
        parentesco: texto("Parentesco"),
        telefone: z.string().optional(),
      }),
    )
    .min(1, "Informe ao menos uma pessoa autorizada a buscar a criança"),
});

export const responsavelSchema = z.object({
  nome: texto("Nome"),
  email: z.email("E-mail inválido"),
  telefone: z.string().trim().min(10, "Telefone com DDD"),
  parentesco: texto("Parentesco"),
});
