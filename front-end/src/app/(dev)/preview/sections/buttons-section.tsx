"use client";

import { StandardBtnGroup, UndefinedCssBtnGroup, IconLabelBtnGroup } from "./buttons/standard-btn-groups";
import { TopbarIconGroup, PartyBlockBtnGroup, FieldItemBtnGroup } from "./buttons/screen-btn-groups";

export function ButtonsSection() {
  return (
    <div style={{ fontFamily: "inherit", fontSize: 12, maxWidth: 960, margin: "0 auto", padding: 24 }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>Buttons Preview</h1>
      <StandardBtnGroup />
      <UndefinedCssBtnGroup />
      <IconLabelBtnGroup />
      <TopbarIconGroup />
      <PartyBlockBtnGroup />
      <FieldItemBtnGroup />
    </div>
  );
}
