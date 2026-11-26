import { ImageResponse } from 'next/og'

export const size = { width: 1200, height: 630 }
export const contentType = 'image/png'
export const alt = 'VaultLens — Every clause read by Gemini. Not one name sent to the cloud.'

export default function OpengraphImage() {
  return new ImageResponse(
    (
      <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', width: '100%', height: '100%', background: '#f1f1ef', padding: 64 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
          <svg width="72" height="72" viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="11" fill="#17171c" />
            <circle cx="20" cy="20" r="11" stroke="#ffffff" strokeWidth="2.4" fill="none" />
            <rect x="13.5" y="14.6" width="9" height="2.2" rx="1.1" fill="#ffffff" fillOpacity="0.55" />
            <rect x="13.5" y="18.9" width="13" height="3.6" rx="1.2" fill="#4f46e5" />
            <rect x="13.5" y="24.6" width="6.5" height="2.2" rx="1.1" fill="#ffffff" fillOpacity="0.55" />
            <path d="M28.2 28.2L32.4 32.4" stroke="#ffffff" strokeWidth="2.6" strokeLinecap="round" />
          </svg>
          <span style={{ fontSize: 40, fontWeight: 600, color: '#17171c' }}>VaultLens</span>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <span style={{ fontSize: 68, fontWeight: 500, color: '#17171c', letterSpacing: -2, lineHeight: 1.05 }}>Every clause read by Gemini.</span>
          <span style={{ fontSize: 68, fontWeight: 500, color: '#7a7a85', letterSpacing: -2, lineHeight: 1.05 }}>Not one name sent to the cloud.</span>
        </div>
        <div style={{ display: 'flex', gap: 16, fontSize: 24, color: '#17171c' }}>
          <span style={{ background: '#17171c', color: '#ffffff', padding: '8px 16px', borderRadius: 10 }}>Gemini Nano · on device</span>
          <span style={{ background: '#e8e8fd', color: '#3730a3', padding: '8px 16px', borderRadius: 10 }}>Gemini 3.8 Flash · placeholders only</span>
          <span style={{ background: '#ffffff', padding: '8px 16px', borderRadius: 10 }}>#BuildHybridAI</span>
        </div>
      </div>
    ),
    size,
  )
}
