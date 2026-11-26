import { ImageResponse } from 'next/og'

export const size = { width: 180, height: 180 }
export const contentType = 'image/png'

// Same mark as android/.../VaultLogo.kt and ic_launcher_foreground.xml.
// iOS rounds the corners itself, so the tile is full-bleed (no rx).
export default function AppleIcon() {
  return new ImageResponse(
    (
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="22 22 64 64"
        width="180"
        height="180"
      >
        <defs>
          <linearGradient id="vl" gradientUnits="userSpaceOnUse" x1="30" y1="30" x2="78" y2="78">
            <stop offset="0" stopColor="#1CC1AF" />
            <stop offset="1" stopColor="#50D9EF" />
          </linearGradient>
        </defs>
        <rect x="22" y="22" width="64" height="64" fill="#11131F" />
        <path
          d="M31,44 V38 A7,7 0 0 1 38,31 H44 M64,31 H70 A7,7 0 0 1 77,38 V44 M77,64 V70 A7,7 0 0 1 70,77 H64 M44,77 H38 A7,7 0 0 1 31,70 V64"
          fill="none"
          stroke="url(#vl)"
          strokeWidth="5"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <rect x="41" y="41.5" width="18" height="3.4" rx="1.7" fill="#E2E4EF" fillOpacity="0.55" />
        <rect x="41" y="51" width="26" height="6" rx="2" fill="#E2E4EF" />
        <rect x="41" y="63.1" width="12" height="3.4" rx="1.7" fill="#E2E4EF" fillOpacity="0.55" />
      </svg>
    ),
    { ...size }
  )
}
