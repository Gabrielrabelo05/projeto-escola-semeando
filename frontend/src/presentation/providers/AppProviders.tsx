"use client";

import { type ReactNode, useState } from "react";
import { Toaster } from "@/components/ui/sonner";
import { API_URL, criarGateways } from "@/infrastructure/di/container";
import { AxiosHttpClient } from "@/infrastructure/http/AxiosHttpClient";
import { AuthProvider } from "./AuthProvider";
import { GatewayProvider } from "./GatewayProvider";

/** Páginas abertas sem login: uma sessão expirada não deve tirar a pessoa delas. */
const PAGINAS_PUBLICAS = ["/", "/login", "/pre-matricula", "/definir-senha"];

function montarDependencias() {
  const http = new AxiosHttpClient(API_URL, () => {
    if (
      typeof window !== "undefined" &&
      !PAGINAS_PUBLICAS.includes(window.location.pathname)
    ) {
      window.location.assign("/login");
    }
  });
  return { gateways: criarGateways(http) };
}

export function AppProviders({ children }: { children: ReactNode }) {
  const [{ gateways }] = useState(montarDependencias);
  return (
    <GatewayProvider gateways={gateways}>
      <AuthProvider gateway={gateways.auth}>
        {children}
        <Toaster richColors position="top-right" />
      </AuthProvider>
    </GatewayProvider>
  );
}
