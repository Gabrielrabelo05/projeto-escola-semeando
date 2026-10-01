import type { LucideIcon } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

interface Props {
  titulo: string;
  valor: string | number;
  detalhe?: string;
  icone: LucideIcon;
  destaque?: "alerta" | "normal";
}

export function Kpi({
  titulo,
  valor,
  detalhe,
  icone: Icone,
  destaque = "normal",
}: Props) {
  return (
    <Card size="sm">
      <CardContent className="flex items-start justify-between gap-3">
        <div className="space-y-1">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
            {titulo}
          </p>
          <p
            className={`text-2xl font-semibold tabular-nums ${destaque === "alerta" ? "text-destructive" : ""}`}
          >
            {valor}
          </p>
          {detalhe && (
            <p className="text-xs text-muted-foreground">{detalhe}</p>
          )}
        </div>
        <Icone className="size-5 text-muted-foreground" />
      </CardContent>
    </Card>
  );
}
