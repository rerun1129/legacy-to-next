"use client";

import { useState, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useRouter } from "next/navigation";
import { useTranslations } from "next-intl";
import { ChevronLeft, Copy } from "lucide-react";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { BlQuickSearchFilters, BlQuickSearchItem } from "@/domain/bl-quick-search";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import { useBlQuickSearch } from "@/lib/use-bl-quick-search";
import { openBlEntry } from "@/lib/open-bl-entry";
import { CopyBlModal } from "@/components/fms/bl-copy/copy-bl-modal";
import { QuickSearchFilterFields } from "./quick-search-filter-fields";

interface Props {
  onBack: () => void;
}

export function QuickSearchPanel({ onBack }: Props) {
  const router = useRouter();
  const t = useTranslations("shell.quickSearch");
  const [blQuery, setBlQuery] = useState("");
  const [selectedItem, setSelectedItem] = useState<BlQuickSearchItem | null>(null);
  const [isCopyModalOpen, setIsCopyModalOpen] = useState(false);

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
    setSelectedItem(item);
    openBlEntry(item, router, t("noAccess"));
  }

  const handleCopyClick = useCallback(() => {
    setIsCopyModalOpen(true);
  }, []);

  const handleCopyModalClose = useCallback(() => {
    setIsCopyModalOpen(false);
  }, []);

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

      {/* B/L 번호 검색 + Copy 버튼 슬롯 */}
      <div className="quick-search-panel__bl-search">
        <div className="quick-search-panel__bl-row">
          <div className="quick-search-panel__bl-input">
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
          {selectedItem != null && (
            <button
              type="button"
              className="quick-search-panel__copy-btn"
              onClick={handleCopyClick}
              aria-label={t("copy")}
              title={t("copy")}
            >
              <Copy size={14} />
            </button>
          )}
        </div>
      </div>

      {/* B/L Copy 모달 */}
      {selectedItem != null && isCopyModalOpen && (
        <CopyBlModal
          item={selectedItem}
          isOpen={isCopyModalOpen}
          onClose={handleCopyModalClose}
        />
      )}
    </div>
  );
}
