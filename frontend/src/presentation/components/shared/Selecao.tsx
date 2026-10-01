"use client";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

export interface Opcao {
  valor: string;
  rotulo: string;
}

interface Props {
  id?: string;
  valor: string | undefined;
  opcoes: Opcao[];
  aoMudar: (valor: string) => void;
  placeholder?: string;
  invalido?: boolean;
  className?: string;
}

/** Select do shadcn (Base UI) com rótulos legíveis no gatilho. */
export function Selecao({
  id,
  valor,
  opcoes,
  aoMudar,
  placeholder = "Selecione",
  invalido,
  className,
}: Props) {
  const rotuloDe = (v: unknown) =>
    opcoes.find((o) => o.valor === v)?.rotulo ?? placeholder;
  return (
    <Select
      value={valor ?? null}
      onValueChange={(v) => v !== null && aoMudar(String(v))}
    >
      <SelectTrigger
        id={id}
        className={className ?? "w-full"}
        aria-invalid={invalido ? true : undefined}
      >
        <SelectValue>{(v: unknown) => rotuloDe(v)}</SelectValue>
      </SelectTrigger>
      <SelectContent>
        {opcoes.map((opcao) => (
          <SelectItem key={opcao.valor} value={opcao.valor}>
            {opcao.rotulo}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
