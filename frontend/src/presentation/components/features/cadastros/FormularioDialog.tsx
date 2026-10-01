"use client";

import type { ReactNode } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { FieldGroup } from "@/components/ui/field";

interface Props {
  aberto: boolean;
  titulo: string;
  aoFechar: () => void;
  aoEnviar: () => void;
  enviando: boolean;
  children: ReactNode;
}

/** Diálogo padrão de cadastro/edição. */
export function FormularioDialog({
  aberto,
  titulo,
  aoFechar,
  aoEnviar,
  enviando,
  children,
}: Props) {
  return (
    <Dialog open={aberto} onOpenChange={(a) => !a && aoFechar()}>
      <DialogContent className="sm:max-w-lg">
        <form
          noValidate
          onSubmit={(e) => {
            e.preventDefault();
            aoEnviar();
          }}
          className="grid gap-4"
        >
          <DialogHeader>
            <DialogTitle>{titulo}</DialogTitle>
          </DialogHeader>
          <FieldGroup className="max-h-[60vh] overflow-y-auto pr-1">
            {children}
          </FieldGroup>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={aoFechar}>
              Cancelar
            </Button>
            <Button type="submit" disabled={enviando}>
              Salvar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
