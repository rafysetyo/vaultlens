import { cn } from '@/lib/utils'

/**
 * VaultLens mark: a lens over a document. Two grey lines of text and one solid
 * block, the redaction, which is the only thing the cloud ever sees.
 *
 * The block is the same shape as the redaction bar in the hero. The colors are
 * neutral and fixed, so the mark never clashes with the accent (indigo in light
 * mode, teal in dark mode). The lens is filled with paper so the black block
 * stays visible on the dark tile. Keep app/icon.svg in sync with this file.
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
      {/* tile: neutral ink, with a hairline so it stays visible on a dark page */}
      <rect
        x="0.5"
        y="0.5"
        width="39"
        height="39"
        rx="9.5"
        className="fill-[#111113] stroke-transparent dark:stroke-white/20"
      />
      {/* lens and handle */}
      <circle cx="18" cy="18" r="12.5" fill="#fafafa" />
      <path d="M27.2 27.2L33.6 33.6" stroke="#fafafa" strokeWidth="4.6" strokeLinecap="round" />
      {/* document: two lines of text and the redaction block */}
      <rect x="10.5" y="9.8" width="13" height="3" rx="1.5" fill="#a1a1aa" />
      <rect x="10.5" y="15.4" width="15" height="5.2" rx="1.6" fill="#111113" />
      <rect x="10.5" y="23.2" width="8.5" height="3" rx="1.5" fill="#a1a1aa" />
    </svg>
  )
}

export function Logo({ className, showWordmark = true }: { className?: string; showWordmark?: boolean }) {
  return (
    <span className={cn('inline-flex items-center gap-2.5', className)}>
      <LogoMark />
      {showWordmark && <span className="text-[17px] font-semibold tracking-tight text-foreground">VaultLens</span>}
    </span>
  )
}
