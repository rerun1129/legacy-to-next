import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { CodeBox } from "../code-box";

const baseProps = {
  codeProps: { "data-testid": "code-input" } as React.InputHTMLAttributes<HTMLInputElement>,
  nameProps: { "data-testid": "name-input" } as React.InputHTMLAttributes<HTMLInputElement>,
  onLookup: vi.fn(),
};

describe("CodeBox", () => {
  it("onLookup 제공 시 Search 버튼 렌더 및 클릭 시 콜백 호출", () => {
    const onLookup = vi.fn();
    render(<CodeBox {...baseProps} onLookup={onLookup} lookupAriaLabel="Search" />);
    const btn = screen.getByRole("button", { name: "Search" });
    fireEvent.click(btn);
    expect(onLookup).toHaveBeenCalledOnce();
  });

  it("kind=lcn + required + label → label에 is-required 클래스 존재", () => {
    render(
      <CodeBox
        {...baseProps}
        kind="lcn"
        required
        label="Port"
      />
    );
    const label = document.querySelector(".lcn__label");
    expect(label).toHaveClass("is-required");
  });

  it("kind=party-cn 렌더 → party-cn__code 클래스 존재", () => {
    const { container } = render(<CodeBox {...baseProps} kind="party-cn" />);
    expect(container.querySelector(".party-cn__code")).toBeInTheDocument();
  });
});
