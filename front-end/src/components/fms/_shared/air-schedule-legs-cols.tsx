import type { UseFormRegister, FieldValues, Path, Control } from "react-hook-form";
import { Controller } from "react-hook-form";
import type { GridColumn } from "@/components/shared/grid-list";
import { TextBox, DateBox } from "@/components/shared/inputs";
import { TimeBox } from "@/components/shared/inputs/time-box";

export interface LegRow {
  id?: string;
  toCode: string;
  byCarrier: string;
  flightNo: string;
  onBoardDt: string;
  onBoardTm: string;
  arrivalDt: string;
  arrivalTm: string;
}

export function buildAirScheduleLegCols<T extends FieldValues>(
  register: UseFormRegister<T>,
  control:  Control<T>,
  fieldArrayName: string,
): GridColumn<LegRow>[] {
  // 동적 경로를 Path<T>로 단언: fieldArrayName은 호출자가 스키마와 맞춰 전달하므로 안전
  function reg(path: string) {
    return register(path as Path<T>);
  }

  return [
    {
      key: "_no", width: 32, align: "center", label: "#", className: "row-num",
      render: (_v, _r, i) => i + 1,
    },
    {
      key: "toCode", width: 50, align: "center", label: "To",
      render: (_v, _r, i) => (
        <TextBox
          variant="cell"
          style={{ fontFamily: "var(--font-mono)", fontWeight: 600 }}
          {...reg(`${fieldArrayName}.${i}.toCode`)}
        />
      ),
    },
    {
      key: "byCarrier", width: 50, align: "center", label: "By",
      render: (_v, _r, i) => (
        <TextBox variant="cell" {...reg(`${fieldArrayName}.${i}.byCarrier`)} />
      ),
    },
    {
      key: "flightNo", width: 60, align: "center", label: "Flight",
      render: (_v, _r, i) => (
        <TextBox
          variant="cell"
          style={{ fontFamily: "var(--font-mono)" }}
          {...reg(`${fieldArrayName}.${i}.flightNo`)}
        />
      ),
    },
    {
      key: "onBoardDt", width: 96, align: "center", label: "On Board",
      render: (_v, _r, i) => (
        <Controller
          name={`${fieldArrayName}.${i}.onBoardDt` as Path<T>}
          control={control}
          render={({ field }) => (
            <DateBox
              variant="cell"
              ref={field.ref}
              name={field.name}
              value={field.value as string}
              onChange={field.onChange}
              onBlur={field.onBlur}
            />
          )}
        />
      ),
    },
    {
      key: "onBoardTm", width: 58, align: "center", label: "Time",
      render: (_v, _r, i) => (
        <Controller
          name={`${fieldArrayName}.${i}.onBoardTm` as Path<T>}
          control={control}
          render={({ field }) => (
            <TimeBox
              variant="cell"
              ref={field.ref}
              name={field.name}
              value={field.value as string}
              onChange={field.onChange}
              onBlur={field.onBlur}
            />
          )}
        />
      ),
    },
    {
      key: "arrivalDt", width: 96, align: "center", label: "Arrival",
      render: (_v, _r, i) => (
        <Controller
          name={`${fieldArrayName}.${i}.arrivalDt` as Path<T>}
          control={control}
          render={({ field }) => (
            <DateBox
              variant="cell"
              ref={field.ref}
              name={field.name}
              value={field.value as string}
              onChange={field.onChange}
              onBlur={field.onBlur}
            />
          )}
        />
      ),
    },
    {
      key: "arrivalTm", width: 58, align: "center", label: "Time",
      render: (_v, _r, i) => (
        <Controller
          name={`${fieldArrayName}.${i}.arrivalTm` as Path<T>}
          control={control}
          render={({ field }) => (
            <TimeBox
              variant="cell"
              ref={field.ref}
              name={field.name}
              value={field.value as string}
              onChange={field.onChange}
              onBlur={field.onBlur}
            />
          )}
        />
      ),
    },
  ];
}
