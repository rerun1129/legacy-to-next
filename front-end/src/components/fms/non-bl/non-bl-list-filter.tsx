"use client";

import { CodeBox } from "@/components/shared/inputs/code-box";
import { DropBox } from "@/components/shared/inputs/drop-box";
import { TextBox } from "@/components/shared/inputs/text-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";

function getDefaultMonthRange() {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const pad = (n: number) => String(n).padStart(2, "0");
  const lastDate = new Date(y, m + 1, 0).getDate();
  return {
    from: `${y}${pad(m + 1)}01`,
    to: `${y}${pad(m + 1)}${pad(lastDate)}`,
  };
}

const BOUND_OPTIONS = [
  { value: "ALL", label: "ALL" },
  { value: "I", label: "Inbound" },
  { value: "O", label: "Outbound" },
];

export function NonBlListFilter() {
  const { from, to } = getDefaultMonthRange();
  return (
    <div className="search-card">
      <div className="search-card__body">
        <div className="filter-grid">
          {/* Row 1 */}
          <div className="lcn">
            <span className="lcn__label">Bound</span>
            <DropBox
              variant="panel"
              options={BOUND_OPTIONS}
              style={{ gridColumn: "2 / span 2" }}
            />
          </div>

          <DateRangeBox
            label="Date"
            required
            fromProps={{ placeholder: "From", defaultValue: from }}
            toProps={{ placeholder: "To", defaultValue: to }}
          />

          <CodeBox
            kind="lcn"
            label="Liner"
            codeProps={{ placeholder: "Code" }}
            nameProps={{ placeholder: "Name" }}
            onLookup={() => {}}
          />

          <div className="lcn">
            <span className="lcn__label">Non B/L No</span>
            <TextBox
              variant="panel"
              placeholder="Non B/L No"
              style={{ gridColumn: "2 / span 2" }}
            />
          </div>

          {/* Row 2 */}
          <CodeBox
            kind="lcn"
            label="Party"
            codeProps={{ placeholder: "Code" }}
            nameProps={{ placeholder: "Name" }}
            onLookup={() => {}}
          />

          <CodeBox
            kind="lcn"
            label="Port"
            codeProps={{ placeholder: "Code" }}
            nameProps={{ placeholder: "Name" }}
            onLookup={() => {}}
          />

          <div className="lcn">
            <span className="lcn__label">Vessel/Voyage</span>
            <TextBox variant="panel" placeholder="Vessel" />
            <TextBox variant="panel" placeholder="Voyage" />
          </div>

          <CodeBox
            kind="lcn"
            label="Operator"
            codeProps={{ placeholder: "Code" }}
            nameProps={{ placeholder: "Name" }}
            onLookup={() => {}}
          />

          {/* Row 3 */}
          <CodeBox
            kind="lcn"
            label="Team"
            codeProps={{ placeholder: "Code" }}
            nameProps={{ placeholder: "Name" }}
            onLookup={() => {}}
          />
        </div>
      </div>
    </div>
  );
}
