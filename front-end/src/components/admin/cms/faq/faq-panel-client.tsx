"use client";

import { useState } from "react";
import { Plus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { FaqCategoryPanel } from "./faq-category-panel";
import { FaqListGrid } from "./faq-list-grid";
import { FaqCategoryEntryModal } from "./faq-category-entry-modal";
import type { FaqCategoryEntryModalState } from "./faq-category-entry-modal";
import { FaqEntryModal } from "./faq-entry-modal";
import type { FaqEntryModalState } from "./faq-entry-modal";

export function FaqPanelClient() {
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);
  const [categoryPage, setCategoryFaqPage] = useState(1);
  const [categoryModalState, setCategoryModalState] =
    useState<FaqCategoryEntryModalState | null>(null);
  const [faqModalState, setFaqModalState] = useState<FaqEntryModalState | null>(null);

  function handleCategorySelect(id: number) {
    setSelectedCategoryId(id);
    setCategoryFaqPage(1);
  }

  function handleCategoryEdit(id: number) {
    setCategoryModalState({ mode: "edit", id });
  }

  function handleCategoryCreate() {
    setCategoryModalState({ mode: "create" });
  }

  function handleCategoryModalClose() {
    setCategoryModalState(null);
  }

  function handleCategoryModalSaved() {
    setCategoryModalState(null);
  }

  function handleFaqCreate() {
    setFaqModalState({ mode: "create", defaultCategoryId: selectedCategoryId ?? undefined });
  }

  function handleFaqEdit(id: number) {
    setFaqModalState({ mode: "edit", id });
  }

  function handleFaqModalClose() {
    setFaqModalState(null);
  }

  function handleFaqModalSaved() {
    setFaqModalState(null);
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      {/* 상단 툴바 */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <Button
          size="sm"
          variant="modal"
          leftIcon={<Plus size={12} />}
          onClick={handleFaqCreate}
          disabled={selectedCategoryId === null}
          title={selectedCategoryId === null ? "카테고리를 선택하세요." : undefined}
        >
          신규 FAQ
        </Button>
      </div>

      {/* 2-panel 본체 */}
      <div style={{ display: "flex", gap: 12, flex: 1, minHeight: 0 }}>
        {/* 좌: 카테고리 패널 */}
        <div style={{ width: 280, flexShrink: 0, display: "flex", flexDirection: "column" }}>
          <FaqCategoryPanel
            selectedId={selectedCategoryId}
            onSelect={handleCategorySelect}
            onEdit={handleCategoryEdit}
            onCreate={handleCategoryCreate}
          />
        </div>

        {/* 우: FAQ 목록 */}
        <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column" }}>
          <FaqListGrid
            categoryId={selectedCategoryId}
            currentPage={categoryPage}
            onPageChange={setCategoryFaqPage}
            onRowDoubleClick={handleFaqEdit}
          />
        </div>
      </div>

      {/* 모달 */}
      <FaqCategoryEntryModal
        state={categoryModalState}
        onClose={handleCategoryModalClose}
        onSaved={handleCategoryModalSaved}
      />
      <FaqEntryModal
        state={faqModalState}
        onClose={handleFaqModalClose}
        onSaved={handleFaqModalSaved}
      />
    </div>
  );
}
