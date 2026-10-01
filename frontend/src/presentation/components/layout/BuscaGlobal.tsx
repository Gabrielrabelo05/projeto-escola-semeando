"use client";

import { Search } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import type { ResultadoBusca } from "@/domain/models/cadastro";
import { rotulo } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

const DESTINO: Record<ResultadoBusca["tipo"], (id: string) => string> = {
  ALUNO: (id) => `/alunos/${id}`,
  PROFESSOR: () => "/cadastros?aba=professores",
  FUNCIONARIO: () => "/cadastros?aba=funcionarios",
};

/** RF-38: busca unificada por nome, CPF ou matrícula. */
export function BuscaGlobal() {
  const { cadastro } = useGateways();
  const router = useRouter();
  const [termo, setTermo] = useState("");
  const [resultados, setResultados] = useState<ResultadoBusca[]>([]);

  useEffect(() => {
    if (termo.trim().length < 2) {
      setResultados([]);
      return;
    }
    const temporizador = setTimeout(() => {
      cadastro
        .buscar(termo)
        .then(setResultados)
        .catch(() => setResultados([]));
    }, 300);
    return () => clearTimeout(temporizador);
  }, [termo, cadastro]);

  function abrir(resultado: ResultadoBusca) {
    setTermo("");
    router.push(DESTINO[resultado.tipo](resultado.id));
  }

  return (
    <div className="relative max-w-md">
      <Search className="pointer-events-none absolute top-2 left-2.5 size-4 text-muted-foreground" />
      <Input
        aria-label="Buscar alunos, professores ou funcionários"
        placeholder="Buscar por nome ou CPF…"
        className="pl-8"
        value={termo}
        onChange={(e) => setTermo(e.target.value)}
      />
      {resultados.length > 0 && (
        <ul className="absolute z-40 mt-1 w-full overflow-hidden rounded-lg border bg-popover shadow-md">
          {resultados.slice(0, 8).map((r) => (
            <li key={`${r.tipo}-${r.id}`}>
              <button
                type="button"
                onClick={() => abrir(r)}
                className="flex w-full items-center justify-between gap-2 px-3 py-2 text-left text-sm hover:bg-muted"
              >
                <span className="font-medium">{r.nome}</span>
                <span className="text-xs text-muted-foreground">
                  {rotulo(r.tipo)} · {r.detalhe}
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
