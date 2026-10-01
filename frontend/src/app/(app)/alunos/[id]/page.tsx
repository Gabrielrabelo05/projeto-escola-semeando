import { FichaAlunoTela } from "@/presentation/components/features/alunos/FichaAlunoTela";

export default async function Page({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <FichaAlunoTela id={id} />;
}
