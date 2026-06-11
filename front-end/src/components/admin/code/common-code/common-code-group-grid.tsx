"use client";

import { useMemo } from "react";
import { useTranslations } from "next-intl";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import type { CommonCodeGroupRow } from "@/domain/common-code";
import type { GroupFilterValues } from "./common-code-filter-types";

interface Props {
  groups: CommonCodeGroupRow[];
  selectedGroupCode: string | null;
  onSelectGroup: (groupCode: string) => void;
  isLoading: boolean;
  submittedFilter: GroupFilterValues | null;
}

export function CommonCodeGroupGrid({
  groups,
  selectedGroupCode,
  onSelectGroup,
  isLoading,
  submittedFilter,
}: Props) {
  const tMsg = useTranslations("admin.commonCode.msg");
  const tCols = useTranslations("admin.commonCode.cols");
  const tGroup = useTranslations("admin.commonCode.group");

  // 클라이언트 측 필터링 — groups는 36행으로 서버 재조회 불필요
  const filteredGroups = useMemo<CommonCodeGroupRow[]>(() => {
    if (submittedFilter === null) return [];
    return groups.filter((g) => {
      const codeMatch =
        submittedFilter.groupCode === "" ||
        g.groupCode.toLowerCase().includes(submittedFilter.groupCode.toLowerCase());
      const moduleMatch =
        submittedFilter.module === "ALL" || g.sourceModule === submittedFilter.module;
      return codeMatch && moduleMatch;
    });
  }, [groups, submittedFilter]);

  const columns = useMemo<GridColumn<CommonCodeGroupRow>[]>(
    () => [
      {
        key: "_no",
        label: tCols("no"),
        width: 36,
        className: "row-num",
        render: (_v, _row, i) => i + 1,
      },
      {
        key: "groupCode",
        label: tCols("groupCode"),
        render: (_v, row) => (
          <span style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}>
            {row.groupCode}
          </span>
        ),
      },
      {
        key: "sourceModule",
        label: tCols("module"),
        width: 70,
        render: (_v, row) => row.sourceModule,
      },
      {
        key: "description",
        label: tCols("description"),
        render: (_v, row) => row.description ?? "",
      },
    ],
    [tCols],
  );

  const emptyMessage =
    submittedFilter === null ? tMsg("enterCriteria") : tGroup("noGroups");

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tGroup("title")}</span>
        <span className="panel__rowcount">{filteredGroups.length}</span>
      </div>
      <div className="list-wrap">
        <GridList<CommonCodeGroupRow>
          columns={columns}
          data={filteredGroups}
          rowKey={(row) => row.id}
          onRowClick={(row) => onSelectGroup(row.groupCode)}
          rowClassName={(row) =>
            row.groupCode === selectedGroupCode ? "is-selected" : undefined
          }
          isLoading={isLoading}
          emptyMessage={emptyMessage}
        />
      </div>
    </div>
  );
}
