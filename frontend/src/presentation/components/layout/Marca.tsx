import { Sprout } from "lucide-react";

export function Marca() {
  return (
    <div className="flex items-center gap-2">
      <span className="flex size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
        <Sprout className="size-4" />
      </span>
      <div className="leading-tight">
        <p className="text-sm font-semibold">Semeando Digital</p>
        <p className="text-[11px] text-muted-foreground">Gestão escolar</p>
      </div>
    </div>
  );
}
