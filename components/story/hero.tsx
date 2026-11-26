import Link from 'next/link'
import { ArrowRight, Cloud, Smartphone } from 'lucide-react'
import { PlaceholderText, RedactedOriginal } from '@/components/vault/redacted-text'
import type { LocalRedactionResult } from '@/lib/vault/types'

export function Hero({ contract }: { contract: LocalRedactionResult }) {
  const firstParagraphEnd = contract.originalText.indexOf('\n\n1.')
  const cut = firstParagraphEnd > 0 ? firstParagraphEnd : 400
  const spansInCut = contract.spans.filter((span) => span.end <= cut)
  const sanitizedEnd = contract.sanitizedText.indexOf('\n\n1.')
  const sanitizedCut = contract.sanitizedText.slice(0, sanitizedEnd > 0 ? sanitizedEnd : cut)

  return (
    <section className="relative isolate flex flex-col items-center gap-12 px-5 pt-16 pb-16 sm:gap-14 lg:px-8 lg:pt-24 lg:pb-24">
      <style>{HERO_MOTION}</style>
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-x-0 top-0 -z-10 h-[34rem] bg-[radial-gradient(50%_60%_at_50%_0%,color-mix(in_oklab,var(--primary)_10%,transparent),transparent)]"
      />

      <div className="flex max-w-6xl flex-col items-center gap-6 text-center">
        <h1 className="text-balance text-4xl font-medium tracking-[-0.035em] text-foreground sm:text-5xl lg:text-6xl xl:text-7xl">
          Every clause read by Gemini. <br className="hidden md:block" />
          <span className="text-muted-foreground">Not one name sent to the cloud.</span>
        </h1>
        <p className="max-w-2xl text-balance text-lg leading-relaxed text-muted-foreground">
          VaultLens scans contracts, medical bills and IDs on your phone, finds every personal identifier on device with Gemini
          Nano, and lets Gemini 3.8 Flash reason over placeholders only.
        </p>
        <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
          <Link
            href="/vault"
            className="inline-flex h-11 items-center gap-2 rounded-full bg-foreground px-5 text-sm font-medium text-background transition-opacity hover:opacity-90 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          >
            Open the vault
            <ArrowRight className="size-4" aria-hidden="true" />
          </Link>
          <Link
            href="#how"
            className="inline-flex h-11 items-center rounded-full border bg-background px-5 text-sm font-medium text-foreground transition-colors hover:bg-muted focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          >
            See how it works
          </Link>
        </div>
      </div>

      <div className="flex w-full max-w-6xl flex-col items-center gap-5">
        <div className="w-full rounded-[28px] border bg-muted/50 p-2 sm:p-3">
          <div className="overflow-hidden rounded-[20px] border bg-card shadow-sm">
            <div className="grid grid-cols-[1fr_auto_1fr] items-center border-b px-4 py-3">
              <div className="flex gap-1.5" aria-hidden="true">
                <span className="size-2.5 rounded-full bg-border" />
                <span className="size-2.5 rounded-full bg-border" />
                <span className="size-2.5 rounded-full bg-border" />
              </div>
              <span className="font-mono text-xs text-muted-foreground">live run · {contract.latencyMs} ms</span>
              <span />
            </div>

            <div className="relative grid md:grid-cols-2">
              <figure className="relative flex min-w-0 flex-col gap-5 overflow-hidden p-6 md:border-r lg:p-8">
                <figcaption className="flex items-center justify-between gap-3">
                  <span className="inline-flex items-center gap-1.5 rounded-full border bg-background px-2.5 py-1 text-xs font-medium text-foreground">
                    <Smartphone className="size-3.5 text-muted-foreground" aria-hidden="true" />
                    On the phone
                  </span>
                  <span className="font-mono text-xs text-muted-foreground">stays in memory</span>
                </figcaption>
                <RedactedOriginal text={contract.originalText.slice(0, cut)} spans={spansInCut} />
                <span aria-hidden="true" className="vl-beam pointer-events-none absolute inset-x-0 top-0 h-px bg-primary/60 opacity-0 shadow-[0_0_24px_6px_color-mix(in_oklab,var(--primary)_28%,transparent)]" />
              </figure>
              <figure className="flex min-w-0 flex-col gap-5 border-t p-6 md:border-t-0 lg:p-8">
                <figcaption className="flex items-center justify-between gap-3">
                  <span className="inline-flex items-center gap-1.5 rounded-full border bg-background px-2.5 py-1 text-xs font-medium text-foreground">
                    <Cloud className="size-3.5 text-muted-foreground" aria-hidden="true" />
                    What Gemini receives
                  </span>
                  <span className="font-mono text-xs text-muted-foreground">placeholders</span>
                </figcaption>
                <div className="vl-wipe">
                  <PlaceholderText text={sanitizedCut} />
                </div>
              </figure>
              <span
                aria-hidden="true"
                className="vl-chip absolute top-1/2 left-1/2 hidden size-9 -translate-x-1/2 -translate-y-1/2 items-center justify-center rounded-full border bg-card text-muted-foreground shadow-sm md:flex"
              >
                <ArrowRight className="size-4" />
              </span>
            </div>

            <div className="grid grid-cols-2 border-t md:grid-cols-4">
              <div className={CELL}>
                <BigStat value="0" label="Raw identifiers sent to the cloud" accent />
              </div>
              <div className={CELL}>
                <BigStat value={String(spansInCut.length)} label="A copy-paste into a chatbot sends" />
              </div>
              <div className={CELL}>
                <Facts
                  rows={[
                    ['engine', 'local rules'],
                    ['network', 'none'],
                  ]}
                />
              </div>
              <div className={CELL}>
                <Facts
                  rows={[
                    ['latency', `${contract.latencyMs} ms`],
                    ['cloud', 'gemini-3.8-flash'],
                  ]}
                />
              </div>
            </div>
          </div>
        </div>

        <p className="max-w-2xl text-pretty text-center text-xs leading-relaxed text-muted-foreground">
          Elena Rostova, Marcus Vance, Sarah Miller and Oakwood Medical are fictional. The redactions above are a live run of the
          on-device engine on this page.
        </p>
      </div>
    </section>
  )
}

