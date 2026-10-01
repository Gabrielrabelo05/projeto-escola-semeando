import type { ReactNode } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

export interface Coluna<T> {
  titulo: string;
  celula: (linha: T) => ReactNode;
  className?: string;
}

interface Props<T> {
  colunas: Coluna<T>[];
  linhas: T[];
  chave: (linha: T) => string;
  aoClicar?: (linha: T) => void;
}

/** Tabela genérica baseada no componente Table do shadcn. */
export function Tabela<T>({ colunas, linhas, chave, aoClicar }: Props<T>) {
  return (
    <div className="overflow-x-auto rounded-lg border">
      <Table>
        <TableHeader>
          <TableRow>
            {colunas.map((coluna) => (
              <TableHead key={coluna.titulo} className={coluna.className}>
                {coluna.titulo}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {linhas.map((linha) => (
            <TableRow
              key={chave(linha)}
              className={aoClicar ? "cursor-pointer" : undefined}
              onClick={aoClicar ? () => aoClicar(linha) : undefined}
            >
              {colunas.map((coluna) => (
                <TableCell key={coluna.titulo} className={coluna.className}>
                  {coluna.celula(linha)}
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
