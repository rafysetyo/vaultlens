import { AlertTriangle } from 'lucide-react'
import type { VaultDocument } from '@/lib/vault/types'

export function ClauseExample({ doc }: { doc: VaultDocument }) {
  const clause = doc.sanitizedText.split('\n').find((line) => line.startsWith('3.'))?.replace(/^3\.\s*/, '') ?? ''
  const renewal = doc.sanitizedText.split('\n').find((line) => line.startsWith('4.'))?.replace(/^4\.\s*/, '') ?? ''

  return (
    <section className="grid gap-10 border-b px-5 py-16 lg:grid-cols-[14rem_minmax(0,1fr)] lg:px-8">
      <p className="text-sm text-muted-foreground">One clause, ruled</p>
      <div className="flex max-w-4xl flex-col gap-6">
        <figure className="flex flex-col gap-5 rounded-2xl border bg-card p-6">
          <blockquote className="text-pretty text-2xl font-medium tracking-tight">{`“${clause}”`}</blockquote>
          <figcaption className="font-mono text-xs text-muted-foreground">Clause 3 · Freelance Software Architecture Agreement · 0 identifiers in the request</figcaption>
          <div className="flex gap-3 rounded-xl bg-background p-4 ring-1 ring-border">
            <AlertTriangle className="mt-0.5 size-4 shrink-0 text-destructive" aria-hidden="true" />
            <p className="text-sm leading-relaxed">
              <span className="font-medium">Red flag. </span>
              {doc.redFlagsAndRisks[0]} Gemini read this from placeholder text. It never learned the consultant&apos;s name, and
              did not need to.
            </p>
          </div>
        </figure>
        <figure className="flex flex-col gap-3 rounded-2xl border bg-card p-6">
          <blockquote className="text-pretty text-lg leading-relaxed">{`“${renewal}”`}</blockquote>
          <figcaption className="font-mono text-xs text-muted-foreground">Clause 4 · deadline extracted</figcaption>
          <p className="text-sm leading-relaxed text-muted-foreground">
            {doc.redFlagsAndRisks[1]} VaultLens puts the 45-day notice window on the vault&apos;s deadline list, next to the amount
            it protects.
          </p>
        </figure>
      </div>
    </section>
  )
}
