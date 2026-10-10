import { z } from "zod";

export const definirSenhaSchema = z
  .object({
    senha: z.string().min(8, "A senha deve ter ao menos 8 caracteres"),
    confirmacao: z.string(),
  })
  .refine((d) => d.senha === d.confirmacao, {
    path: ["confirmacao"],
    message: "As senhas não conferem",
  });

export type DefinirSenhaForm = z.infer<typeof definirSenhaSchema>;
