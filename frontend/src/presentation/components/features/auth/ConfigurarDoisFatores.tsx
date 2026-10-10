"use client";

import QRCode from "qrcode";
import { useEffect, useRef, useState } from "react";
import { toast } from "sonner";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { FieldGroup } from "@/components/ui/field";
import type {
  AtivacaoDoisFatores,
  ConfiguracaoDoisFatores,
} from "@/domain/models/auth";
import { mensagemDeErro } from "@/domain/models/erro";
import { Carregando } from "@/presentation/components/shared/Estados";
import { CampoCodigo } from "./CampoCodigo";

interface Props {
  preparar(): Promise<ConfiguracaoDoisFatores>;
  ativar(codigo: string): Promise<AtivacaoDoisFatores>;
  aoConcluir(ativacao: AtivacaoDoisFatores): void;
}

/**
 * Passo a passo da verificação em duas etapas (RNF-01): ler o QR Code no aplicativo autenticador, confirmar com
 * o primeiro código e guardar os códigos de recuperação, que só aparecem nesta tela.
 */
export function ConfigurarDoisFatores({ preparar, ativar, aoConcluir }: Props) {
  const [configuracao, setConfiguracao] =
    useState<ConfiguracaoDoisFatores | null>(null);
  const [qrCode, setQrCode] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [codigo, setCodigo] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [ativacao, setAtivacao] = useState<AtivacaoDoisFatores | null>(null);
  // Um único segredo por tela, mesmo com o efeito executado duas vezes (StrictMode em desenvolvimento).
  const pedido = useRef<Promise<ConfiguracaoDoisFatores> | null>(null);

  useEffect(() => {
    let ativo = true;
    pedido.current ??= preparar();
    pedido.current
      .then(async (c) => {
        if (!ativo) return;
        setConfiguracao(c);
        setQrCode(await QRCode.toDataURL(c.uri, { margin: 1, width: 192 }));
      })
      .catch((e) => ativo && setErro(mensagemDeErro(e)));
    return () => {
      ativo = false;
    };
  }, [preparar]);

  async function confirmar(evento: React.FormEvent) {
    evento.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      setAtivacao(await ativar(codigo));
    } catch (e) {
      setErro(mensagemDeErro(e));
      setCodigo("");
    } finally {
      setEnviando(false);
    }
  }

  if (ativacao) {
    return (
      <CodigosRecuperacao
        codigos={ativacao.codigosRecuperacao}
        aoContinuar={() => aoConcluir(ativacao)}
      />
    );
  }

  if (!configuracao) {
    return erro ? (
      <Alert variant="destructive">
        <AlertDescription>{erro}</AlertDescription>
      </Alert>
    ) : (
      <Carregando linhas={3} />
    );
  }

  return (
    <form onSubmit={confirmar} noValidate>
      <FieldGroup>
        <ol className="list-decimal space-y-1 pl-5 text-sm text-muted-foreground">
          <li>
            Instale um aplicativo autenticador (Google Authenticator, Microsoft
            Authenticator ou Authy).
          </li>
          <li>Leia o QR Code abaixo com o aplicativo.</li>
          <li>Digite o código de 6 dígitos que aparecer.</li>
        </ol>
        <div className="flex flex-col items-center gap-2">
          {qrCode && (
            // biome-ignore lint/performance/noImgElement: QR Code gerado localmente (data URL)
            <img
              src={qrCode}
              alt="QR Code para o aplicativo autenticador"
              width={192}
              height={192}
              className="rounded-md border bg-white"
            />
          )}
          <p className="text-center text-xs text-muted-foreground">
            Sem câmera? Digite esta chave no aplicativo:
            <br />
            <code className="break-all font-mono text-foreground">
              {configuracao.segredo.replace(/(.{4})/g, "$1 ").trim()}
            </code>
          </p>
        </div>
        {erro && (
          <Alert variant="destructive">
            <AlertDescription>{erro}</AlertDescription>
          </Alert>
        )}
        <CampoCodigo valor={codigo} aoMudar={setCodigo} somenteAplicativo />
        <Button
          type="submit"
          className="w-full"
          disabled={enviando || codigo.length !== 6}
        >
          {enviando ? "Confirmando…" : "Ativar verificação em duas etapas"}
        </Button>
      </FieldGroup>
    </form>
  );
}

function CodigosRecuperacao({
  codigos,
  aoContinuar,
}: {
  codigos: string[];
  aoContinuar(): void;
}) {
  const [guardou, setGuardou] = useState(false);
  const texto = codigos.join("\n");

  async function copiar() {
    await navigator.clipboard.writeText(texto);
    toast.success("Códigos copiados");
    setGuardou(true);
  }

  function baixar() {
    const url = URL.createObjectURL(
      new Blob([`Semeando Digital – códigos de recuperação\n\n${texto}\n`], {
        type: "text/plain",
      }),
    );
    const link = document.createElement("a");
    link.href = url;
    link.download = "semeando-codigos-recuperacao.txt";
    link.click();
    URL.revokeObjectURL(url);
    setGuardou(true);
  }

  return (
    <div className="space-y-4">
      <Alert>
        <AlertDescription>
          Verificação ativada. Guarde estes códigos em local seguro: cada um
          permite entrar <strong>uma vez</strong> se você perder o celular. Eles
          não serão mostrados de novo.
        </AlertDescription>
      </Alert>
      <ul className="grid grid-cols-2 gap-2 rounded-md border bg-muted/40 p-3 font-mono text-sm">
        {codigos.map((c) => (
          <li key={c}>{c}</li>
        ))}
      </ul>
      <div className="flex gap-2">
        <Button variant="outline" className="flex-1" onClick={copiar}>
          Copiar
        </Button>
        <Button variant="outline" className="flex-1" onClick={baixar}>
          Baixar .txt
        </Button>
      </div>
      <Button className="w-full" disabled={!guardou} onClick={aoContinuar}>
        Já guardei, continuar
      </Button>
    </div>
  );
}
