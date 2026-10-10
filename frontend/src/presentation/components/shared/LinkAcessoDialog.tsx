"use client";

import { Copy, MessageCircle } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import type { ConviteAcesso } from "@/domain/models/auth";
import { data } from "@/presentation/lib/formatadores";

interface Props {
  convite: ConviteAcesso | null;
  aoFechar: () => void;
}

/**
 * Mostra o link para a pessoa definir a própria senha. Enquanto o envio de e-mail é simulado, quem convidou
 * repassa o link (copiando ou pelo WhatsApp).
 */
export function LinkAcessoDialog({ convite, aoFechar }: Props) {
  const mensagem = convite
    ? `Olá, ${convite.usuario.nome}! Este é o seu acesso ao Semeando Digital. Defina sua senha neste link (válido até ${data(convite.expiraEm)}): ${convite.link}`
    : "";
  async function copiar() {
    if (!convite) return;
    await navigator.clipboard.writeText(convite.link);
    toast.success("Link copiado");
  }
  return (
    <Dialog open={convite !== null} onOpenChange={(a) => !a && aoFechar()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Link de acesso criado</DialogTitle>
          <DialogDescription>
            Envie este link para {convite?.usuario.nome} (
            {convite?.usuario.email}). Ao abrir, a pessoa cria a própria senha.
            O link vale uma única vez, até {convite && data(convite.expiraEm)}.
          </DialogDescription>
        </DialogHeader>
        <Input
          readOnly
          aria-label="Link de acesso"
          value={convite?.link ?? ""}
          onFocus={(e) => e.target.select()}
        />
        <DialogFooter>
          <Button
            variant="outline"
            nativeButton={false}
            render={
              // biome-ignore lint/a11y/useAnchorContent: o conteúdo vem do Button
              <a
                href={`https://wa.me/?text=${encodeURIComponent(mensagem)}`}
                target="_blank"
                rel="noreferrer"
              />
            }
          >
            <MessageCircle /> Enviar pelo WhatsApp
          </Button>
          <Button onClick={copiar}>
            <Copy /> Copiar link
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
