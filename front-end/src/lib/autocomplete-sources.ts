import type { AutocompleteSource } from "./use-code-autocomplete";
import { customerUseCases } from "@/application/customer/use-cases";
import { carrierUseCases } from "@/application/code/carrier/use-cases";
import { portUseCases } from "@/application/code/port/use-cases";
import { userUseCases } from "@/application/user/use-cases";

export const CODE_SOURCES = {
  customer: { key: "ac-customer",         fetch: (q: string) => customerUseCases.autocomplete(q, undefined, "CUSTOMER") },
  partner:  { key: "ac-customer-partner", fetch: (q: string) => customerUseCases.autocomplete(q, undefined, "PARTNER") },
  trucker:  { key: "ac-customer-trucker", fetch: (q: string) => customerUseCases.autocomplete(q, undefined, "TRUCKER") },
  carrier:  { key: "ac-code-carrier",     fetch: (q: string) => carrierUseCases.autocomplete(q) },
  port:     { key: "ac-code-port",        fetch: (q: string) => portUseCases.autocomplete(q) },
  user:     { key: "ac-user",             fetch: (q: string) => userUseCases.autocomplete(q) },
} satisfies Record<string, AutocompleteSource>;
