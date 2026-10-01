"use client";

import { createContext, type ReactNode, useContext } from "react";
import type { Gateways } from "@/domain/gateways";

const GatewayContext = createContext<Gateways | null>(null);

export function GatewayProvider({
  gateways,
  children,
}: {
  gateways: Gateways;
  children: ReactNode;
}) {
  return (
    <GatewayContext.Provider value={gateways}>
      {children}
    </GatewayContext.Provider>
  );
}

/** Acesso às portas de saída. Componentes nunca chamam HTTP diretamente – apenas métodos de gateway. */
export function useGateways(): Gateways {
  const gateways = useContext(GatewayContext);
  if (!gateways)
    throw new Error("useGateways deve ser usado dentro de <GatewayProvider>");
  return gateways;
}
