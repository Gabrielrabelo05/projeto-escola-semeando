"use client";

import { type ReactNode, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";

interface Props {
  aberto: boolean;
  aoFechar: () => void;
  titulo: string;
  descricao?: ReactNode;
  rotuloConfirmar?: string;
  destrutivo?: boolean;
  /** Quando informado, exige um texto (ex.: justificativa) antes de confirmar. */
  pedirTexto?: string;
  aoConfirmar: (texto: string) => Promise<unknown> | unknown;
}

export function Confirmacao({
  aberto,
  aoFechar,
  titulo,
  descricao,
  rotuloConfirmar = "Confirmar",
  destrutivo,
  pedirTexto,
  aoConfirmar,
}: Props) {
  const [texto, setTexto] = useState("");
  const [enviando, setEnviando] = useState(false);
  const bloqueado =
    enviando || (pedirTexto !== undefined && texto.trim().length === 0);

  async function confirmar() {
    setEnviando(true);
    try {
      await aoConfirmar(texto.trim());
      setTexto("");
      aoFechar();
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Dialog open={aberto} onOpenChange={(abrir) => !abrir && aoFechar()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{titulo}</DialogTitle>
          {descricao && <DialogDescription>{descricao}</DialogDescription>}
        </DialogHeader>
        {pedirTexto !== undefined && (
          <Textarea
            aria-label={pedirTexto}
            placeholder={pedirTexto}
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
          />
        )}
        <DialogFooter>
          <Button variant="outline" onClick={aoFechar}>
            Cancelar
          </Button>
          <Button
            variant={destrutivo ? "destructive" : "default"}
            disabled={bloqueado}
            onClick={confirmar}
          >
            {rotuloConfirmar}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
