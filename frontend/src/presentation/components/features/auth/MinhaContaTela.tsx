"use client";

import { ShieldCheck, ShieldOff } from "lucide-react";
import { useRouter } from "next/navigation";
import { useCallback, useState } from "react";
import { toast } from "sonner";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { FieldGroup } from "@/components/ui/field";
import type { AtivacaoDoisFatores } from "@/domain/models/auth";
import { mensagemDeErro } from "@/domain/models/erro";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { rotulo } from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { CampoCodigo } from "./CampoCodigo";
import { ConfigurarDoisFatores } from "./ConfigurarDoisFatores";

/** Dados da conta e verificação em duas etapas (RNF-01), para qualquer perfil. */
export function MinhaContaTela() {
  const { usuario, concluirLogin, sair } = useAuth();
  const { auth } = useGateways();
  const router = useRouter();
  const situacao = useCarregar(() => auth.situacaoDoisFatores(), [auth]);
  const [configurando, setConfigurando] = useState(false);

  const preparar = useCallback(() => auth.prepararDoisFatores(), [auth]);
  const ativar = useCallback(
    (codigo: string) => auth.ativarDoisFatores(codigo),
    [auth],
  );

  function ativado(ativacao: AtivacaoDoisFatores) {
    concluirLogin(ativacao.usuario);
    setConfigurando(false);
    toast.success("Verificação em duas etapas ativada");
    void situacao.recarregar();
  }

  async function desativar(codigo: string) {
    const { sessaoEncerrada } = await auth.desativarDoisFatores(codigo);
    if (sessaoEncerrada) {
      toast.success(
        "Verificação desativada. Entre de novo para configurar o novo aparelho.",
      );
      sair();
      router.replace("/login");
      return;
    }
    toast.success("Verificação em duas etapas desativada");
    void situacao.recarregar();
  }

  return (
    <>
      <PageHeader
        titulo="Minha conta"
        descricao={`${usuario?.nome ?? ""} · ${usuario?.email ?? ""} · ${rotulo(usuario?.perfil)}`}
      />
      <Card className="max-w-xl">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Verificação em duas etapas
            {situacao.dados &&
              (situacao.dados.ativo ? (
                <Badge>
                  <ShieldCheck /> Ativa
                </Badge>
              ) : (
                <Badge variant="secondary">
                  <ShieldOff /> Inativa
                </Badge>
              ))}
          </CardTitle>
          <CardDescription>
            Além da senha, o login pede um código do aplicativo autenticador no
            celular. Mesmo que alguém descubra sua senha, não consegue entrar.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Conteudo estado={situacao}>
            {(dados) =>
              dados.ativo ? (
                <div className="space-y-4">
                  <p className="text-sm text-muted-foreground">
                    Códigos de recuperação restantes:{" "}
                    <strong className="text-foreground">
                      {dados.codigosRecuperacaoRestantes}
                    </strong>
                    . Se acabarem ou você perder o celular, peça à administração
                    um novo link de acesso.
                  </p>
                  <Desativar
                    obrigatorio={dados.obrigatorio}
                    aoDesativar={desativar}
                  />
                </div>
              ) : configurando ? (
                <ConfigurarDoisFatores
                  preparar={preparar}
                  ativar={ativar}
                  aoConcluir={ativado}
                />
              ) : (
                <div className="space-y-3">
                  {dados.obrigatorio && (
                    <Alert>
                      <AlertDescription>
                        Obrigatória para o seu perfil.
                      </AlertDescription>
                    </Alert>
                  )}
                  <Button onClick={() => setConfigurando(true)}>
                    <ShieldCheck /> Ativar
                  </Button>
                </div>
              )
            }
          </Conteudo>
        </CardContent>
      </Card>
    </>
  );
}

function Desativar({
  obrigatorio,
  aoDesativar,
}: {
  obrigatorio: boolean;
  aoDesativar(codigo: string): Promise<void>;
}) {
  const [aberto, setAberto] = useState(false);
  const [codigo, setCodigo] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  if (!aberto) {
    return (
      <Button variant="outline" onClick={() => setAberto(true)}>
        {obrigatorio ? "Trocar de aparelho" : "Desativar"}
      </Button>
    );
  }

  async function enviar(evento: React.FormEvent) {
    evento.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      await aoDesativar(codigo.trim());
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
        <Alert>
          <AlertDescription>
            {obrigatorio
              ? "Confirme com um código. Você sairá do sistema e, ao entrar de novo, vai configurar o novo aparelho."
              : "Confirme com um código do aplicativo ou de recuperação."}
          </AlertDescription>
        </Alert>
        {erro && (
          <Alert variant="destructive">
            <AlertDescription>{erro}</AlertDescription>
          </Alert>
        )}
        <CampoCodigo valor={codigo} aoMudar={setCodigo} />
        <div className="flex gap-2">
          <Button
            type="submit"
            variant="destructive"
            disabled={enviando || codigo.trim().length < 6}
          >
            {enviando ? "Confirmando…" : "Confirmar"}
          </Button>
          <Button
            type="button"
            variant="ghost"
            onClick={() => setAberto(false)}
          >
            Cancelar
          </Button>
        </div>
      </FieldGroup>
    </form>
  );
}
