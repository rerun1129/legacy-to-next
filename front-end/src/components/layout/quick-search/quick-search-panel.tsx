"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { useTranslations } from "next-intl";
import { ChevronLeft } from "lucide-react";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { BlQuickSearchFilters, BlQuickSearchItem } from "@/domain/bl-quick-search";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { useBlQuickSearch } from "@/lib/use-bl-quick-search";
import { openBlEntry } from "@/lib/open-bl-entry";
import { QuickSearchFilterFields } from "./quick-search-filter-fields";

interface Props {
  onBack: () => void;
}

export function QuickSearchPanel({ onBack }: Props) {
  const router = useRouter();
  const t = useTranslations("shell.quickSearch");
  const [blQuery, setBlQuery] = useState("");

  const form = useForm<BlQuickSearchFilters>({
    defaultValues: { dateKind: "ETD", partyKind: "SHIPPER" },
  });

  const { items, isLoading } = useBlQuickSearch(blQuery, form.watch());

  // id 충돌 회피: items 인덱스를 CodeBoxSuggestion.id에 매핑하여 선택 시 items 역참조
  const suggestions: CodeBoxSuggestion[] = items.map((it, i) => ({
    code: it.blNo,
    name: it.label,
    id: i,
  }));

  function handleSelect(suggestion: CodeBoxSuggestion): void {
    const item: BlQuickSearchItem | undefined = items[suggestion.id as number];
    if (!item) return;
    setBlQuery(item.blNo);
    openBlEntry(item, router, t("noAccess"));
  }

  return (
    <div className="quick-search-panel">
      {/* 헤더 */}
      <div className="quick-search-panel__head">
        <button
          type="button"
          className="quick-search-panel__back"
          onClick={onBack}
          aria-label={t("back")}
        >
          <ChevronLeft size={16} />
        </button>
        <span className="quick-search-panel__title">{t("title")}</span>
      </div>

      {/* 필터 필드 */}
      <QuickSearchFilterFields form={form} />

      {/* B/L 번호 검색 */}
      <div className="quick-search-panel__bl-search">
        <CodeBox
          kind="code-only"
          label={t("blNo")}
          codeProps={{
            placeholder: "B/L No",
            value: blQuery,
            onChange: (e) => setBlQuery(e.target.value),
          }}
          onSearch={setBlQuery}
          suggestions={suggestions}
          suggestionsLoading={isLoading}
          onSelect={handleSelect}
        />
      </div>
    </div>
  );
}
