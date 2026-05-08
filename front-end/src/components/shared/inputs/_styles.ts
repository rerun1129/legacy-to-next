export function panelClass(opts: { required?: boolean } = {}): string {
  return opts.required ? "box-panel is-required" : "box-panel";
}

export function cellClass(opts: { required?: boolean } = {}): string {
  return opts.required ? "grid__cell-input is-required" : "grid__cell-input";
}

export function labelClass(opts: { required?: boolean } = {}): string {
  return opts.required ? "box-label is-required" : "box-label";
}
