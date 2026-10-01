import { Badge } from "@/components/ui/badge";
import { rotulo } from "@/presentation/lib/formatadores";

type Variante = "default" | "secondary" | "destructive" | "outline";

const VARIANTES: Record<string, Variante> = {
  ATIVA: "default",
  ATIVO: "default",
  APROVADA: "default",
  APROVADO: "default",
  ASSINADO: "default",
  PAGA: "default",
  PUBLICADA: "default",
  CONCLUIDA: "default",
  AGUARDANDO_ANALISE: "secondary",
  AGUARDANDO_ASSINATURA: "secondary",
  PRE_MATRICULA: "secondary",
  EMITIDA: "secondary",
  SUBMETIDO: "secondary",
  EM_ANDAMENTO: "secondary",
  RASCUNHO: "outline",
  PENDENTE: "outline",
  SUBSTITUIDO: "outline",
  ARQUIVADA: "outline",
  REJEITADA: "destructive",
  CANCELADA: "destructive",
  CANCELADO: "destructive",
  VENCIDA: "destructive",
  DEVOLVIDO: "destructive",
  INATIVO: "destructive",
};

export function StatusBadge({ status }: { status: string }) {
  return (
    <Badge variant={VARIANTES[status] ?? "outline"}>{rotulo(status)}</Badge>
  );
}
