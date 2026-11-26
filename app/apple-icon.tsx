import { ImageResponse } from 'next/og'

export const size = { width: 180, height: 180 }
export const contentType = 'image/png'

export default function AppleIcon() {
  return new ImageResponse(
    (
      <svg width="180" height="180" viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
        <rect width="40" height="40" fill="#17171c" />
        <circle cx="20" cy="20" r="11" stroke="#ffffff" strokeWidth="2.4" fill="none" />
        <rect x="13.5" y="14.6" width="9" height="2.2" rx="1.1" fill="#ffffff" fillOpacity="0.55" />
        <rect x="13.5" y="18.9" width="13" height="3.6" rx="1.2" fill="#4f46e5" />
        <rect x="13.5" y="24.6" width="6.5" height="2.2" rx="1.1" fill="#ffffff" fillOpacity="0.55" />
        <path d="M28.2 28.2L32.4 32.4" stroke="#ffffff" strokeWidth="2.6" strokeLinecap="round" />
      </svg>
    ),
    size,
  )
}
