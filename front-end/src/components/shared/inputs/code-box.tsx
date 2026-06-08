"use client";

import { forwardRef, useState, useCallback, useRef } from "react";
import { Search } from "lucide-react";
import type { CodeBoxProps, CodeBoxSuggestion } from "./_types";
import { LcnLabel } from "./lcn-label";
import { CodeBoxSuggestions } from "./code-box-suggestions";
import { useCodeInputHandlers } from "./use-code-input-handlers";

const lookupBtnStyle: React.CSSProperties = {
  position: "absolute",
  right: 6,
  top: "50%",
  transform: "translateY(-50%)",
  background: "none",
  border: "none",
  cursor: "pointer",
  padding: 0,
  display: "flex",
  alignItems: "center",
  color: "var(--ink-muted)",
};

const searchIndicatorStyle: React.CSSProperties = {
  position: "absolute",
  right: 6,
  top: "50%",
  transform: "translateY(-50%)",
  display: "flex",
  alignItems: "center",
  color: "var(--ink-muted)",
  pointerEvents: "none",
};

export const CodeBox = forwardRef<HTMLInputElement, CodeBoxProps>(
  function CodeBox(
    {
      kind = "lcn",
      variant,
      label,
      required,
      readOnly = false,
      disabled = false,
      style,
      codeProps,
      nameProps,
      onLookup,
      mono = true,
      lookupAriaLabel,
      labelOptions,
      labelValue,
      onLabelChange,
      suggestions = [],
      onSearch,
      onSelect,
      suggestionsLoading,
      clearInvalidOnBlur = true,
    },
    ref
  ) {
    const [isOpen, setIsOpen] = useState(false);
    const [activeIndex, setActiveIndex] = useState(0);
    const [expandCount, setExpandCount] = useState(0);
    const [widthPx, setWidthPx] = useState<number | undefined>(undefined);
    const anchorRef = useRef<HTMLDivElement>(null);
    const dropdownRef = useRef<HTMLDivElement>(null);
    // 첫 확장 시 자연 너비를 캡처 — 이벤트 핸들러 안에서만 접근
    const baseWidthRef = useRef(0);

    const handleExpand = useCallback(() => {
      if (!dropdownRef.current) return;
      // 0→1→2→3→0 순환: 4번째 클릭은 초기 자연 너비로 복귀
      if (expandCount === 0) baseWidthRef.current = dropdownRef.current.offsetWidth;
      const next = expandCount >= 3 ? 0 : expandCount + 1;
      setWidthPx(next === 0 ? undefined : baseWidthRef.current * (1 + next));
      setExpandCount(next);
    }, [expandCount]);

    const handleShrink = useCallback(() => {
      const next = Math.max(0, expandCount - 1);
      setExpandCount(next);
      setWidthPx(next === 0 ? undefined : baseWidthRef.current * (1 + next));
    }, [expandCount]);

    const wrappedSetIsOpen = useCallback((v: boolean) => {
      setIsOpen(v);
      if (v) { setExpandCount(0); setWidthPx(undefined); }
    }, []);

    const hasSuggestions = Boolean(onSearch);

    // name input DOM ref — clearName에서 native setter 방식 적용을 위해 보유
    const nameInputRef = useRef<HTMLInputElement | null>(null);
    // RHF register의 콜백 ref와 우리 ref를 동시에 연결하는 병합 콜백
    const setNameRef = useCallback(
      (el: HTMLInputElement | null) => {
        nameInputRef.current = el;
        const r = (nameProps as React.RefAttributes<HTMLInputElement> | undefined)?.ref;
        if (typeof r === "function") r(el);
        else if (r) (r as React.MutableRefObject<HTMLInputElement | null>).current = el;
      },
      [nameProps]
    );

    const handleSelectItem = useCallback(
      (item: CodeBoxSuggestion) => {
        // code input 값 세팅: native input value setter + input event로 RHF register도 인식
        const codeNativeInput = (ref as React.RefObject<HTMLInputElement>)?.current;
        if (codeNativeInput) {
          Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")
            ?.set?.call(codeNativeInput, item.code);
          codeNativeInput.dispatchEvent(new Event("input", { bubbles: true }));
        }

        // name input 값 세팅: nameProps.onChange 트리거
        if (nameProps?.onChange) {
          const syntheticEvent = {
            target: { value: item.name, name: nameProps.name ?? "" },
            currentTarget: { value: item.name, name: nameProps.name ?? "" },
          } as React.ChangeEvent<HTMLInputElement>;
          nameProps.onChange(syntheticEvent);
        }

        onSelect?.(item);
        setIsOpen(false);
      },
      [ref, nameProps, onSelect]
    );

    // 코드가 비워질 때 짝꿍 name 필드도 초기화
    // synthetic onChange는 RHF store만 건드리고 uncontrolled DOM 표시값을 바꾸지 못하므로,
    // code input 비우기와 동일한 native setter + input 이벤트 방식 사용
    const clearName = useCallback(() => {
      const el = nameInputRef.current;
      if (!el) return;
      Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value")
        ?.set?.call(el, "");
      el.dispatchEvent(new Event("input", { bubbles: true }));
    }, []);

    const { handleChange, handleKeyDown, handleBlur } = useCodeInputHandlers(
      codeProps,
      onSearch,
      readOnly,
      disabled,
      wrappedSetIsOpen,
      setActiveIndex,
      activeIndex,
      suggestions,
      handleSelectItem,
      isOpen,
      handleExpand,
      handleShrink,
      clearInvalidOnBlur,
      clearName
    );

    if (kind === "code-only") {
      const { className: codeCn, onChange: _origChange, onKeyDown: _origKeyDown, onBlur: _origBlur, ...restCodeProps } = codeProps;
      const baseCodeCn = codeCn ?? (mono ? "text-mono" : undefined);
      const mergedCodeCn = [baseCodeCn, required ? "is-required" : undefined].filter(Boolean).join(" ") || undefined;
      return (
        <div className={variant === "cell" ? "lcn lcn--cell" : "lcn"} style={style}>
          {(label || labelOptions) && (
            <LcnLabel
              options={labelOptions}
              value={labelValue}
              onChange={onLabelChange}
              required={required}
            >
              {label}
            </LcnLabel>
          )}
          <div ref={anchorRef} className="lcn__code" style={{ position: "relative" }}>
            <input
              ref={ref}
              autoComplete="off"
              className={mergedCodeCn}
              readOnly={readOnly}
              disabled={disabled}
              onChange={hasSuggestions ? handleChange : _origChange}
              onKeyDown={hasSuggestions ? handleKeyDown : _origKeyDown}
              onBlur={hasSuggestions ? handleBlur : _origBlur}
              {...restCodeProps}
            />
            {onLookup && !readOnly && !disabled ? (
              <button
                type="button"
                onClick={onLookup}
                style={lookupBtnStyle}
                aria-label={lookupAriaLabel ?? "Lookup"}
              >
                <Search size={12} />
              </button>
            ) : onSearch && !readOnly && !disabled ? (
              <span style={searchIndicatorStyle} aria-hidden="true"><Search size={12} /></span>
            ) : null}
            {hasSuggestions && (
              <CodeBoxSuggestions
                items={suggestions}
                loading={suggestionsLoading}
                activeIndex={activeIndex}
                onSelect={handleSelectItem}
                visible={isOpen}
                expandCount={expandCount}
                onExpand={handleExpand}
                anchorRef={anchorRef}
                dropdownRef={dropdownRef}
                width={widthPx}
              />
            )}
          </div>
        </div>
      );
    }

    if (kind === "party-cn") {
      const { onChange: _origChange, onKeyDown: _origKeyDown, onBlur: _origBlur, ...restCodeProps } = codeProps;
      return (
        <div className="party-block">
          <div className="party-block__head">
            <span className={required ? "is-required" : undefined}>{label}</span>
            <div className="party-cn">
              <div ref={anchorRef} className="party-cn__code" style={{ position: "relative" }}>
                <input
                  ref={ref}
                  autoComplete="off"
                  className={mono ? "text-mono" : undefined}
                  readOnly={readOnly}
                  disabled={disabled}
                  onChange={hasSuggestions ? handleChange : _origChange}
                  onKeyDown={hasSuggestions ? handleKeyDown : _origKeyDown}
                  onBlur={hasSuggestions ? handleBlur : _origBlur}
                  {...restCodeProps}
                />
                {onLookup && !readOnly && !disabled ? (
                  <button
                    type="button"
                    onClick={onLookup}
                    style={lookupBtnStyle}
                    aria-label={lookupAriaLabel ?? "Lookup"}
                  >
                    <Search size={12} className="party-cn__icon" />
                  </button>
                ) : onSearch && !readOnly && !disabled ? (
                  <span style={searchIndicatorStyle} aria-hidden="true"><Search size={12} /></span>
                ) : null}
                {hasSuggestions && (
                  <CodeBoxSuggestions
                    items={suggestions}
                    loading={suggestionsLoading}
                    activeIndex={activeIndex}
                    onSelect={handleSelectItem}
                    visible={isOpen}
                    expandCount={expandCount}
                    onExpand={handleExpand}
                    anchorRef={anchorRef}
                    dropdownRef={dropdownRef}
                    width={widthPx}
                  />
                )}
              </div>
              <input
                autoComplete="off"
                className="party-cn__name"
                readOnly={readOnly}
                disabled={disabled}
                {...(nameProps ?? {})}
                ref={setNameRef}
              />
            </div>
          </div>
        </div>
      );
    }

    // kind="lcn" (default)
    const { className: codeCn, onChange: _origChange, onKeyDown: _origKeyDown, onBlur: _origBlur, ...restCodeProps } = codeProps;
    const baseCodeCn = codeCn ?? (mono ? "text-mono" : undefined);
    const mergedCodeCn = [baseCodeCn, required ? "is-required" : undefined].filter(Boolean).join(" ") || undefined;

    return (
      <div className="lcn" style={style}>
        {(label || labelOptions) && (
          <LcnLabel
            options={labelOptions}
            value={labelValue}
            onChange={onLabelChange}
            required={required}
          >
            {label}
          </LcnLabel>
        )}
        <div ref={anchorRef} className="lcn__code" style={{ position: "relative" }}>
          <input
            ref={ref}
            autoComplete="off"
            className={mergedCodeCn}
            readOnly={readOnly}
            disabled={disabled}
            onChange={hasSuggestions ? handleChange : _origChange}
            onKeyDown={hasSuggestions ? handleKeyDown : _origKeyDown}
            onBlur={hasSuggestions ? handleBlur : _origBlur}
            {...restCodeProps}
          />
          {onLookup && !readOnly && !disabled ? (
            <button
              type="button"
              onClick={onLookup}
              style={lookupBtnStyle}
              aria-label={lookupAriaLabel ?? "Lookup"}
            >
              <Search size={12} />
            </button>
          ) : onSearch && !readOnly && !disabled ? (
            <span style={searchIndicatorStyle} aria-hidden="true"><Search size={12} /></span>
          ) : null}
          {hasSuggestions && (
            <CodeBoxSuggestions
              items={suggestions}
              loading={suggestionsLoading}
              activeIndex={activeIndex}
              onSelect={handleSelectItem}
              visible={isOpen}
              expandCount={expandCount}
              onExpand={handleExpand}
              anchorRef={anchorRef}
              dropdownRef={dropdownRef}
              width={widthPx}
            />
          )}
        </div>
        <input
          autoComplete="off"
          className="lcn__name"
          readOnly={readOnly}
          disabled={disabled}
          {...(nameProps ?? {})}
          ref={setNameRef}
        />
      </div>
    );
  }
);
