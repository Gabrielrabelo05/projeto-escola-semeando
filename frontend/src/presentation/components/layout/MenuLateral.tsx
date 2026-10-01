"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { NAVEGACAO } from "@/presentation/lib/navegacao";
import { useAuth } from "@/presentation/providers/AuthProvider";

export function MenuLateral({ aoNavegar }: { aoNavegar?: () => void }) {
  const { usuario } = useAuth();
  const caminho = usePathname();
  const itens = NAVEGACAO.filter(
    (item) => usuario && item.perfis.includes(usuario.perfil),
  );

  return (
    <nav aria-label="Menu principal" className="flex flex-col gap-0.5">
      {itens.map(({ href, titulo, icone: Icone }) => {
        const ativo = caminho === href || caminho.startsWith(`${href}/`);
        return (
          <Link
            key={href}
            href={href}
            onClick={aoNavegar}
            aria-current={ativo ? "page" : undefined}
            className={cn(
              "flex items-center gap-2.5 rounded-lg px-2.5 py-2 text-sm text-muted-foreground transition-colors hover:bg-muted hover:text-foreground",
              ativo && "bg-muted font-medium text-foreground",
            )}
          >
            <Icone className="size-4" />
            {titulo}
          </Link>
        );
      })}
    </nav>
  );
}
