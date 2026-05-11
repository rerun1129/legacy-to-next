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

  describe("kind=code-only", () => {
    it("lcn__code 존재, lcn__name 미존재", () => {
      const { container } = render(
        <CodeBox codeProps={{ "data-testid": "code-input" } as React.InputHTMLAttributes<HTMLInputElement>} kind="code-only" />
      );
      expect(container.querySelector(".lcn__code")).toBeInTheDocument();
      expect(container.querySelector(".lcn__name")).not.toBeInTheDocument();
    });

    it("onLookup 미지정 시 Search 버튼 미존재", () => {
      render(
        <CodeBox codeProps={{ "data-testid": "code-input" } as React.InputHTMLAttributes<HTMLInputElement>} kind="code-only" />
      );
      expect(screen.queryByRole("button")).not.toBeInTheDocument();
    });

    it("onLookup 지정 시 Search 버튼 존재 및 클릭 시 호출", () => {
      const onLookup = vi.fn();
      render(
        <CodeBox
          codeProps={{ "data-testid": "code-input" } as React.InputHTMLAttributes<HTMLInputElement>}
          kind="code-only"
          onLookup={onLookup}
          lookupAriaLabel="Lookup"
        />
      );
      const btn = screen.getByRole("button", { name: "Lookup" });
      fireEvent.click(btn);
      expect(onLookup).toHaveBeenCalledOnce();
    });
  });
});
