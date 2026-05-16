// Thin wrapper — jobDiv 기준으로 main-sea / main-air 분기 (House main-tab.tsx 패턴 정합)
import type { MasterVariantConfig } from "@/lib/bl-variants";
import type { UseFormReturn }       from "react-hook-form";
import type { MasterBlFormValues }  from "../master-bl-schema";
import { MasterMainTabSea }         from "./main-sea";
import { MasterMainTabAir }         from "./main-air";

interface Props {
  variant: MasterVariantConfig;
  form:    UseFormReturn<MasterBlFormValues>;
  active?: boolean;
}

export function MasterMainTab({ variant, form, active }: Props) {
  if (variant.mode === "AIR") {
    return <MasterMainTabAir variant={variant} active={active} />;
  }
  return <MasterMainTabSea variant={variant} form={form} active={active} />;
}
