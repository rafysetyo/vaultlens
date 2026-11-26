import { Check } from 'lucide-react'

const CHECKS = [
  { claim: 'Raw identifiers never reach the model', how: 'Open the Scan tab. The payload panel shows the exact JSON sent, and counts raw values in it.', file: 'components/vault/scanner.tsx' },
  { claim: 'The server double-checks', how: 'A payload that still holds an SSN, email or 16-digit ID is refused with 422 before Gemini is called.', file: 'lib/vault/server-guards.ts' },
  { claim: 'Hybrid routing is real', how: 'Firebase AI Logic: ONLY_ON_DEVICE for raw text (rules take over when AICore is missing), the cloud model for sanitized text only.', file: 'android/…/HybridPrivacyRouter.kt' },
  { claim: 'Storage cannot hold raw text', how: 'Firestore rules enforce per-user isolation and reject any raw-text shadow field.', file: 'firestore.rules' },
  { claim: 'Works offline', how: 'Toggle Simulate offline in the vault, scan, then reconnect. The queue drains on its own.', file: 'android/…/NetworkMonitor.kt' },
]

export function Proof({ caught, total }: { caught: number; total: number }) {
  return (
    <section id="proof" className="grid gap-10 border-b px-5 py-16 lg:grid-cols-[14rem_minmax(0,1fr)] lg:px-8">
      <p className="text-sm text-muted-foreground">What a judge can check</p>
      <div className="flex flex-col gap-6">
        <div className="grid gap-4 sm:grid-cols-3">
          <Figure value={`${caught}/${total}`} label="identifiers caught in the sample set" />
          <Figure value="0" label="raw values in any outbound payload" />
          <Figure value="2" label="Gemini models, one on device, one in the cloud" />
        </div>
        <ul className="overflow-hidden rounded-2xl border bg-card">
          {CHECKS.map((check) => (
            <li key={check.claim} className="grid gap-2 border-b p-5 last:border-b-0 md:grid-cols-[1.5rem_minmax(0,16rem)_minmax(0,1fr)_auto] md:items-start md:gap-4">
              <Check className="mt-0.5 size-4 text-primary" aria-hidden="true" />
              <p className="font-medium">{check.claim}</p>
              <p className="text-sm leading-relaxed text-muted-foreground">{check.how}</p>
              <p className="font-mono text-[11px] text-muted-foreground md:text-right">{check.file}</p>
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}

function Figure({ value, label }: { value: string; label: string }) {
  return (
    <div className="flex flex-col gap-1 rounded-2xl border bg-card p-5">
      <span className="text-4xl font-medium tracking-tight text-foreground tabular-nums">{value}</span>
      <span className="text-sm text-muted-foreground">{label}</span>
    </div>
  )
}
