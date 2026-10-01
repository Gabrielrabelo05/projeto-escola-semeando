"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { CampoTexto } from "@/presentation/components/shared/Campo";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { data, hojeISO } from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";

/** RF-43: registro de capacitações. */
export function FormacaoAba() {
  const { pedagogico } = useGateways();
  const { possuiPerfil } = useAuth();
  const estado = useCarregar(() => pedagogico.formacoes(), []);
  const [form, setForm] = useState({
    tema: "",
    data: hojeISO(),
    cargaHoraria: "4",
    instituicao: "",
  });
  const registrar = useAcao(async () => {
    await pedagogico.registrarFormacao({
      ...form,
      cargaHoraria: Number(form.cargaHoraria),
    });
    setForm({ tema: "", data: hojeISO(), cargaHoraria: "4", instituicao: "" });
    await estado.recarregar();
  }, "Formação registrada");
  const campo = (nome: keyof typeof form) => ({
    value: form[nome],
    onChange: (e: React.ChangeEvent<HTMLInputElement>) =>
      setForm((f) => ({ ...f, [nome]: e.target.value })),
  });

  return (
    <div className="space-y-4">
      {possuiPerfil("PROFESSOR") && (
        <div className="grid max-w-3xl items-end gap-2 sm:grid-cols-[2fr_1fr_1fr_2fr_auto]">
          <CampoTexto id="form-tema" rotulo="Tema" {...campo("tema")} />
          <CampoTexto
            id="form-data"
            rotulo="Data"
            type="date"
            {...campo("data")}
          />
          <CampoTexto id="form-ch" rotulo="Horas" {...campo("cargaHoraria")} />
          <CampoTexto
            id="form-inst"
            rotulo="Instituição"
            {...campo("instituicao")}
          />
          <Button
            disabled={!form.tema || !form.instituicao}
            onClick={() => registrar.executar()}
          >
            Registrar
          </Button>
        </div>
      )}
      <Conteudo
        estado={estado}
        vazio={(l) => l.length === 0}
        mensagemVazio="Nenhuma formação registrada."
      >
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(f) => f.id}
            colunas={[
              { titulo: "Tema", celula: (f) => f.tema },
              { titulo: "Data", celula: (f) => data(f.data) },
              { titulo: "Carga horária", celula: (f) => `${f.cargaHoraria}h` },
              { titulo: "Instituição", celula: (f) => f.instituicao },
            ]}
          />
        )}
      </Conteudo>
    </div>
  );
}
