import { z } from "zod";

// ── Freight row schema ─────────────────────────────────────
export const FREIGHT_ROW_SCHEMA = z.object({
  id:     z.number(),
  code:   z.string().optional(),
  desc:   z.string().optional(),
  qty:    z.string().optional(),
  unit:   z.string().optional(),
  sell:   z.string().optional(),
  buy:    z.string().optional(),
  cur:    z.string().optional(),
  remark: z.string().optional(),
});

export type FreightRow = z.infer<typeof FREIGHT_ROW_SCHEMA>;

// ── House BL form schema ───────────────────────────────────
export const HOUSE_BL_FORM_SCHEMA = z.object({
  // ── Toolbar fields ──────────────────────────────────────
  hbl:    z.string().max(35),
  mbl:    z.string().max(35),
  sType:  z.string(),
  lType:  z.string(),
  etd:    z.string().regex(/^\d{8}$/).or(z.literal("")).optional(),
  eta:    z.string().regex(/^\d{8}$/).or(z.literal("")).optional(),
  pol:    z.string().max(5).optional(),
  pod:    z.string().max(5).optional(),
  settle: z.enum(["PREPAID", "COLLECT"]),
  expImp: z.enum(["EXP", "IMP"]),

  // ── Party ───────────────────────────────────────────────
  shipperCode:   z.string().optional(),
  shipperName:   z.string().optional(),
  shipperAddr:   z.string().optional(),
  consigneeCode: z.string().optional(),
  consigneeName: z.string().optional(),
  consigneeAddr: z.string().optional(),
  notifyCode:    z.string().optional(),
  notifyName:    z.string().optional(),
  notifyAddr:    z.string().optional(),

  // ── Trade ───────────────────────────────────────────────
  paymentType:  z.string().optional(),
  paymentPlace: z.string().optional(),

  // ── Schedule (SEA) ──────────────────────────────────────
  linerCode:   z.string().optional(),
  linerName:   z.string().optional(),
  vesselCode:  z.string().optional(),
  vesselName:  z.string().optional(),
  voyNo:       z.string().optional(),
  onboardDate: z.string().optional(),

  // ── Air Schedule ────────────────────────────────────────
  airlineCode: z.string().optional(),
  airlineName: z.string().optional(),
  flightNo:    z.string().optional(),
  flightDate:  z.string().optional(),

  // ── Marks & Description ─────────────────────────────────
  marksAndNumbers:    z.string().optional(),
  descriptionOfGoods: z.string().optional(),
  natureOfGoods:      z.string().optional(),

  // ── Freight rows ────────────────────────────────────────
  freightSelling: z.array(FREIGHT_ROW_SCHEMA).optional(),
  freightBuying:  z.array(FREIGHT_ROW_SCHEMA).optional(),
});

export type HouseBlFormValues = z.infer<typeof HOUSE_BL_FORM_SCHEMA>;
