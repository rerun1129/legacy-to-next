"use client";

import type { UseFormReturn } from "react-hook-form";
import { useWatch } from "react-hook-form";
import { CodeBox } from "@/components/shared/inputs/code-box";
import { DropBox } from "@/components/shared/inputs/drop-box";
import { TextBox } from "@/components/shared/inputs/text-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { useEnumOptions } from "@/application/enums/use-enum";

export interface NonBlListFilterValues {
  bound: string;
  dateFrom: string;
  dateTo: string;
  linerCode: string;
  linerName: string;
  nonBlNo: string;
  partyCode: string;
  partyName: string;
  portCode: string;
  portName: string;
  vessel: string;
  voyage: string;
  operatorCode: string;
  operatorName: string;
  teamCode: string;
  teamName: string;
}

interface Props {
  form: UseFormReturn<NonBlListFilterValues>;
}

export function NonBlListFilter({ form }: Props) {
  useListFilterSync(form, "/fms/non-bl/list");
  const { options: boundOptions, isLoading: boundLoading, placeholder: boundPlaceholder } = useEnumOptions("Bound")
  const boundOptionsWithAll = [{ value: "", label: "ALL" }, ...boundOptions]
  const dateFrom = useWatch({ control: form.control, name: "dateFrom" });
  const dateTo   = useWatch({ control: form.control, name: "dateTo" });
  const { register } = form;
  // ref를 분리해 react-hook-form의 DOM 직접 조작을 차단 — reset 시 마스크 플리커 방지
  const { ref: _fromRef, ...fromReg } = register("dateFrom");
  const { ref: _toRef,   ...toReg   } = register("dateTo");

  return (
    <div className="search-card">
      <div className="search-card__body">
        <div className="filter-grid">
          {/* Row 1 */}
          <div className="lcn">
            <span className="lcn__label">Bound</span>
            <DropBox
              variant="panel"
              options={boundOptionsWithAll}
              disabled={boundLoading}
              placeholder={boundPlaceholder}
              style={{ gridColumn: "2 / span 2" }}
              {...register("bound")}
            />
          </div>

          <DateRangeBox
            label="Date"
            required
            fromProps={{ ...fromReg, value: dateFrom, placeholder: "From" }}
            toProps={{ ...toReg,   value: dateTo,   placeholder: "To" }}
          />

          <CodeBox
            kind="lcn"
            label="Liner"
            codeProps={{ ...register("linerCode"), placeholder: "Code" }}
            nameProps={{ ...register("linerName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          <div className="lcn">
            <span className="lcn__label">Non B/L No</span>
            <TextBox
              variant="panel"
              placeholder="Non B/L No"
              style={{ gridColumn: "2 / span 2" }}
              {...register("nonBlNo")}
            />
          </div>

          {/* Row 2 */}
          <CodeBox
            kind="lcn"
            label="Party"
            codeProps={{ ...register("partyCode"), placeholder: "Code" }}
            nameProps={{ ...register("partyName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          <CodeBox
            kind="lcn"
            label="Port"
            codeProps={{ ...register("portCode"), placeholder: "Code" }}
            nameProps={{ ...register("portName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          <div className="lcn">
            <span className="lcn__label">Vessel/Voyage</span>
            <TextBox variant="panel" placeholder="Vessel" {...register("vessel")} />
            <TextBox variant="panel" placeholder="Voyage" {...register("voyage")} />
          </div>

          <CodeBox
            kind="lcn"
            label="Operator"
            codeProps={{ ...register("operatorCode"), placeholder: "Code" }}
            nameProps={{ ...register("operatorName"), placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* Row 3 */}
          <CodeBox
            kind="lcn"
            label="Team"
            codeProps={{ ...register("teamCode"), placeholder: "Code" }}
            nameProps={{ ...register("teamName"), placeholder: "Name" }}
            onLookup={() => {}}
          />
        </div>
      </div>
    </div>
  );
}
