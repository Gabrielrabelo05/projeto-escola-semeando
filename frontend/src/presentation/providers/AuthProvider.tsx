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
  children: ReactNode;
}

/** Versões anteriores guardavam o token no localStorage; ele é removido para não ficar exposto. */
const CHAVE_LEGADA = "semeando.sessao";

/**
 * O token fica num cookie HttpOnly, invisível ao JavaScript. Ao abrir, o front pergunta à API quem é o usuário
 * da sessão; sair pede à API para apagar o cookie.
 */
export function AuthProvider({ gateway, children }: Props) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    try {
      window.localStorage.removeItem(CHAVE_LEGADA);
    } catch {
      // armazenamento indisponível (modo privado): nada a limpar
    }
    let ativo = true;
    gateway
      .sessaoAtual()
      .then((atual) => ativo && setUsuario(atual))
      .catch(() => ativo && setUsuario(null))
      .finally(() => ativo && setCarregando(false));
    return () => {
      ativo = false;
    };
  }, [gateway]);

  const entrar = useCallback(
    async (credenciais: Credenciais) => {
      const sessao = await gateway.entrar(credenciais);
      setUsuario(sessao.usuario);
      return sessao.usuario;
    },
    [gateway],
  );

  const sair = useCallback(() => {
    setUsuario(null);
    void gateway.sair().catch(() => {});
  }, [gateway]);

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
