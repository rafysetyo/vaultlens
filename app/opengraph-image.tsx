import { ImageResponse } from 'next/og'

export const size = { width: 1200, height: 630 }
export const contentType = 'image/png'
export const alt = 'VaultLens — Every clause read by Gemini. Not one name sent to the cloud.'

export default function OpengraphImage() {
  return new ImageResponse(
    (
      <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', width: '100%', height: '100%', background: 'linear-gradient(135deg, #0d1022 0%, #07080f 100%)', padding: 64 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
          <svg width="72" height="72" viewBox="22 22 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="vl" gradientUnits="userSpaceOnUse" x1="30" y1="30" x2="78" y2="78">
                <stop offset="0" stopColor="#1CC1AF" />
                <stop offset="1" stopColor="#50D9EF" />
              </linearGradient>
            </defs>
            <rect x="22" y="22" width="64" height="64" rx="16.6" fill="#11131F" />
            <rect x="22.4" y="22.4" width="63.2" height="63.2" rx="16.2" stroke="#FFFFFF" strokeOpacity="0.1" strokeWidth="0.8" />
            <path d="M31,44 V38 A7,7 0 0 1 38,31 H44 M64,31 H70 A7,7 0 0 1 77,38 V44 M77,64 V70 A7,7 0 0 1 70,77 H64 M44,77 H38 A7,7 0 0 1 31,70 V64" stroke="url(#vl)" strokeWidth="5" strokeLinecap="round" strokeLinejoin="round" />
            <rect x="41" y="41.5" width="18" height="3.4" rx="1.7" fill="#E2E4EF" fillOpacity="0.55" />
            <rect x="41" y="51" width="26" height="6" rx="2" fill="#E2E4EF" />
            <rect x="41" y="63.1" width="12" height="3.4" rx="1.7" fill="#E2E4EF" fillOpacity="0.55" />
          </svg>
          <span style={{ fontSize: 40, fontWeight: 600, color: '#ecf4f2' }}>VaultLens</span>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <span style={{ fontSize: 68, fontWeight: 500, color: '#ecf4f2', letterSpacing: -2, lineHeight: 1.05 }}>Every clause read by Gemini.</span>
          <span style={{ fontSize: 68, fontWeight: 500, color: '#19baa8', letterSpacing: -2, lineHeight: 1.05 }}>Not one name sent to the cloud.</span>
        </div>
        <div style={{ display: 'flex', gap: 16, fontSize: 24, color: '#ecf4f2' }}>
          <span style={{ background: '#1c2040', border: '1px solid #343a6b', padding: '8px 16px', borderRadius: 10 }}>Gemini Nano · on device</span>
          <span style={{ background: '#013630', color: '#a0e5d9', padding: '8px 16px', borderRadius: 10 }}>Gemini 3.8 Flash · placeholders only</span>
          <span style={{ background: '#0f3a44', color: '#a5f3fc', padding: '8px 16px', borderRadius: 10 }}>#BuildHybridAI</span>
        </div>
      </div>
    ),
    size,
  )
}
