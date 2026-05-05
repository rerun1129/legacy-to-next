import { ReactNode } from 'react'

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
  onBlur?: React.FocusEventHandler<HTMLSelectElement>
}

export function LcnLabel({ children, options, value, onChange, required, name, onBlur }: LcnLabelProps) {
  const requiredClass = required ? ' is-required' : ''

  if (!options || options.length === 0) {
    return <span className={`lcn__label${requiredClass}`}>{children}</span>
  }

  const select = (
    <select
      className="lcn__label-select"
      value={value ?? ''}
      onChange={e => onChange?.(e.target.value)}
      name={name}
      onBlur={onBlur}
    >
      {options.map(opt => (
        <option key={opt.value} value={opt.value}>{opt.label}</option>
      ))}
    </select>
  )

  if (required) {
    return <span className={`lcn__label${requiredClass}`}>{select}</span>
  }

  return <span className="lcn__label">{select}</span>
}
