import React from "react";

const variants = {
  default: "bg-white border border-slate-200 shadow-sm",
  subtle: "bg-slate-50/70 border border-slate-200",
  elevated: "bg-white border border-slate-200 shadow-md",
};

export default function AdminCard({
  children,
  className = "",
  header,
  footer,
  variant = "default",
  bodyClassName = "",
}) {
  return (
    <div className={`rounded-2xl ${variants[variant]} ${className}`}>
      {header ? <div className="px-5 pt-5">{header}</div> : null}
      <div className={`${header ? "px-5 pb-5 pt-4" : "p-5"} ${bodyClassName}`}>
        {children}
      </div>
      {footer ? <div className="px-5 pb-5">{footer}</div> : null}
    </div>
  );
}
