"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { FieldGroup } from "@/components/ui/field";
import { mensagemDeErro } from "@/domain/models/erro";
import { Marca } from "@/presentation/components/layout/Marca";
import { CampoTexto } from "@/presentation/components/shared/Campo";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import {
  type DefinirSenhaForm,
  definirSenhaSchema,
} from "@/presentation/schemas/definirSenha";

/** Página pública aberta pelo link de acesso: a pessoa cria (ou redefine) a própria senha. */
export function DefinirSenhaTela() {
  const { auth } = useGateways();
  const router = useRouter();
  const parametros = useSearchParams();
  const [token] = useState(() => parametros.get("token") ?? "");
  // O token sai da barra de endereço (e do histórico) assim que é lido.
  useEffect(() => {
    window.history.replaceState(null, "", "/definir-senha");
  }, []);
  const convite = useCarregar(() => auth.consultarConvite(token), [token]);
  const [erro, setErro] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<DefinirSenhaForm>({ resolver: zodResolver(definirSenhaSchema) });

  async function enviar(dados: DefinirSenhaForm) {
    setErro(null);
    try {
      await auth.definirSenha(token, dados.senha);
      toast.success("Senha definida. Entre com o seu e-mail e a nova senha.");
      router.replace("/login");
    } catch (e) {
      setErro(mensagemDeErro(e));
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-muted/40 p-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <Marca />
          <CardTitle className="pt-4 text-lg">Criar senha</CardTitle>
          <CardDescription>
            Defina a senha que você vai usar para entrar no sistema.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Conteudo estado={convite}>
            {(usuario) => (
              <form onSubmit={handleSubmit(enviar)} noValidate>
                <FieldGroup>
                  {erro && (
                    <Alert variant="destructive">
                      <AlertDescription>{erro}</AlertDescription>
                    </Alert>
                  )}
                  <p className="text-sm">
                    Olá, <strong>{usuario.nome}</strong>. Seu login é{" "}
                    <strong>{usuario.email}</strong>.
                  </p>
                  <CampoTexto
                    id="senha"
                    rotulo="Nova senha"
                    type="password"
                    autoComplete="new-password"
                    erro={errors.senha?.message}
                    {...register("senha")}
                  />
                  <CampoTexto
                    id="confirmacao"
                    rotulo="Repita a senha"
                    type="password"
                    autoComplete="new-password"
                    erro={errors.confirmacao?.message}
                    {...register("confirmacao")}
                  />
                  <Button
                    type="submit"
                    className="w-full"
                    disabled={isSubmitting}
                  >
                    {isSubmitting ? "Salvando…" : "Salvar senha"}
                  </Button>
                </FieldGroup>
              </form>
            )}
          </Conteudo>
        </CardContent>
        <CardFooter className="justify-center text-sm text-muted-foreground">
          <Link
            href="/login"
            className="font-medium text-foreground underline-offset-4 hover:underline"
          >
            Ir para o login
          </Link>
        </CardFooter>
      </Card>
    </main>
  );
}
