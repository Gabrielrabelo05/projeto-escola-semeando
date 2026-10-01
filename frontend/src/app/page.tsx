"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { rotaInicial } from "@/presentation/lib/navegacao";
import { useAuth } from "@/presentation/providers/AuthProvider";

export default function Inicio() {
  const { usuario, carregando } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!carregando)
      router.replace(usuario ? rotaInicial(usuario.perfil) : "/login");
  }, [carregando, usuario, router]);

  return null;
}
