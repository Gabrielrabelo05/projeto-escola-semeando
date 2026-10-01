import { z } from "zod";
import { cpfValido } from "@/presentation/lib/formatadores";

const obrigatorio = (campo: string) =>
  z.string().trim().min(1, `${campo} é obrigatório`);

export const autorizadoSchema = z.object({
  nome: obrigatorio("Nome"),
  documento: obrigatorio("Documento"),
  parentesco: obrigatorio("Parentesco"),
  telefone: z.string().optional(),
});

/** RF-01 – inclui os campos obrigatórios por cláusula contratual (RN-06). */
export const preMatriculaSchema = z.object({
  aluno: z.object({
    nome: obrigatorio("Nome do aluno"),
    dataNascimento: obrigatorio("Data de nascimento").refine(
      (valor) => new Date(valor) < new Date(),
      "A data de nascimento deve estar no passado",
    ),
    alergias: z.string().optional(),
    tipoSanguineo: z.string().optional(),
    necessidadesEspeciais: z.string().optional(),
    hospitalEmergencia: obrigatorio("Hospital de emergência"),
    autorizados: z
      .array(autorizadoSchema)
      .min(1, "Informe ao menos uma pessoa autorizada a buscar a criança"),
  }),
  responsavel: z.object({
    nome: obrigatorio("Nome do responsável"),
    cpf: z.string().refine(cpfValido, "CPF inválido"),
    email: z.email("E-mail inválido"),
    telefone: z.string().trim().min(10, "Telefone com DDD"),
    parentesco: obrigatorio("Parentesco"),
  }),
  turmaId: z.string().min(1, "Selecione a turma"),
});

export type PreMatriculaForm = z.infer<typeof preMatriculaSchema>;

export const ETAPAS = [
  { titulo: "Aluno", campos: ["aluno.nome", "aluno.dataNascimento"] },
  {
    titulo: "Saúde e segurança",
    campos: ["aluno.hospitalEmergencia", "aluno.autorizados"],
  },
  { titulo: "Responsável", campos: ["responsavel"] },
  { titulo: "Turma", campos: ["turmaId"] },
] as const;
