import { useId } from 'react'
import { cn } from '@/lib/utils'

/**
 * VaultLens mark: a scan viewfinder around three document lines. The bright
 * middle bar is the redaction, which is the only thing the cloud ever sees.
 *
 * This is the same mark as the Android app (android/.../VaultLogo.kt and
 * res/drawable/ic_launcher_foreground.xml). The geometry uses the same grid
 * (viewBox 22 22 64 64) and the colours are fixed brand colours, so the mark
 * looks the same in light and dark mode. Keep public/icon.svg and
 * app/apple-icon.tsx in sync with this file.
 */
export function LogoMark({ className }: { className?: string }) {
  // One gradient id per instance, so several logos on a page never share (or lose) a gradient.
  const gradientId = `vl-${useId().replace(/:/g, '')}`

  return (
    <svg
      viewBox="22 22 64 64"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={cn('size-9', className)}
      aria-hidden="true"
    >
      <defs>
        <linearGradient id={gradientId} gradientUnits="userSpaceOnUse" x1="30" y1="30" x2="78" y2="78">
          <stop offset="0" stopColor="#1CC1AF" />
          <stop offset="1" stopColor="#50D9EF" />
        </linearGradient>
      </defs>
      {/* tile and hairline border */}
      <rect x="22" y="22" width="64" height="64" rx="16.6" fill="#11131F" />
      <rect
        x="22.4"
        y="22.4"
        width="63.2"
        height="63.2"
        rx="16.2"
        stroke="#FFFFFF"
        strokeOpacity="0.1"
        strokeWidth="0.8"
      />
      {/* viewfinder corners */}
      <path
        d="M31,44 V38 A7,7 0 0 1 38,31 H44 M64,31 H70 A7,7 0 0 1 77,38 V44 M77,64 V70 A7,7 0 0 1 70,77 H64 M44,77 H38 A7,7 0 0 1 31,70 V64"
        stroke={`url(#${gradientId})`}
        strokeWidth="5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      {/* document: two muted lines of text and the redaction bar */}
      <rect x="41" y="41.5" width="18" height="3.4" rx="1.7" fill="#E2E4EF" fillOpacity="0.55" />
      <rect x="41" y="51" width="26" height="6" rx="2" fill="#E2E4EF" />
      <rect x="41" y="63.1" width="12" height="3.4" rx="1.7" fill="#E2E4EF" fillOpacity="0.55" />
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
