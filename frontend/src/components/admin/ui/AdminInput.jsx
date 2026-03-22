import React from "react";

export default function AdminInput({
  label,
  helper,
  error,
  className = "",
  inputClassName = "",
  ...props
}) {
  return (
    <label className={`block ${className}`}>
      {label ? (
        <span className="mb-1.5 block text-xs font-semibold text-slate-600">
          {label}
        </span>
      ) : null}
      <input
        className={`w-full rounded-xl border px-3 py-2.5 text-sm text-slate-800 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-100 ${
          error ? "border-rose-300" : "border-slate-200"
        } ${inputClassName}`}
        {...props}
      />
      {helper ? (
        <span className="mt-1 block text-[11px] text-slate-500">{helper}</span>
      ) : null}
      {error ? (
        <span className="mt-1 block text-[11px] text-rose-600">{error}</span>
      ) : null}
    </label>
  );
}
