"use client";

import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import type { RelatorioFinanceiro } from "@/domain/models/cobranca";
import { moeda } from "@/presentation/lib/formatadores";

const MESES = [
  "jan",
  "fev",
  "mar",
  "abr",
  "mai",
  "jun",
  "jul",
  "ago",
  "set",
  "out",
  "nov",
  "dez",
];

/** RF-29: previsto x recebido ao longo do ano. */
export function GraficoFinanceiro({
  relatorio,
}: {
  relatorio: RelatorioFinanceiro;
}) {
  const dados = relatorio.serieAnual.map((s) => ({
    ...s,
    rotulo: MESES[Number(s.mes.slice(5, 7)) - 1],
  }));
  return (
    <div
      className="h-64 w-full"
      role="img"
      aria-label="Gráfico de valores previstos e recebidos por mês"
    >
      <ResponsiveContainer>
        <BarChart data={dados} margin={{ left: 8, right: 8 }}>
          <CartesianGrid
            vertical={false}
            strokeDasharray="3 3"
            className="stroke-border"
          />
          <XAxis
            dataKey="rotulo"
            tickLine={false}
            axisLine={false}
            fontSize={12}
          />
          <YAxis
            tickLine={false}
            axisLine={false}
            fontSize={12}
            width={70}
            tickFormatter={(v) => moeda(Number(v))}
          />
          <Tooltip
            cursor={{ fill: "var(--muted)" }}
            formatter={(v) => moeda(Number(v))}
          />
          <Legend />
          <Bar
            dataKey="previsto"
            name="Previsto"
            fill="var(--chart-2)"
            radius={[4, 4, 0, 0]}
          />
          <Bar
            dataKey="recebido"
            name="Recebido"
            fill="var(--chart-1)"
            radius={[4, 4, 0, 0]}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
