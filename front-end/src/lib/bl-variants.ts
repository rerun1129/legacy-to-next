export type Mode = 'SEA' | 'AIR'
export type Direction = 'EXP' | 'IMP'
export type BLVariantKey = 'sea-exp' | 'sea-imp' | 'air-exp' | 'air-imp'

export interface BLVariantConfig {
  key: BLVariantKey
  mode: Mode
  direction: Direction
  label: string          // "SEA/EXP"
  modeLabel: string      // "해상" | "항공"
  dirLabel: string       // "수출" | "수입"
  blNoLabel: string      // "HBL No" | "HAWB No"
  mblNoLabel: string     // "MBL No" | "MAWB No"
  hasContainer: boolean  // SEA only
  hasDimension: boolean  // AIR only
  hasIssueInfo: boolean  // EXP: true, IMP-AIR: false
  hasDoDate: boolean     // IMP-SEA only
  issueFields: string[]  // issue info fields per variant
  printDocs: string[]    // what can be printed
}

export const BL_VARIANTS: Record<BLVariantKey, BLVariantConfig> = {
  'sea-exp': {
    key: 'sea-exp', mode: 'SEA', direction: 'EXP',
    label: 'SEA / Export', modeLabel: '해상', dirLabel: 'Export',
    blNoLabel: 'HBL No', mblNoLabel: 'MBL No',
    hasContainer: true, hasDimension: false,
    hasIssueInfo: true, hasDoDate: false,
    issueFields: ['Issue Date', 'No. of B/L', 'Issue Place'],
    printDocs: ['B/L', 'S/A', 'S/R'],
  },
  'sea-imp': {
    key: 'sea-imp', mode: 'SEA', direction: 'IMP',
    label: 'SEA / Import', modeLabel: '해상', dirLabel: 'Import',
    blNoLabel: 'HBL No', mblNoLabel: 'MBL No',
    hasContainer: true, hasDimension: false,
    hasIssueInfo: false, hasDoDate: true,
    issueFields: ['D/O Date'],
    printDocs: ['A/N', 'D/O'],
  },
  'air-exp': {
    key: 'air-exp', mode: 'AIR', direction: 'EXP',
    label: 'AIR / Export', modeLabel: '항공', dirLabel: 'Export',
    blNoLabel: 'HAWB No', mblNoLabel: 'MAWB No',
    hasContainer: false, hasDimension: true,
    hasIssueInfo: true, hasDoDate: false,
    issueFields: ['Issue Date', 'Issue Place', 'Signature'],
    printDocs: ['HAWB', 'S/A', 'S/R'],
  },
  'air-imp': {
    key: 'air-imp', mode: 'AIR', direction: 'IMP',
    label: 'AIR / Import', modeLabel: '항공', dirLabel: 'Import',
    blNoLabel: 'HAWB No', mblNoLabel: 'MAWB No',
    hasContainer: false, hasDimension: true,
    hasIssueInfo: false, hasDoDate: false,
    issueFields: [],
    printDocs: [],
  },
}

export const BL_VARIANT_KEYS: BLVariantKey[] = ['sea-exp', 'sea-imp', 'air-exp', 'air-imp']

export function getBLVariant(key: string): BLVariantConfig {
  return BL_VARIANTS[key as BLVariantKey] ?? BL_VARIANTS['sea-exp']
}

export function getPageTitle(variant: BLVariantConfig, blType: 'House' | 'Master', pageType: 'Entry' | 'List'): string {
  const modeStr = variant.mode === 'SEA' ? 'Sea' : 'Air';
  const dirStr  = variant.direction === 'EXP' ? 'Export' : 'Import';
  return `${modeStr} ${dirStr} ${blType} B/L ${pageType}`;
}

export function getMasterVariant(key: string): MasterVariantConfig {
  return MASTER_VARIANTS[key as BLVariantKey] ?? MASTER_VARIANTS['sea-exp']
}

// Master B/L variant configs — mostly same as House but with differences noted
export interface MasterVariantConfig extends BLVariantConfig {
  bottomActions: string[]
  hasLineBkgNo: boolean
  toolbarColumnCount: number
}

export const MASTER_VARIANTS: Record<BLVariantKey, MasterVariantConfig> = {
  'sea-exp': {
    ...BL_VARIANTS['sea-exp'],
    blNoLabel: 'MBL No', mblNoLabel: 'Master Ref No',
    bottomActions: ['Profit/Loss', 'House B/L Load', 'Shipping Request', 'Shipping Advice', 'M/F Send', 'AFR Send', 'EDMS'],
    hasLineBkgNo: true,
    toolbarColumnCount: 8,
    printDocs: ['S/A', 'S/R'],
  },
  'sea-imp': {
    ...BL_VARIANTS['sea-imp'],
    blNoLabel: 'MBL No', mblNoLabel: 'Master Ref No',
    hasIssueInfo: false, hasDoDate: false, issueFields: [],
    bottomActions: ['Profit/Loss', 'House B/L Load', 'M/F Send', 'AFR Send', 'EDMS'],
    hasLineBkgNo: true,
    toolbarColumnCount: 8,
    printDocs: [],
  },
  'air-exp': {
    ...BL_VARIANTS['air-exp'],
    blNoLabel: 'MAWB No', mblNoLabel: 'Master Ref No',
    issueFields: ['Issue Date', 'Signature', 'Issue Place'],
    bottomActions: ['MAWB', 'Shipping Request', 'Shipping Advice', 'Profit/Loss', 'House B/L Load', 'M/F Send', 'AFR Send', 'EDMS'],
    hasLineBkgNo: false,
    toolbarColumnCount: 4,
    printDocs: ['MAWB', 'S/A', 'S/R'],
  },
  'air-imp': {
    ...BL_VARIANTS['air-imp'],
    blNoLabel: 'MAWB No', mblNoLabel: 'Master Ref No',
    bottomActions: ['Profit/Loss', 'House B/L Load', 'M/F Send', 'AFR Send', 'EDMS'],
    hasLineBkgNo: false,
    toolbarColumnCount: 4,
    printDocs: [],
  },
}
