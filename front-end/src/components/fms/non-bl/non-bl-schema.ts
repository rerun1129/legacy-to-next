import { z } from "zod";

export const CONTAINER_INFO_SCHEMA = z.object({
  id: z.number(),
  cno: z.string().optional(),
  contType: z.string().optional(),
  sealNo1: z.string().optional(),
  sealNo2: z.string().optional(),
  sealNo3: z.string().optional(),
  pkg: z.number().optional(),
  pkgUnit: z.string().optional(),
  grossWt: z.number().optional(),
  cbm: z.number().optional(),
});

export const DIM_SCHEMA = z.object({
  id: z.number(),
  length: z.string().optional(),
  width: z.string().optional(),
  height: z.string().optional(),
  qty: z.string().optional(),
  cbm: z.string().optional(),
  volWt: z.string().optional(),
});

export const NON_BL_SCHEMA = z.object({
  containers: z.array(CONTAINER_INFO_SCHEMA).optional(),
  dimensions: z.array(DIM_SCHEMA).optional(),
});

export type NonBlFormValues = z.infer<typeof NON_BL_SCHEMA>;

export const EMPTY_CONTAINER_ROW: Omit<z.infer<typeof CONTAINER_INFO_SCHEMA>, "id"> = {
  cno: "", contType: "", sealNo1: "", sealNo2: "", sealNo3: "",
  pkg: 0, pkgUnit: "", grossWt: 0, cbm: 0,
};

export const EMPTY_DIM_ROW: Omit<z.infer<typeof DIM_SCHEMA>, "id"> = {
  length: "", width: "", height: "", qty: "", cbm: "", volWt: "",
};
