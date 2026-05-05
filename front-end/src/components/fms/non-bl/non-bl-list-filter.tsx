"use client";

import { CodeBox } from "@/components/shared/inputs/code-box";
import { DropBox } from "@/components/shared/inputs/drop-box";
import { TextBox } from "@/components/shared/inputs/text-box";
import { DateRangeBox } from "@/components/shared/inputs/date-range-box";

const BOUND_OPTIONS = [
  { value: "ALL", label: "ALL" },
  { value: "I", label: "Inbound" },
  { value: "O", label: "Outbound" },
];

export function NonBlListFilter() {
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
            fromProps={{ placeholder: "From" }}
            toProps={{ placeholder: "To" }}
          />

          <CodeBox
            kind="lcn"
            label="Liner"
            codeProps={{ placeholder: "Code" }}
            nameProps={{ placeholder: "Name" }}
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
          />

          <CodeBox
            kind="lcn"
            label="Port"
            codeProps={{ placeholder: "Code" }}
            nameProps={{ placeholder: "Name" }}
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
          />

          {/* Row 3 */}
          <CodeBox
            kind="lcn"
            label="Team"
            codeProps={{ placeholder: "Code" }}
            nameProps={{ placeholder: "Name" }}
          />
        </div>
      </div>
    </div>
  );
}
