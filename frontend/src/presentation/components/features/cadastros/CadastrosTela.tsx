"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { Suspense } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { FuncionariosAba } from "./FuncionariosAba";
import { PrestadoresAba } from "./PrestadoresAba";
import { ProfessoresAba } from "./ProfessoresAba";
import { TurmasAba } from "./TurmasAba";

/** Módulo 5 – Cadastros gerais (RF-32 a RF-34) e turmas. */
export function CadastrosTela() {
  return (
    <Suspense>
      <Cadastros />
    </Suspense>
  );
}

function Cadastros() {
  const parametros = useSearchParams();
  const router = useRouter();
  const aba = parametros.get("aba") ?? "professores";
  return (
    <>
      <PageHeader
        titulo="Cadastros"
        descricao="Registros nunca são excluídos – apenas inativados, mantendo o histórico (RF-37)"
      />
      <Tabs
        value={aba}
        onValueChange={(v) => router.replace(`/cadastros?aba=${v}`)}
      >
        <TabsList>
          <TabsTrigger value="professores">Professores</TabsTrigger>
          <TabsTrigger value="funcionarios">Funcionários</TabsTrigger>
          <TabsTrigger value="prestadores">Prestadores</TabsTrigger>
          <TabsTrigger value="turmas">Turmas</TabsTrigger>
        </TabsList>
        <TabsContent value="professores">
          <ProfessoresAba />
        </TabsContent>
        <TabsContent value="funcionarios">
          <FuncionariosAba />
        </TabsContent>
        <TabsContent value="prestadores">
          <PrestadoresAba />
        </TabsContent>
        <TabsContent value="turmas">
          <TurmasAba />
        </TabsContent>
      </Tabs>
    </>
  );
}
