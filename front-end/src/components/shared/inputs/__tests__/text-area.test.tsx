import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { TextArea } from "../text-area";

describe("TextArea", () => {
  it("lineNumbers=false → <textarea> 엘리먼트 렌더", () => {
    render(<TextArea lineNumbers={false} data-testid="ta" />);
    expect(screen.getByTestId("ta").tagName).toBe("TEXTAREA");
  });

  it("lineNumbers=true → LineNumberTextarea 위임 (line-gutter div 존재)", () => {
    const { container } = render(
      <TextArea lineNumbers defaultValue="line1&#10;line2" />
    );
    // LineNumberTextarea는 aria-hidden gutter div를 렌더함
    const gutter = container.querySelector("[aria-hidden]");
    expect(gutter).toBeInTheDocument();
  });

  it("readOnly prop → readOnly attribute 반영", () => {
    render(<TextArea readOnly data-testid="ta" />);
    expect(screen.getByTestId("ta")).toHaveAttribute("readonly");
  });
});
