"use client";

import {
  type DependencyList,
  useCallback,
  useEffect,
  useRef,
  useState,
} from "react";
import { mensagemDeErro } from "@/domain/models/erro";

interface Estado<T> {
  dados: T | undefined;
  carregando: boolean;
  erro: string | null;
  recarregar: () => Promise<void>;
}

/** Executa uma consulta de gateway e mantém dados/carregando/erro sincronizados com as dependências. */
export function useCarregar<T>(
  consulta: () => Promise<T>,
  deps: DependencyList,
  ativo = true,
): Estado<T> {
  const [dados, setDados] = useState<T>();
  const [carregando, setCarregando] = useState(ativo);
  const [erro, setErro] = useState<string | null>(null);
  const consultaRef = useRef(consulta);
  consultaRef.current = consulta;

  const recarregar = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      setDados(await consultaRef.current());
    } catch (e) {
      setErro(mensagemDeErro(e));
    } finally {
      setCarregando(false);
    }
  }, []);

  useEffect(() => {
    if (ativo) void recarregar();
  }, [ativo, recarregar, ...deps]);

  return { dados, carregando, erro, recarregar };
}
