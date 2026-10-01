"use client";

import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import type { AuthGateway } from "@/domain/gateways";
import type { Credenciais, Usuario } from "@/domain/models/auth";
import type { Perfil } from "@/domain/models/comum";
import type { TokenStorage } from "@/infrastructure/auth/TokenStorage";

interface AuthContexto {
  usuario: Usuario | null;
  carregando: boolean;
  entrar(credenciais: Credenciais): Promise<Usuario>;
  sair(): void;
  possuiPerfil(...perfis: Perfil[]): boolean;
}

const Contexto = createContext<AuthContexto | null>(null);

interface Props {
  gateway: AuthGateway;
  tokens: TokenStorage;
  children: ReactNode;
}

export function AuthProvider({ gateway, tokens, children }: Props) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    setUsuario(tokens.ler()?.usuario ?? null);
    setCarregando(false);
  }, [tokens]);

  const entrar = useCallback(
    async (credenciais: Credenciais) => {
      const sessao = await gateway.entrar(credenciais);
      tokens.salvar(sessao);
      setUsuario(sessao.usuario);
      return sessao.usuario;
    },
    [gateway, tokens],
  );

  const sair = useCallback(() => {
    tokens.limpar();
    setUsuario(null);
  }, [tokens]);

  const possuiPerfil = useCallback(
    (...perfis: Perfil[]) =>
      usuario !== null && perfis.includes(usuario.perfil),
    [usuario],
  );

  const valor = useMemo(
    () => ({ usuario, carregando, entrar, sair, possuiPerfil }),
    [usuario, carregando, entrar, sair, possuiPerfil],
  );
  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>;
}

export function useAuth(): AuthContexto {
  const contexto = useContext(Contexto);
  if (!contexto)
    throw new Error("useAuth deve ser usado dentro de <AuthProvider>");
  return contexto;
}
