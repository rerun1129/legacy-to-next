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
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

const ROWS = [
  {
    id: 1,
    nonBlNo: "NBL-2026-04-001",
    bound: "O",
    etd: "20260424",
    eta: "20260508",
    pol: "KRBSAN",
    pod: "CNSHA",
    vesselName: "HYUNDAI BRAVE",
    voyNo: "026E",
    shipperCode: "SHP001",
    shipperName: "한진무역(주)",
    consigneeCode: "CNS001",
    consigneeName: "SHANGHAI TRADING CO.",
    notifyCode: "NTF001",
    notifyName: "SHANGHAI TRADING CO.",
    settlePartnerCode: "PTR001",
    settlePartnerName: "한진로지스틱스",
    linerCode: "HMM",
    linerName: "현대상선",
    actualCustomerCode: "CST001",
    actualCustomerName: "한진무역(주)",
    pkgQty: "120",
    pkgUnit: "CTN",
    grossWt: "1234.000",
    cbm: "10.500",
    teamName: "해운팀",
  },
  {
    id: 2,
    nonBlNo: "NBL-2026-04-002",
    bound: "I",
    etd: "20260422",
    eta: "20260423",
    pol: "JPNRT",
    pod: "KRICN",
    vesselName: "KOREA AIR 701",
    voyNo: "KE701",
    shipperCode: "SHP002",
    shipperName: "LG전자(주)",
    consigneeCode: "CNS002",
    consigneeName: "LG JAPAN K.K.",
    notifyCode: "NTF002",
    notifyName: "LG JAPAN K.K.",
    settlePartnerCode: "PTR002",
    settlePartnerName: "대한항공 카고",
    linerCode: "KE",
    linerName: "대한항공",
    actualCustomerCode: "CST002",
    actualCustomerName: "LG전자(주)",
    pkgQty: "45",
    pkgUnit: "PLT",
    grossWt: "560.000",
    cbm: "4.200",
    teamName: "항공팀",
  },
  {
    id: 3,
    nonBlNo: "NBL-2026-04-003",
    bound: "O",
    etd: "20260501",
    eta: "20260520",
    pol: "KRINCHON",
    pod: "USNYC",
    vesselName: "MAERSK SEALAND",
    voyNo: "0142N",
    shipperCode: "SHP003",
    shipperName: "삼성전자",
    consigneeCode: "CNS003",
    consigneeName: "SAMSUNG AMERICA INC.",
    notifyCode: "NTF003",
    notifyName: "SAMSUNG AMERICA INC.",
    settlePartnerCode: "PTR003",
    settlePartnerName: "삼성SDS",
    linerCode: "MSC",
    linerName: "MSC SHIPPING",
    actualCustomerCode: "CST003",
    actualCustomerName: "삼성전자",
    pkgQty: "200",
    pkgUnit: "CTN",
    grossWt: "3450.000",
    cbm: "28.800",
    teamName: "해운팀",
  },
  {
    id: 4,
    nonBlNo: "NBL-2026-04-004",
    bound: "I",
    etd: "20260425",
    eta: "20260426",
    pol: "CNSHA",
    pod: "KRPUS",
    vesselName: "COSCO SHIPPING",
    voyNo: "S138E",
    shipperCode: "SHP004",
    shipperName: "포스코",
    consigneeCode: "CNS004",
    consigneeName: "평택항 터미널",
    notifyCode: "NTF004",
    notifyName: "포스코 물류",
    settlePartnerCode: "PTR004",
    settlePartnerName: "포스코 물류",
    linerCode: "COSCO",
    linerName: "코스코 해운",
    actualCustomerCode: "CST004",
    actualCustomerName: "포스코",
    pkgQty: "80",
    pkgUnit: "PLT",
    grossWt: "9800.000",
    cbm: "42.000",
    teamName: "해운팀",
  },
];

type NonBlRow = typeof ROWS[number];

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
