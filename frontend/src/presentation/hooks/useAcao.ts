"use client";

import { useCallback, useState } from "react";
import { toast } from "sonner";
import { mensagemDeErro } from "@/domain/models/erro";

/** Envolve um comando de gateway com estado "executando" e feedback via toast. */
export function useAcao<A extends unknown[], R>(
  comando: (...args: A) => Promise<R>,
  mensagemSucesso?: string,
) {
  const [executando, setExecutando] = useState(false);

  const executar = useCallback(
    async (...args: A): Promise<R | undefined> => {
      setExecutando(true);
      try {
        const resultado = await comando(...args);
        if (mensagemSucesso) toast.success(mensagemSucesso);
        return resultado;
      } catch (erro) {
        toast.error(mensagemDeErro(erro));
        return undefined;
      } finally {
        setExecutando(false);
      }
    },
    [comando, mensagemSucesso],
  );

  return { executar, executando };
}
