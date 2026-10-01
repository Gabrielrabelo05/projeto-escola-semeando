import { AreaRestrita } from "@/presentation/components/layout/AreaRestrita";

export default function AreaRestritaLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <AreaRestrita>{children}</AreaRestrita>;
}
