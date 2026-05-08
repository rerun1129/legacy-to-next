import { ReactNode } from 'react'
import { ComboBox } from './combo-box'

export interface LcnLabelOption {
  value: string
  label: string
}

interface LcnLabelProps {
  children?: ReactNode
  options?: LcnLabelOption[]
  value?: string
  onChange?: (v: string) => void
  required?: boolean
  name?: string
  onBlur?: React.FocusEventHandler<HTMLInputElement>
}

export function LcnLabel({ children, options, value, onChange, required, name, onBlur }: LcnLabelProps) {
  const requiredClass = required ? ' is-required' : ''

  if (!options || options.length === 0) {
    return <span className={`lcn__label${requiredClass}`}>{children}</span>
  }

  // LcnLabel의 onChange는 (v: string) => void이고, ComboBox의 onChange는
  // React.ChangeEvent<HTMLInputElement> — hidden input의 value를 읽어 위임
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange?.(e.target.value)
  }

  const comboBox = (
    <ComboBox
      variant="label"
      options={options}
      value={value ?? ''}
      onChange={handleChange}
      name={name}
      onBlur={onBlur}
      required={required}
    />
  )

  return <span className={`lcn__label${requiredClass}`}>{comboBox}</span>
}
