"use client";

import { type ReactNode, useState } from "react";
import { Toaster } from "@/components/ui/sonner";
import { LocalStorageTokenStorage } from "@/infrastructure/auth/TokenStorage";
import { API_URL, criarGateways } from "@/infrastructure/di/container";
import { AxiosHttpClient } from "@/infrastructure/http/AxiosHttpClient";
import { AuthProvider } from "./AuthProvider";
import { GatewayProvider } from "./GatewayProvider";

function montarDependencias() {
  const tokens = new LocalStorageTokenStorage();
  const http = new AxiosHttpClient(API_URL, tokens, () => {
    tokens.limpar();
    if (
      typeof window !== "undefined" &&
      !window.location.pathname.startsWith("/login")
    ) {
      window.location.assign("/login");
    }
  });
  return { tokens, gateways: criarGateways(http) };
}

export function AppProviders({ children }: { children: ReactNode }) {
  const [{ tokens, gateways }] = useState(montarDependencias);
  return (
    <GatewayProvider gateways={gateways}>
      <AuthProvider gateway={gateways.auth} tokens={tokens}>
        {children}
        <Toaster richColors position="top-right" />
      </AuthProvider>
    </GatewayProvider>
  );
}
