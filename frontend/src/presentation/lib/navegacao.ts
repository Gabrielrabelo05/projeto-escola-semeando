import {
  BookOpen,
  ClipboardList,
  FileSignature,
  GraduationCap,
  HandCoins,
  House,
  KeyRound,
  type LucideIcon,
  Package,
  ScrollText,
  ShieldCheck,
  UserCog,
  Users,
} from "lucide-react";
import type { Perfil } from "@/domain/models/comum";

export interface ItemNavegacao {
  href: string;
  titulo: string;
  icone: LucideIcon;
  perfis: Perfil[];
}

const GESTAO: Perfil[] = ["ADMIN", "DIRETOR"];
const SECRETARIA: Perfil[] = [...GESTAO, "SECRETARIA"];

/** Menu por perfil – espelha o RBAC do backend (RF-35). */
export const NAVEGACAO: ItemNavegacao[] = [
  {
    href: "/painel",
    titulo: "Painel",
    icone: House,
    perfis: [...SECRETARIA, "FINANCEIRO", "PROFESSOR"],
  },
  {
    href: "/portal",
    titulo: "Meus filhos",
    icone: House,
    perfis: ["RESPONSAVEL"],
  },
  {
    href: "/matriculas",
    titulo: "Matrículas",
    icone: ClipboardList,
    perfis: SECRETARIA,
  },
  {
    href: "/alunos",
    titulo: "Alunos",
    icone: GraduationCap,
    perfis: SECRETARIA,
  },
  {
    href: "/contratos",
    titulo: "Contratos",
    icone: FileSignature,
    perfis: SECRETARIA,
  },
  {
    href: "/financeiro",
    titulo: "Financeiro",
    icone: HandCoins,
    perfis: [...GESTAO, "FINANCEIRO", "SECRETARIA"],
  },
  { href: "/cadastros", titulo: "Cadastros", icone: Users, perfis: SECRETARIA },
  {
    href: "/pedagogico",
    titulo: "Pedagógico",
    icone: BookOpen,
    perfis: [...GESTAO, "PROFESSOR"],
  },
  {
    href: "/materiais",
    titulo: "Materiais",
    icone: Package,
    perfis: SECRETARIA,
  },
  {
    href: "/obrigacoes",
    titulo: "Obrigações",
    icone: ScrollText,
    perfis: [...SECRETARIA, "PRESTADOR"],
  },
  { href: "/usuarios", titulo: "Usuários", icone: UserCog, perfis: ["ADMIN"] },
  {
    href: "/auditoria",
    titulo: "Auditoria",
    icone: ShieldCheck,
    perfis: GESTAO,
  },
  {
    href: "/conta",
    titulo: "Minha conta",
    icone: KeyRound,
    perfis: [
      ...SECRETARIA,
      "FINANCEIRO",
      "PROFESSOR",
      "RESPONSAVEL",
      "PRESTADOR",
    ],
  },
];

export function rotaInicial(perfil: Perfil): string {
  if (perfil === "RESPONSAVEL") return "/portal";
  if (perfil === "PRESTADOR") return "/obrigacoes";
  return "/painel";
}
