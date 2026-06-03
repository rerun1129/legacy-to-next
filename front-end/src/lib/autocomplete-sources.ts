import type { AutocompleteSource } from "./use-code-autocomplete";
import { customerUseCases } from "@/application/customer/use-cases";
import { carrierUseCases } from "@/application/code/carrier/use-cases";
import { portUseCases } from "@/application/code/port/use-cases";
import { userUseCases } from "@/application/user/use-cases";
import { packageUnitUseCases } from "@/application/code/package-unit/use-cases";
import { hsCodeUseCases } from "@/application/code/hs-code/use-cases";
import { currencyUseCases } from "@/application/code/currency/use-cases";
import { teamUseCases } from "@/application/team/use-cases";
import { subscriberUseCases } from "@/application/subscriber/use-cases";
import { freightUseCases } from "@/application/code/freight/use-cases";

export const CODE_SOURCES = {
  customer:    { key: "ac-customer",         fetch: (q: string) => customerUseCases.autocomplete(q, undefined, "CUSTOMER") },
  partner:     { key: "ac-customer-partner", fetch: (q: string) => customerUseCases.autocomplete(q, undefined, "PARTNER") },
  trucker:     { key: "ac-customer-trucker", fetch: (q: string) => customerUseCases.autocomplete(q, undefined, "TRUCKER") },
  carrier:     { key: "ac-code-carrier",     fetch: (q: string) => carrierUseCases.autocomplete(q) },
  carrierSea:  { key: "ac-code-carrier-sea", fetch: (q: string) => carrierUseCases.autocomplete(q, undefined, "SEA") },
  carrierAir:  { key: "ac-code-carrier-air", fetch: (q: string) => carrierUseCases.autocomplete(q, undefined, "AIR") },
  port:        { key: "ac-code-port",        fetch: (q: string) => portUseCases.autocomplete(q) },
  portSea:     { key: "ac-code-port-sea",    fetch: (q: string) => portUseCases.autocomplete(q, undefined, "SEA") },
  portAir:     { key: "ac-code-port-air",    fetch: (q: string) => portUseCases.autocomplete(q, undefined, "AIR") },
  user:        { key: "ac-user",             fetch: (q: string) => userUseCases.autocomplete(q) },
  packageUnit: { key: "ac-code-package",     fetch: (q: string) => packageUnitUseCases.autocomplete(q) },
  hsCode:      { key: "ac-code-hs-code",     fetch: (q: string) => hsCodeUseCases.autocomplete(q) },
  currency:    { key: "ac-code-currency",    fetch: (q: string) => currencyUseCases.autocomplete(q) },
  team:        { key: "ac-team",             fetch: (q: string) => teamUseCases.autocomplete(q) },
  subscriber:  { key: "ac-subscriber",       fetch: (q: string) => subscriberUseCases.autocomplete(q) },
  freight:     { key: "ac-code-freight",     fetch: (q: string) => freightUseCases.autocomplete(q) },
} satisfies Record<string, AutocompleteSource>;
