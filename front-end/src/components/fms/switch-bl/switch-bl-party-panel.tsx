"use client";

import type { UseFormReturn } from "react-hook-form";
import type { SwitchBlFormValues } from "./switch-bl-modal";

interface SwitchBlPartyPanelProps {
  form: UseFormReturn<SwitchBlFormValues>;
}

export function SwitchBlPartyPanel({ form }: SwitchBlPartyPanelProps) {
  const { register, getValues, setValue } = form;

  function handleSameAsShipper() {
    setValue("notifyCode", getValues("shipperCode"), { shouldDirty: true });
    setValue("notifyAddress", getValues("shipperAddress"), { shouldDirty: true });
  }

  function handleSameAsConsignee() {
    setValue("notifyCode", getValues("consigneeCode"), { shouldDirty: true });
    setValue("notifyAddress", getValues("consigneeAddress"), { shouldDirty: true });
  }

  return (
    <div className="switch-bl-party-panel">
      {/* Shipper */}
      <div className="party-row">
        <div className="party-row__label">Shipper</div>
        <div className="party-row__fields">
          <input
            {...register("shipperCode")}
            className="party-row__code"
            placeholder="Shipper Code"
          />
          <textarea
            {...register("shipperAddress")}
            className="party-row__address"
            placeholder="Shipper Address"
            rows={3}
          />
        </div>
      </div>

      {/* Consignee */}
      <div className="party-row">
        <div className="party-row__label">Consignee</div>
        <div className="party-row__fields">
          <input
            {...register("consigneeCode")}
            className="party-row__code"
            placeholder="Consignee Code"
          />
          <textarea
            {...register("consigneeAddress")}
            className="party-row__address"
            placeholder="Consignee Address"
            rows={3}
          />
        </div>
      </div>

      {/* Notify */}
      <div className="party-row">
        <div className="party-row__label">
          <span>Notify</span>
          <div className="party-row__label-actions">
            <button
              type="button"
              className="btn btn--xs"
              onClick={handleSameAsShipper}
            >
              Same As Shipper
            </button>
            <button
              type="button"
              className="btn btn--xs"
              onClick={handleSameAsConsignee}
            >
              Same As Consignee
            </button>
          </div>
        </div>
        <div className="party-row__fields">
          <input
            {...register("notifyCode")}
            className="party-row__code"
            placeholder="Notify Code"
          />
          <textarea
            {...register("notifyAddress")}
            className="party-row__address"
            placeholder="Notify Address"
            rows={3}
          />
        </div>
      </div>
    </div>
  );
}
