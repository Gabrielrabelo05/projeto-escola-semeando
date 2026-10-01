"use client";

import { FileSignature } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import type { Contrato } from "@/domain/models/contrato";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { dataHora, moeda, rotulo } from "@/presentation/lib/formatadores";

interface Props {
  contrato: Contrato | null;
  aoFechar: () => void;
  aoAssinar?: (contrato: Contrato) => void;
  acoesExtras?: React.ReactNode;
}

/** Exibe o documento com as evidências de assinatura (RF-15) e integridade (hash). */
export function ContratoVisualizacao({
  contrato,
  aoFechar,
  aoAssinar,
  acoesExtras,
}: Props) {
  return (
    <Dialog open={contrato !== null} onOpenChange={(a) => !a && aoFechar()}>
      <DialogContent className="sm:max-w-2xl">
        {contrato && (
          <>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                {rotulo(contrato.tipo)} · v{contrato.versao}{" "}
                <StatusBadge status={contrato.status} />
              </DialogTitle>
              <DialogDescription>
                Gerado em {dataHora(contrato.geradoEm)} por {contrato.geradoPor}
                {contrato.valorTotal !== null &&
                  ` · ${contrato.numeroParcelas}x ${moeda(contrato.valorParcela)} = ${moeda(contrato.valorTotal)}`}
              </DialogDescription>
            </DialogHeader>
            <pre className="max-h-80 overflow-auto rounded-lg bg-muted p-3 font-mono text-xs whitespace-pre-wrap">
              {contrato.conteudo}
            </pre>
            <dl className="grid gap-1 text-xs text-muted-foreground">
              <div>
                <dt className="inline font-medium">Hash SHA-256: </dt>
                <dd className="inline break-all font-mono">
                  {contrato.hashDocumento}
                </dd>
              </div>
              {contrato.assinatura && (
                <div>
                  <dt className="inline font-medium">Assinado: </dt>
                  <dd className="inline">
                    {contrato.assinatura.assinante} em{" "}
                    {dataHora(contrato.assinatura.assinadoEm)} · IP{" "}
                    {contrato.assinatura.enderecoIp}
                  </dd>
                </div>
              )}
            </dl>
            <DialogFooter>
              {acoesExtras}
              {aoAssinar && contrato.status === "AGUARDANDO_ASSINATURA" && (
                <Button onClick={() => aoAssinar(contrato)}>
                  <FileSignature /> Assinar eletronicamente
                </Button>
              )}
            </DialogFooter>
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}
