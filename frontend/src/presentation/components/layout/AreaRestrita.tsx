"use client";

import { usePathname, useRouter } from "next/navigation";
import { type ReactNode, useEffect } from "react";
import { NAVEGACAO, rotaInicial } from "@/presentation/lib/navegacao";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { Carregando } from "../shared/Estados";
import { AppShell } from "./AppShell";

/** Exige autenticação e respeita o perfil para cada rota do menu (RF-35). */
export function AreaRestrita({ children }: { children: ReactNode }) {
  const { usuario, carregando } = useAuth();
  const router = useRouter();
  const caminho = usePathname();
  const item = NAVEGACAO.find(
    (n) => caminho === n.href || caminho.startsWith(`${n.href}/`),
  );
  const permitido =
    !!usuario && (!item || item.perfis.includes(usuario.perfil));

  useEffect(() => {
    if (carregando) return;
    if (!usuario) router.replace("/login");
    else if (!permitido) router.replace(rotaInicial(usuario.perfil));
  }, [carregando, usuario, permitido, router]);

  if (carregando || !permitido) {
    return (
      <div className="mx-auto max-w-md p-10">
        <Carregando />
      </div>
    );
  }
  return <AppShell>{children}</AppShell>;
}
