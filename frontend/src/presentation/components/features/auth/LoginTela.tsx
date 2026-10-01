"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useForm } from "react-hook-form";
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
import { rotaInicial } from "@/presentation/lib/navegacao";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { type LoginForm, loginSchema } from "@/presentation/schemas/login";

export function LoginTela() {
  const { entrar } = useAuth();
  const router = useRouter();
  const [erro, setErro] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) });

  async function enviar(dados: LoginForm) {
    setErro(null);
    try {
      const usuario = await entrar(dados);
      router.replace(rotaInicial(usuario.perfil));
    } catch (e) {
      setErro(mensagemDeErro(e));
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-muted/40 p-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <Marca />
          <CardTitle className="pt-4 text-lg">Entrar</CardTitle>
          <CardDescription>
            Acesse com o e-mail cadastrado na escola.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(enviar)} noValidate>
            <FieldGroup>
              {erro && (
                <Alert variant="destructive">
                  <AlertDescription>{erro}</AlertDescription>
                </Alert>
              )}
              <CampoTexto
                id="email"
                rotulo="E-mail"
                type="email"
                autoComplete="email"
                erro={errors.email?.message}
                {...register("email")}
              />
              <CampoTexto
                id="senha"
                rotulo="Senha"
                type="password"
                autoComplete="current-password"
                erro={errors.senha?.message}
                {...register("senha")}
              />
              <Button type="submit" className="w-full" disabled={isSubmitting}>
                {isSubmitting ? "Entrando…" : "Entrar"}
              </Button>
            </FieldGroup>
          </form>
        </CardContent>
        <CardFooter className="justify-center text-sm text-muted-foreground">
          Ainda não é aluno?&nbsp;
          <Link
            href="/pre-matricula"
            className="font-medium text-foreground underline-offset-4 hover:underline"
          >
            Faça a pré-matrícula
          </Link>
        </CardFooter>
      </Card>
    </main>
  );
}
