"use client";

import { forwardRef, useState, useCallback } from "react";
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
    },
    ref
  ) {
    const [isOpen, setIsOpen] = useState(false);
    const [activeIndex, setActiveIndex] = useState(0);
    const [expandCount, setExpandCount] = useState(0);

    const handleExpand = useCallback(() => setExpandCount((c) => c + 1), []);
    const handleShrink = useCallback(() => setExpandCount((c) => Math.max(0, c - 1)), []);

    const wrappedSetIsOpen = useCallback((v: boolean) => {
      setIsOpen(v);
      if (v) setExpandCount(0);
    }, []);

    const hasSuggestions = Boolean(onSearch);

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
      handleShrink
    );

    if (kind === "code-only") {
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
          <div className="lcn__code" style={{ position: "relative" }}>
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
              <div className="party-cn__code" style={{ position: "relative" }}>
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
                  />
                )}
              </div>
              <input
                autoComplete="off"
                className="party-cn__name"
                readOnly={readOnly}
                disabled={disabled}
                {...(nameProps ?? {})}
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
        <div className="lcn__code" style={{ position: "relative" }}>
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
            />
          )}
        </div>
        <input
          autoComplete="off"
          className="lcn__name"
          readOnly={readOnly}
          disabled={disabled}
          {...(nameProps ?? {})}
        />
      </div>
    );
  }
);
