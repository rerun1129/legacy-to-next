"use client";

import { Save, Plus } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { Button } from "@/components/shared/button";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import type { CommonCodeFormRow } from "./common-code-grid-columns";
import { getCommonCodeRowClassName } from "./common-code-grid-columns";

interface Props {
  selectedGroupCode: string | null;
  tPanel: (key: string) => string;
  tMsg: (key: string) => string;
  fields: CommonCodeFormRow[];
  columns: GridColumn<CommonCodeFormRow>[];
  originalRows: CommonCodeFormRow[];
  codesFetching: boolean;
  selectedKeys: Set<number>;
  onSelectionChange: (keys: Set<number>) => void;
  isSaveDisabled: boolean;
  onSave: () => void;
  onAdd: () => void;
}

export function CommonCodeCodePanel({
  selectedGroupCode,
  tPanel,
  tMsg,
  fields,
  columns,
  originalRows,
  codesFetching,
  selectedKeys,
  onSelectionChange,
  isSaveDisabled,
  onSave,
  onAdd,
}: Props) {
  if (selectedGroupCode === null) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{tPanel("title")}</span>
        </div>
        <div
          className="list-wrap"
          style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
        >
          <span style={{ color: "var(--ink-3)" }}>{tMsg("selectGroup")}</span>
        </div>
      </div>
    );
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      {/* 코드 그리드 툴바 — Save / Add */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_COMMON_CODE_SAVE"
          className="btn btn--transaction btn--sm"
          disabled={isSaveDisabled}
          onClick={onSave}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
        <Button variant="success" size="sm" iconOnly onClick={onAdd}>
          <Plus size={12} />
        </Button>
      </div>

      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{tPanel("title")}</span>
          <span className="panel__rowcount">{fields.length}</span>
        </div>
        <div className="list-wrap">
          <GridList<CommonCodeFormRow>
            columns={columns}
            data={fields}
            rowKey={(row) => row.entityId}
            rowClassName={(row) => getCommonCodeRowClassName(row, originalRows)}
            isLoading={codesFetching}
            emptyMessage={tMsg("noResults")}
            selectable
            selectedKeys={selectedKeys}
            onSelectionChange={(next) => onSelectionChange(new Set([...next].map(Number)))}
          />
        </div>
      </div>
    </div>
  );
}
