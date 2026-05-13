"use client";

import { PageHeadActionsGroup } from "./buttons/page-head-actions-group";
import { PageBundleGroup } from "./buttons/page-bundle-group";
import { IconButtonsGroup } from "./buttons/icon-buttons-group";

export function ButtonsSection() {
  return (
    <div style={{ fontFamily: "inherit", fontSize: 12, maxWidth: 960, margin: "0 auto", padding: 24 }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>Buttons Preview</h1>
      <PageHeadActionsGroup />
      <PageBundleGroup />
      <IconButtonsGroup />
    </div>
  );
}
