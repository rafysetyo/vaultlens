import { PlaceholderText, RedactedOriginal } from '@/components/vault/redacted-text'
import type { LocalRedactionResult } from '@/lib/vault/types'

export function Hero({ contract }: { contract: LocalRedactionResult }) {
  const firstParagraphEnd = contract.originalText.indexOf('\n\n1.')
  const cut = firstParagraphEnd > 0 ? firstParagraphEnd : 400
  const spansInCut = contract.spans.filter((span) => span.end <= cut)
  const sanitizedCut = contract.sanitizedText.slice(0, contract.sanitizedText.indexOf('\n\n1.'))

  return (
    <section className="flex flex-col gap-10 px-5 pt-14 pb-12 lg:px-8 lg:pt-20">
      <div className="flex max-w-4xl flex-col gap-5">
        <h1 className="text-balance text-4xl font-medium tracking-tight text-foreground sm:text-5xl lg:text-6xl">
          Every clause read by Gemini. <span className="text-muted-foreground">Not one name sent to the cloud.</span>
        </h1>
        <p className="max-w-2xl text-pretty text-lg leading-relaxed text-muted-foreground">
          VaultLens scans contracts, medical bills and IDs on your phone, finds every personal identifier on device with Gemini
          Nano, and lets Gemini 3.8 Flash reason over placeholders only.
        </p>
        <p className="text-xs text-muted-foreground">
          Elena Rostova, Marcus Vance, Sarah Miller and Oakwood Medical are fictional. The redactions below are a live run of the
          on-device engine on this page.
        </p>
      </div>

      <div className="grid overflow-hidden rounded-2xl border bg-card lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_17rem]">
        <figure className="flex flex-col gap-3 border-b p-5 lg:border-r lg:border-b-0">
          <figcaption className="flex items-center justify-between text-xs text-muted-foreground">
            <span>On the phone</span>
            <span className="font-mono">stays in memory</span>
          </figcaption>
          <RedactedOriginal text={contract.originalText.slice(0, cut)} spans={spansInCut} />
        </figure>
        <figure className="flex flex-col gap-3 border-b p-5 lg:border-r lg:border-b-0">
          <figcaption className="flex items-center justify-between text-xs text-muted-foreground">
            <span>What Gemini receives</span>
            <span className="font-mono">placeholders</span>
          </figcaption>
          <PlaceholderText text={sanitizedCut} />
        </figure>
        <div className="flex flex-col justify-between gap-6 bg-background p-5">
          <div className="flex flex-col gap-1">
            <p className="text-sm text-muted-foreground">Raw identifiers sent to the cloud</p>
            <p className="text-6xl font-medium tracking-tight text-primary tabular-nums">0</p>
            <p className="text-sm text-muted-foreground">
              A copy-paste into a chatbot sends <span className="font-medium text-foreground">{spansInCut.length}</span>
            </p>
          </div>
          <dl className="grid grid-cols-2 gap-3 border-t pt-4 font-mono text-xs">
            <div>
              <dt className="text-muted-foreground">engine</dt>
              <dd className="text-foreground">local rules</dd>
            </div>
            <div>
              <dt className="text-muted-foreground">latency</dt>
              <dd className="text-foreground">{contract.latencyMs} ms</dd>
            </div>
            <div>
              <dt className="text-muted-foreground">network</dt>
              <dd className="text-foreground">none</dd>
            </div>
            <div>
              <dt className="text-muted-foreground">cloud</dt>
              <dd className="text-foreground">gemini-3.8-flash</dd>
            </div>
          </dl>
        </div>
      </div>
    </section>
  )
}
