"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { GridList, GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { TextBox } from "@/components/shared/inputs/text-box";
import { DropBox } from "@/components/shared/inputs/drop-box";
import { NumberBox } from "@/components/shared/inputs/number-box";
import { DateCell } from "@/components/shared/grid-cell-inputs";
import { useEnumOptions } from "@/application/enums/use-enum";

type NonBlRow = {
  id: number;
  nonBlNo: string;
  bound: string;
  etd: string;
  eta: string;
  pol: string;
  pod: string;
  vesselName: string;
  voyNo: string;
  shipperCode: string;
  shipperName: string;
  consigneeCode: string;
  consigneeName: string;
  notifyCode: string;
  notifyName: string;
  settlePartnerCode: string;
  settlePartnerName: string;
  linerCode: string;
  linerName: string;
  actualCustomerCode: string;
  actualCustomerName: string;
  pkgQty: string;
  pkgUnit: string;
  grossWt: string;
  cbm: string;
  teamName: string;
};

const ROWS: NonBlRow[] = [];

export function NonBlGrid() {
  const router = useRouter();
  const [selected, setSelected] = useState<string | null>(null);
  const { options: boundOptions } = useEnumOptions("Bound");

  const columns: GridColumn<NonBlRow>[] = [
    {
      key: "nonBlNo",
      label: "Non B/L No",
      minWidth: 150,
      render: (v) => (
        <div
          onDoubleClick={() => router.push("/fms/non-bl/entry")}
          style={{ cursor: "pointer" }}
        >
          <TextBox variant="cell" readOnly value={String(v ?? "")} />
        </div>
      ),
    },
    {
      key: "bound",
      label: "Bound",
      minWidth: 90,
      render: (v) => (
        <DropBox variant="cell" readOnly options={boundOptions} value={String(v ?? "")} />
      ),
    },
    {
      key: "etd",
      label: "ETD",
      minWidth: 110,
      render: (v) => <DateCell readOnly value={String(v ?? "")} />,
    },
    {
      key: "eta",
      label: "ETA",
      minWidth: 110,
      render: (v) => <DateCell readOnly value={String(v ?? "")} />,
    },
    {
      key: "pol",
      label: "POL",
      minWidth: 80,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "pod",
      label: "POD",
      minWidth: 80,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "vesselName",
      label: "Vessel",
      minWidth: 130,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "voyNo",
      label: "Voyage",
      minWidth: 80,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "shipperCode",
      label: "Shipper",
      minWidth: 90,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "shipperName",
      label: "Shipper Name",
      minWidth: 140,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "consigneeCode",
      label: "Consignee",
      minWidth: 90,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "consigneeName",
      label: "Consignee Name",
      minWidth: 150,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "notifyCode",
      label: "Notify",
      minWidth: 90,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "notifyName",
      label: "Notify Name",
      minWidth: 140,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "settlePartnerCode",
      label: "Partner",
      minWidth: 90,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "settlePartnerName",
      label: "Partner Name",
      minWidth: 140,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "linerCode",
      label: "Liner",
      minWidth: 80,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "linerName",
      label: "Liner Name",
      minWidth: 120,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "actualCustomerCode",
      label: "Actual Customer",
      minWidth: 110,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "actualCustomerName",
      label: "Actual Customer Name",
      minWidth: 160,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "pkgQty",
      label: "Package",
      minWidth: 80,
      render: (v) => <NumberBox variant="cell" decimalPlaces={0} readOnly value={String(v ?? "")} />,
    },
    {
      key: "pkgUnit",
      label: "Package Unit",
      minWidth: 90,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
    {
      key: "grossWt",
      label: "Gross W/T",
      minWidth: 100,
      render: (v) => <NumberBox variant="cell" decimalPlaces={3} readOnly value={String(v ?? "")} />,
    },
    {
      key: "cbm",
      label: "CBM",
      minWidth: 90,
      render: (v) => <NumberBox variant="cell" decimalPlaces={3} readOnly value={String(v ?? "")} />,
    },
    {
      key: "teamName",
      label: "Team Name",
      minWidth: 90,
      render: (v) => <TextBox variant="cell" readOnly value={String(v ?? "")} />,
    },
  ];

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Non B/L</span>
        <span className="panel__rowcount">{ROWS.length}</span>
        <ColumnVisibilityMenu<NonBlRow> gridId="non-bl" defaultColumns={columns} />
      </div>
      <div className="list-wrap">
        <GridList<NonBlRow>
          columns={columns}
          data={ROWS}
          onRowClick={(row) => setSelected(row.nonBlNo)}
          rowKey={(row) => row.id}
          rowClassName={(row) => (selected === row.nonBlNo ? "is-selected" : undefined)}
          gridId="non-bl"
        />
      </div>
    </div>
  );
}