const CELL =
  'flex flex-col items-center justify-center text-center border-b p-4 sm:p-5 lg:p-6 odd:border-r md:border-r md:border-b-0 md:last:border-r-0 [&:nth-last-child(-n+2)]:border-b-0'

function BigStat({ value, label, accent = false }: { value: string; label: string; accent?: boolean }) {
  return (
    <div className="flex flex-col items-center gap-2">
      <span
        className={`text-4xl leading-none font-medium tracking-tight tabular-nums sm:text-5xl ${accent ? 'text-primary' : 'text-foreground'}`}
      >
        {value}
      </span>
      <span className="text-xs leading-snug text-muted-foreground sm:text-sm">{label}</span>
    </div>
  )
}

function Facts({ rows }: { rows: [string, string][] }) {
  return (
    <dl className="flex flex-col items-center gap-3 font-mono text-xs">
      {rows.map(([term, detail]) => (
        <div key={term} className="flex flex-col items-center gap-0.5">
          <dt className="text-muted-foreground">{term}</dt>
          <dd className="whitespace-nowrap text-foreground">{detail}</dd>
        </div>
      ))}
    </dl>
  )
}

/* One orchestrated moment on load: a scan line crosses the original, placeholders are revealed behind it.
   Everything is gated on no-preference, so reduced-motion visitors simply see the final state. */
const HERO_MOTION = `
@media (prefers-reduced-motion: no-preference) {
  .vl-beam { animation: vl-scan 1.3s cubic-bezier(.45,.05,.3,1) .35s both; }
  .vl-wipe { animation: vl-wipe 1.3s cubic-bezier(.45,.05,.3,1) .35s both; }
  .vl-chip { animation: vl-chip .5s cubic-bezier(.3,1.4,.5,1) 1.5s both; }
}
@keyframes vl-scan { 0% { top: 0; opacity: 0 } 8% { opacity: 1 } 92% { opacity: 1 } 100% { top: 100%; opacity: 0 } }
@keyframes vl-wipe { from { clip-path: inset(0 0 100% 0) } to { clip-path: inset(0 0 -2% 0) } }
@keyframes vl-chip { from { scale: .6; opacity: 0 } to { scale: 1; opacity: 1 } }
`
