import { cn } from '@/lib/utils'

/**
 * VaultLens mark: a lens ring looking at three lines of a document.
 * The middle line is the redaction — solid indigo, the only thing the cloud ever sees.
 */
export function LogoMark({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 40 40"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={cn('size-9', className)}
      aria-hidden="true"
    >
      <rect width="40" height="40" rx="11" fill="#17171c" />
      <circle cx="20" cy="20" r="11" stroke="#ffffff" strokeWidth="2.4" />
      <rect x="13.5" y="14.6" width="9" height="2.2" rx="1.1" fill="#ffffff" fillOpacity="0.55" />
      <rect x="13.5" y="18.9" width="13" height="3.6" rx="1.2" fill="#4f46e5" />
      <rect x="13.5" y="24.6" width="6.5" height="2.2" rx="1.1" fill="#ffffff" fillOpacity="0.55" />
      <path d="M28.2 28.2L32.4 32.4" stroke="#ffffff" strokeWidth="2.6" strokeLinecap="round" />
    </svg>
  )
}

export function Logo({ className, showWordmark = true }: { className?: string; showWordmark?: boolean }) {
  return (
    <span className={cn('inline-flex items-center gap-2.5', className)}>
      <LogoMark />
      {showWordmark && (
        <span className="text-[17px] font-semibold tracking-tight text-foreground">VaultLens</span>
      )}
    </span>
  )
}
