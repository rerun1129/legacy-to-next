import { useEffect } from "react";
import type { UseFormGetValues, UseFormSetValue } from "react-hook-form";
import { PASTE_COLS } from "./attribute-list-helpers";
import type { AttributeFormValues } from "./attribute-list-helpers";

/**
 * 셀 포커스 기준으로 클립보드 붙여넣기를 처리한다.
 * data-row-key / data-col-key 가 부여된 td 에서 paste 이벤트 감지.
 * 탭 구분 5열: attributeKey, name, valueType, allowMulti, active
 */
export function useAttributePasteHandler(
  getValues: UseFormGetValues<AttributeFormValues>,
  setValue: UseFormSetValue<AttributeFormValues>,
) {
  useEffect(() => {
    function handlePaste(e: ClipboardEvent) {
      const active = document.activeElement as HTMLElement | null;
      const td = active?.closest("td[data-row-key][data-col-key]") as HTMLElement | null;
      if (!td) return;

      const text = e.clipboardData?.getData("text/plain");
      if (!text) return;

      const rows = getValues("rows");
      const startRowKey = td.dataset.rowKey!;
      const startColKey = td.dataset.colKey!;
      const startRowIdx = rows.findIndex((r) => String(r.entityId) === startRowKey);
      const startColIdx = PASTE_COLS.indexOf(startColKey as typeof PASTE_COLS[number]);
      if (startRowIdx === -1 || startColIdx === -1) return;

      e.preventDefault();
      const pastedRows = text
        .split(/\r?\n/)
        .filter((l) => l.length > 0)
        .map((l) => l.split("\t"));

      for (let ri = 0; ri < pastedRows.length; ri++) {
        const rowIdx = startRowIdx + ri;
        if (rowIdx >= rows.length) break;
        for (let ci = 0; ci < pastedRows[ri].length; ci++) {
          const colIdx = startColIdx + ci;
          if (colIdx >= PASTE_COLS.length) break;
          const col = PASTE_COLS[colIdx];
          const val = pastedRows[ri][ci];
          if (col === "allowMulti" || col === "active") {
            setValue(
              `rows.${rowIdx}.${col}`,
              val === "true" || val === "Yes" || val === "Active",
              { shouldDirty: true },
            );
          } else {
            setValue(`rows.${rowIdx}.${col}`, val as never, { shouldDirty: true });
          }
        }
      }
    }

    document.addEventListener("paste", handlePaste);
    return () => document.removeEventListener("paste", handlePaste);
  }, [getValues, setValue]);
}
