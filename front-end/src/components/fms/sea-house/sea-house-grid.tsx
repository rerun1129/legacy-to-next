"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { Ship } from "lucide-react";
import { GridList, GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { Pagination } from "@/components/shared/pagination";
import { seaHousePort } from "@/lib/ports";
import { fmtDate, fmtWeight } from "@/lib/grid-formatters";
import type { SeaHouseRow, SeaHouseFilter } from "@/domain/sea-house";

interface Props {
  extraFilter: SeaHouseFilter | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  showAll: boolean;
  onToggleShowAll: () => void;
  bound: "EXP" | "IMP";
}

export function SeaHouseGrid({ extraFilter, currentPage, onPageChange, showAll, onToggleShowAll, bound }: Props) {
  const router = useRouter();
  const [selected, setSelected] = useState<number | null>(null);

  const { data, isFetching, error } = useQuery({
    queryKey: ["sea-house", "list", bound, extraFilter, showAll ? "all" : currentPage],
    queryFn: () => seaHousePort.list(
      { ...extraFilter!, bound },
      showAll ? 1 : currentPage,
      showAll ? 10000000 : 50,
    ),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  const columns: GridColumn<SeaHouseRow>[] = [
    {
      key: "hblNo",
      label: "House B/L No.",
      minWidth: 160,
      render: (v, row) => (
        <div
          onDoubleClick={() => router.push(
            `${bound === "EXP" ? "/fms/house-bl/sea-exp/entry" : "/fms/house-bl/sea-imp/entry"}?id=${row.id}`
          )}
          style={{ cursor: "pointer" }}
        >
          {String(v ?? "")}
        </div>
      ),
    },
    { key: "bound",               label: "Bound",                  minWidth: 60 },
    { key: "mblNo",               label: "Master B/L No",          minWidth: 160 },
    { key: "masterRefNo",         label: "Master Reference No.",   minWidth: 160 },
    { key: "shipmentType",        label: "Shipment Type",          minWidth: 100 },
    { key: "loadType",            label: "Load Type",              minWidth: 90 },
    { key: "etd",                 label: "ETD",                    minWidth: 100, render: (v) => fmtDate(v) },
    { key: "eta",                 label: "ETA",                    minWidth: 100, render: (v) => fmtDate(v) },
    { key: "polCode",             label: "POL",                    minWidth: 100 },
    { key: "podCode",             label: "POD",                    minWidth: 100 },
    { key: "deliveryCode",        label: "Delivery",               minWidth: 90 },
    { key: "vesselName",          label: "Vessel Name",            minWidth: 140 },
    { key: "voyageNo",            label: "Voyage",                 minWidth: 90 },
    { key: "shipperCode",         label: "Shipper",                minWidth: 90 },
    { key: "shipperName",         label: "Shipper Name",           minWidth: 140 },
    { key: "consigneeCode",       label: "Consignee",              minWidth: 90 },
    { key: "consigneeName",       label: "Consignee Name",         minWidth: 150 },
    { key: "notifyCode",          label: "Notify",                 minWidth: 90 },
    { key: "notifyName",          label: "Notify Name",            minWidth: 140 },
    { key: "settlePartnerCode",   label: "Settle Partner",         minWidth: 110 },
    { key: "settlePartnerName",   label: "Settle Partner Name",    minWidth: 160 },
    { key: "docPartnerCode",      label: "DOC Partner",            minWidth: 100 },
    { key: "docPartnerName",      label: "DOC Partner Name",       minWidth: 150 },
    { key: "linerCode",           label: "Liner",                  minWidth: 110 },
    { key: "linerName",           label: "Liner Name",             minWidth: 160 },
    { key: "freightTerm",         label: "Freight Term",           minWidth: 100 },
    { key: "incoterms",           label: "Incoterms",              minWidth: 90 },
    { key: "actualCustomerCode",  label: "Actual Customer",        minWidth: 120 },
    { key: "actualCustomerName",  label: "Actual Customer Name",   minWidth: 160 },
    { key: "pkgQty",              label: "Package",                minWidth: 90 },
    { key: "pkgUnit",             label: "Unit",                   minWidth: 70 },
    { key: "grossWeightKg",       label: "Gross W/T",              minWidth: 100, render: (v) => fmtWeight(v) },
    { key: "cbm",                 label: "CBM",                    minWidth: 90,  render: (v) => (v != null ? (v as number).toFixed(3) : '') },
    { key: "cntr20Qty",           label: "20FT",                   minWidth: 70 },
    { key: "cntr40Qty",           label: "40FT",                   minWidth: 70 },
    { key: "teuQty",              label: "TEU",                    minWidth: 70,  render: (v) => (v != null ? (v as number).toFixed(2) : '') },
    { key: "salesManCode",        label: "Sales Man",              minWidth: 90 },
    { key: "teamCode",            label: "Team Name",              minWidth: 90 },
  ];

  if (error) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Sea House B/L</span>
        </div>
        <div className="list-wrap" style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}>
          <span className="text-error">데이터를 불러오지 못했습니다.</span>
        </div>
      </div>
    );
  }

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <Ship size={14} style={{ marginRight: 4 }} />
        <span className="panel__title">Sea House B/L</span>
        <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
        <ColumnVisibilityMenu<SeaHouseRow> gridId="sea-house" defaultColumns={columns} />
      </div>
      <div className="list-wrap">
        <GridList<SeaHouseRow>
          columns={columns}
          data={rows}
          onRowClick={(row) => setSelected(row.id)}
          rowKey={(row) => row.id}
          rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
          gridId="sea-house"
          isLoading={extraFilter !== null && isFetching}
        />
      </div>
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={onPageChange}
        disabled={isFetching}
        showAll={showAll}
        onToggleShowAll={onToggleShowAll}
      />
    </div>
  );
}
