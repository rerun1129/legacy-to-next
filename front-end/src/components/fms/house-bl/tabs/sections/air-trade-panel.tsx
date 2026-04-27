import { FieldWidgetList, type FieldWidgetDef } from "@/components/widget/field-widget-list";
import { FieldItemGrid,   type FieldItemDef }   from "@/components/widget/field-item-grid";
import type { AnyVariantConfig } from "@/components/widget/widget-registry";

interface Props { variant?: AnyVariantConfig }

const LI_ST: React.CSSProperties = { width: "100%", height: 22, padding: "0 8px", fontSize: 10 };

function LiField({ label, value }: { label: string; value: string }) {
  return (
    <div className="li">
      <span className="li__label">{label}</span>
      <div className="li__input"><input defaultValue={value} style={LI_ST} /></div>
    </div>
  );
}

const BASE_ITEMS: FieldItemDef[] = [
  { key: "currency",     render: () => <LiField label="Currency"     value="USD" /> },
  { key: "incoterms",   render: () => <LiField label="Incoterms"    value="DAP" /> },
  { key: "freight-term", render: () => <LiField label="Freight Term" value="Prepaid" /> },
  { key: "other-term",  render: () => <LiField label="Other Term"   value="" /> },
  { key: "dv-carriage", render: () => <LiField label="D.V Carriage" value="N.V.D." /> },
  { key: "insurance",   render: () => <LiField label="Insurance"    value="NIL" /> },
  { key: "dv-customs",  render: () => <LiField label="D.V Customs"  value="AS PER INV." /> },
  { key: "account-info", render: () => <LiField label="Account Info" value="FREIGHT PREPAID" /> },
];

const FHD_ITEM: FieldItemDef = { key: "fhd", render: () => <LiField label="F.H.D" value="Not" /> };

export function AirTradePanel({ variant }: Props) {
  if (!variant) return null;
  const panelScope = `air-trade-panel.${variant.key}`;
  const isImp      = variant.direction === "IMP";

  const tradeItems: FieldItemDef[] = isImp ? [...BASE_ITEMS, FHD_ITEM] : BASE_ITEMS;

  const fields: FieldWidgetDef[] = [
    {
      key:   "trade-terms",
      label: "Trade Terms",
      render: () => (
        <FieldItemGrid itemScope={`${panelScope}.trade-terms`} items={tradeItems} cols={1} shouldShowRowControls={false} />
      ),
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head"><div className="panel__title-accent" /><span className="panel__title">Trade</span></div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldWidgetList panelScope={panelScope} fields={fields} />
      </div>
    </div>
  );
}
