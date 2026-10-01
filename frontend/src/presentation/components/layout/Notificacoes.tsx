"use client";

import { Bell } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { dataHora } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

export function Notificacoes() {
  const { notificacao } = useGateways();
  const estado = useCarregar(() => notificacao.minhas(true), []);
  const naoLidas = estado.dados ?? [];

  async function marcar(id: string) {
    await notificacao.marcarComoLida(id);
    await estado.recarregar();
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        render={
          <Button
            variant="ghost"
            size="icon"
            aria-label={`Notificações (${naoLidas.length} não lidas)`}
          >
            <span className="relative">
              <Bell />
              {naoLidas.length > 0 && (
                <span className="absolute -top-1 -right-1 size-2 rounded-full bg-destructive" />
              )}
            </span>
          </Button>
        }
      />
      <DropdownMenuContent align="end" className="w-80">
        <DropdownMenuGroup>
          <DropdownMenuLabel>Notificações</DropdownMenuLabel>
        </DropdownMenuGroup>
        <DropdownMenuSeparator />
        {naoLidas.length === 0 && (
          <p className="px-2 py-3 text-sm text-muted-foreground">
            Tudo em dia.
          </p>
        )}
        {naoLidas.slice(0, 8).map((n) => (
          <DropdownMenuItem
            key={n.id}
            onClick={() => marcar(n.id)}
            className="flex-col items-start gap-0.5"
          >
            <span className="font-medium">{n.titulo}</span>
            <span className="line-clamp-2 text-xs text-muted-foreground">
              {n.mensagem}
            </span>
            <span className="text-[11px] text-muted-foreground">
              {dataHora(n.criadaEm)}
            </span>
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
