import type { Mode } from "./bl-variants";

export interface ModeLabels {
  blNo: string;            // MBL No           / MAWB No
  masterBlNo: string;      // Master B/L No     / Master AWB No
  hblNo: string;           // House B/L No.     / House AWB No.
  hblList: string;         // House B/L List    / House AWB List
  newHbl: string;          // New HBL           / New HAWB
  changeBLNo: string;      // Change B/L No     / Change AWB No
  containerPanel: string;  // Container (읽기 전용) / Dimension
  goodsDesc: string;       // Description       / Nature & Quantity of Goods
}

const SEA: ModeLabels = {
  blNo:           "MBL No",
  masterBlNo:     "Master B/L No",
  hblNo:          "House B/L No.",
  hblList:        "House B/L List",
  newHbl:         "New HBL",
  changeBLNo:     "Change B/L No",
  containerPanel: "Container (집계 뷰 — 읽기 전용)",
  goodsDesc:      "Description",
};

const AIR: ModeLabels = {
  blNo:           "MAWB No",
  masterBlNo:     "Master AWB No",
  hblNo:          "House AWB No.",
  hblList:        "House AWB List",
  newHbl:         "New HAWB",
  changeBLNo:     "Change AWB No",
  containerPanel: "Dimension",
  goodsDesc:      "Nature & Quantity of Goods",
};

export function getModeLabels(mode: Mode): ModeLabels {
  return mode === "SEA" ? SEA : AIR;
}
