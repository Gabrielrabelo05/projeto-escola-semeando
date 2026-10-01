"use client";

import { LogOut, Menu } from "lucide-react";
import { useRouter } from "next/navigation";
import { type ReactNode, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { rotulo } from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { BuscaGlobal } from "./BuscaGlobal";
import { Marca } from "./Marca";
import { MenuLateral } from "./MenuLateral";
import { Notificacoes } from "./Notificacoes";

export function AppShell({ children }: { children: ReactNode }) {
  const { usuario, sair, possuiPerfil } = useAuth();
  const router = useRouter();
  const [menuAberto, setMenuAberto] = useState(false);
  const equipe = possuiPerfil(
    "ADMIN",
    "DIRETOR",
    "SECRETARIA",
    "FINANCEIRO",
    "PROFESSOR",
  );

  function encerrar() {
    sair();
    router.replace("/login");
  }

  return (
    <div className="flex min-h-screen bg-muted/30">
      <aside className="hidden w-60 shrink-0 flex-col gap-6 border-r bg-background p-4 md:flex">
        <Marca />
        <MenuLateral />
      </aside>

      <Sheet open={menuAberto} onOpenChange={setMenuAberto}>
        <SheetContent side="left" className="w-64 p-4">
          <SheetHeader className="p-0">
            <SheetTitle>
              <Marca />
            </SheetTitle>
          </SheetHeader>
          <MenuLateral aoNavegar={() => setMenuAberto(false)} />
        </SheetContent>
      </Sheet>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-30 flex h-14 items-center gap-2 border-b bg-background/95 px-4 backdrop-blur">
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden"
            aria-label="Abrir menu"
            onClick={() => setMenuAberto(true)}
          >
            <Menu />
          </Button>
          <div className="flex-1">{equipe && <BuscaGlobal />}</div>
          <Notificacoes />
          <div className="hidden text-right text-xs leading-tight sm:block">
            <p className="font-medium">{usuario?.nome}</p>
            <p className="text-muted-foreground">{rotulo(usuario?.perfil)}</p>
          </div>
          <Button
            variant="ghost"
            size="icon"
            aria-label="Sair"
            onClick={encerrar}
          >
            <LogOut />
          </Button>
        </header>
        <main className="mx-auto w-full max-w-7xl flex-1 space-y-6 p-4 md:p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
