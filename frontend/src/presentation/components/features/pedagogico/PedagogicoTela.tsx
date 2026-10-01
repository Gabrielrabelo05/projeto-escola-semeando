"use client";

import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { CalendarioAba } from "./CalendarioAba";
import { DiarioClasseAba } from "./DiarioClasseAba";
import { FormacaoAba } from "./FormacaoAba";
import { FrequenciaAba } from "./FrequenciaAba";
import { PlanejamentoAba } from "./PlanejamentoAba";
import { PlanosAulaAba } from "./PlanosAulaAba";

/** Módulo 6 – Planejamento pedagógico (RF-39 a RF-46). */
export function PedagogicoTela() {
  return (
    <>
      <PageHeader
        titulo="Pedagógico"
        descricao="Planos de aula, diário de classe, frequência, calendário e formação"
      />
      <Tabs defaultValue="planos">
        <TabsList className="flex-wrap">
          <TabsTrigger value="planos">Planos de aula</TabsTrigger>
          <TabsTrigger value="diario">Diário de classe</TabsTrigger>
          <TabsTrigger value="frequencia">Frequência</TabsTrigger>
          <TabsTrigger value="planejamento">Planejamento diário</TabsTrigger>
          <TabsTrigger value="calendario">Calendário</TabsTrigger>
          <TabsTrigger value="formacao">Formação continuada</TabsTrigger>
        </TabsList>
        <TabsContent value="planos">
          <PlanosAulaAba />
        </TabsContent>
        <TabsContent value="diario">
          <DiarioClasseAba />
        </TabsContent>
        <TabsContent value="frequencia">
          <FrequenciaAba />
        </TabsContent>
        <TabsContent value="planejamento">
          <PlanejamentoAba />
        </TabsContent>
        <TabsContent value="calendario">
          <CalendarioAba />
        </TabsContent>
        <TabsContent value="formacao">
          <FormacaoAba />
        </TabsContent>
      </Tabs>
    </>
  );
}
