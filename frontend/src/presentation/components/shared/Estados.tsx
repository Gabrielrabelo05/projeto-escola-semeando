import { AlertTriangle, Inbox } from "lucide-react";
import type { ReactNode } from "react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";

export function Carregando({ linhas = 4 }: { linhas?: number }) {
  return (
    <output
      className="block space-y-2"
      aria-busy="true"
      aria-label="Carregando"
    >
      {Array.from({ length: linhas }, (_, i) => (
        // biome-ignore lint/suspicious/noArrayIndexKey: placeholders estáticos
        <Skeleton key={i} className="h-9 w-full" />
      ))}
    </output>
  );
}

export function ErroCarregamento({ mensagem }: { mensagem: string }) {
  return (
    <Alert variant="destructive">
      <AlertTriangle />
      <AlertTitle>Não foi possível carregar</AlertTitle>
      <AlertDescription>{mensagem}</AlertDescription>
    </Alert>
  );
}

export function Vazio({ children }: { children: ReactNode }) {
  return (
    <div className="flex flex-col items-center gap-2 rounded-lg border border-dashed py-10 text-sm text-muted-foreground">
      <Inbox className="size-6" />
      {children}
    </div>
  );
}

interface ConteudoProps<T> {
  estado: { dados: T | undefined; carregando: boolean; erro: string | null };
  vazio?: (dados: T) => boolean;
  mensagemVazio?: string;
  children: (dados: T) => ReactNode;
}

/** Renderiza carregando / erro / vazio / conteúdo de forma uniforme. */
export function Conteudo<T>({
  estado,
  vazio,
  mensagemVazio = "Nenhum registro encontrado.",
  children,
}: ConteudoProps<T>) {
  if (estado.carregando && estado.dados === undefined) return <Carregando />;
  if (estado.erro) return <ErroCarregamento mensagem={estado.erro} />;
  if (estado.dados === undefined) return null;
  if (vazio?.(estado.dados)) return <Vazio>{mensagemVazio}</Vazio>;
  return <>{children(estado.dados)}</>;
}
