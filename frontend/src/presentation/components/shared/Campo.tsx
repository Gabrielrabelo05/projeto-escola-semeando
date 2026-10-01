import type { ComponentProps, ReactNode } from "react";
import {
  Field,
  FieldDescription,
  FieldError,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";

interface CampoProps {
  id: string;
  rotulo: string;
  erro?: string;
  descricao?: string;
  children: ReactNode;
}

export function Campo({ id, rotulo, erro, descricao, children }: CampoProps) {
  return (
    <Field data-invalid={erro ? true : undefined}>
      <FieldLabel htmlFor={id}>{rotulo}</FieldLabel>
      {children}
      {descricao && <FieldDescription>{descricao}</FieldDescription>}
      {erro && <FieldError>{erro}</FieldError>}
    </Field>
  );
}

type InputProps = ComponentProps<typeof Input> & {
  id: string;
  rotulo: string;
  erro?: string;
  descricao?: string;
};

export function CampoTexto({
  id,
  rotulo,
  erro,
  descricao,
  ...props
}: InputProps) {
  return (
    <Campo id={id} rotulo={rotulo} erro={erro} descricao={descricao}>
      <Input id={id} aria-invalid={erro ? true : undefined} {...props} />
    </Campo>
  );
}

type TextareaProps = ComponentProps<typeof Textarea> & {
  id: string;
  rotulo: string;
  erro?: string;
};

export function CampoAreaTexto({ id, rotulo, erro, ...props }: TextareaProps) {
  return (
    <Campo id={id} rotulo={rotulo} erro={erro}>
      <Textarea id={id} aria-invalid={erro ? true : undefined} {...props} />
    </Campo>
  );
}
