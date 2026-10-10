"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useCallback, useState } from "react";
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
import type { Usuario } from "@/domain/models/auth";
import { ErroApi, mensagemDeErro } from "@/domain/models/erro";
import { Marca } from "@/presentation/components/layout/Marca";
import { CampoTexto } from "@/presentation/components/shared/Campo";
import { rotaInicial } from "@/presentation/lib/navegacao";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { type LoginForm, loginSchema } from "@/presentation/schemas/login";
import { CampoCodigo } from "./CampoCodigo";
import { ConfigurarDoisFatores } from "./ConfigurarDoisFatores";

type Etapa = "CREDENCIAIS" | "CODIGO" | "CONFIGURAR_DOIS_FATORES";

const TEXTOS: Record<Etapa, { titulo: string; descricao: string }> = {
  CREDENCIAIS: {
    titulo: "Entrar",
    descricao: "Acesse com o e-mail cadastrado na escola.",
  },
  CODIGO: {
    titulo: "Verificação em duas etapas",
    descricao: "Abra o aplicativo autenticador e digite o código.",
  },
  CONFIGURAR_DOIS_FATORES: {
    titulo: "Proteja sua conta",
    descricao:
      "Seu perfil exige verificação em duas etapas. Configure-a para continuar.",
  },
};

export function LoginTela() {
  const { entrar, concluirLogin } = useAuth();
  const { auth } = useGateways();
  const router = useRouter();
  const [etapa, setEtapa] = useState<Etapa>("CREDENCIAIS");
  const [erro, setErro] = useState<string | null>(null);

  const concluir = useCallback(
    (usuario: Usuario) => {
      concluirLogin(usuario);
      router.replace(rotaInicial(usuario.perfil));
    },
    [concluirLogin, router],
  );

  /** O desafio vale 5 minutos: depois disso, a senha precisa ser digitada de novo. */
  const recomecarSeExpirou = useCallback((e: unknown) => {
    if (e instanceof ErroApi && e.status === 401) {
      setEtapa("CREDENCIAIS");
      setErro(mensagemDeErro(e));
      return true;
    }
    return false;
  }, []);

  async function aposSenha(dados: LoginForm) {
    setErro(null);
    try {
      const resultado = await entrar(dados);
      if (resultado.etapa === "AUTENTICADO") concluir(resultado.usuario);
      else setEtapa(resultado.etapa);
    } catch (e) {
      setErro(mensagemDeErro(e));
    }
  }

  const preparar = useCallback(() => auth.prepararDoisFatoresNoLogin(), [auth]);
  const ativar = useCallback(
    async (codigo: string) => {
      try {
        return await auth.ativarDoisFatoresNoLogin(codigo);
      } catch (e) {
        recomecarSeExpirou(e);
        throw e;
      }
    },
    [auth, recomecarSeExpirou],
  );

  return (
    <main className="flex min-h-screen items-center justify-center bg-muted/40 p-4">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <Marca />
          <CardTitle className="pt-4 text-lg">{TEXTOS[etapa].titulo}</CardTitle>
          <CardDescription>{TEXTOS[etapa].descricao}</CardDescription>
        </CardHeader>
        <CardContent>
          {etapa === "CREDENCIAIS" && (
            <FormularioSenha erro={erro} aoEnviar={aposSenha} />
          )}
          {etapa === "CODIGO" && (
            <FormularioCodigo
              confirmar={async (codigo) => {
                try {
                  concluir((await auth.confirmarCodigo(codigo)).usuario);
                } catch (e) {
                  if (!recomecarSeExpirou(e)) throw e;
                }
              }}
              aoVoltar={() => setEtapa("CREDENCIAIS")}
            />
          )}
          {etapa === "CONFIGURAR_DOIS_FATORES" && (
            <ConfigurarDoisFatores
              preparar={preparar}
              ativar={ativar}
              aoConcluir={(ativacao) => concluir(ativacao.usuario)}
            />
          )}
        </CardContent>
        {etapa === "CREDENCIAIS" && (
          <CardFooter className="justify-center text-sm text-muted-foreground">
            Ainda não é aluno?&nbsp;
            <Link
              href="/pre-matricula"
              className="font-medium text-foreground underline-offset-4 hover:underline"
            >
              Faça a pré-matrícula
            </Link>
          </CardFooter>
        )}
      </Card>
    </main>
  );
}

function FormularioSenha({
  erro,
  aoEnviar,
}: {
  erro: string | null;
  aoEnviar(dados: LoginForm): Promise<void>;
}) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) });

  return (
    <form onSubmit={handleSubmit(aoEnviar)} noValidate>
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
  );
}

function FormularioCodigo({
  confirmar,
  aoVoltar,
}: {
  confirmar(codigo: string): Promise<void>;
  aoVoltar(): void;
}) {
  const [codigo, setCodigo] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function enviar(evento: React.FormEvent) {
    evento.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await confirmar(codigo.trim());
    } catch (e) {
      setErro(mensagemDeErro(e));
      setCodigo("");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={enviar} noValidate>
      <FieldGroup>
        {erro && (
          <Alert variant="destructive">
            <AlertDescription>{erro}</AlertDescription>
          </Alert>
        )}
        <CampoCodigo valor={codigo} aoMudar={setCodigo} />
        <Button
          type="submit"
          className="w-full"
          disabled={enviando || codigo.trim().length < 6}
        >
          {enviando ? "Verificando…" : "Verificar"}
        </Button>
        <Button
          type="button"
          variant="ghost"
          className="w-full"
          onClick={aoVoltar}
        >
          Voltar
        </Button>
      </FieldGroup>
    </form>
  );
}
