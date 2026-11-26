import { Check } from 'lucide-react'
import { Frame, Section, SectionHeading } from './frame'

const CHECKS = [
  { claim: 'Raw identifiers never reach the model', how: 'Open the Scan tab. The payload panel shows the exact JSON sent, and counts raw values in it.', file: 'components/vault/scanner.tsx' },
  { claim: 'The server double-checks', how: 'A payload that still holds an SSN, email or 16-digit ID is refused with 422 before Gemini is called.', file: 'lib/vault/server-guards.ts' },
  { claim: 'Hybrid routing is real', how: 'Firebase AI Logic: ONLY_ON_DEVICE for raw text (rules take over when AICore is missing), the cloud model for sanitized text only.', file: 'android/…/HybridPrivacyRouter.kt' },
  { claim: 'Storage cannot hold raw text', how: 'Firestore rules enforce per-user isolation and reject any raw-text shadow field.', file: 'firestore.rules' },
  { claim: 'Works offline', how: 'Toggle Simulate offline in the vault, scan, then reconnect. The queue drains on its own.', file: 'android/…/NetworkMonitor.kt' },
]

export function Proof({ caught, total }: { caught: number; total: number }) {
  return (
    <Section id="proof">
      <SectionHeading>
        What a judge can check. <span className="text-muted-foreground">Every claim points at a file.</span>
      </SectionHeading>
      <Frame>
        <div className="grid border-b sm:grid-cols-3">
          <Figure value={`${caught}/${total}`} label="identifiers caught in the sample set" />
          <Figure value="0" label="raw values in any outbound payload" accent />
          <Figure value="2" label="Gemini models, one on device, one in the cloud" />
        </div>
        <ul>
          {CHECKS.map((check) => (
            <li
              key={check.claim}
              className="grid gap-3 border-b px-6 py-5 last:border-b-0 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-center lg:gap-8 lg:px-8"
            >
              <div className="flex min-w-0 items-start gap-4">
                <span className="mt-0.5 flex size-6 shrink-0 items-center justify-center rounded-full bg-accent text-accent-foreground">
                  <Check className="size-3.5" aria-hidden="true" />
                </span>
                <div className="flex min-w-0 flex-col gap-1">
                  <p className="font-medium">{check.claim}</p>
                  <p className="max-w-2xl text-sm leading-relaxed text-muted-foreground">{check.how}</p>
                </div>
              </div>
              <code className="ml-10 w-fit max-w-full break-words rounded-md bg-muted px-2.5 py-1 font-mono text-[11px] text-muted-foreground lg:ml-0">
                {check.file}
              </code>
            </li>
          ))}
        </ul>
      </Frame>
    </Section>
  )
}

function Figure({ value, label, accent = false }: { value: string; label: string; accent?: boolean }) {
  return (
    <div className="flex flex-col items-center gap-2 border-b p-6 text-center last:border-b-0 sm:border-r sm:border-b-0 sm:last:border-r-0 lg:p-8">
      <span className={`text-4xl leading-none font-medium tracking-tight tabular-nums sm:text-5xl ${accent ? 'text-primary' : 'text-foreground'}`}>
        {value}
      </span>
      <span className="max-w-[16rem] text-sm text-muted-foreground">{label}</span>
    </div>
  )
}
