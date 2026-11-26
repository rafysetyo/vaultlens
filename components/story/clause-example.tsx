import { AlertTriangle } from 'lucide-react'
import type { VaultDocument } from '@/lib/vault/types'
import { Frame, Pill, Section, SectionHeading } from './frame'

export function ClauseExample({ doc }: { doc: VaultDocument }) {
  const clause = doc.sanitizedText.split('\n').find((line) => line.startsWith('3.'))?.replace(/^3\.\s*/, '') ?? ''
  const renewal = doc.sanitizedText.split('\n').find((line) => line.startsWith('4.'))?.replace(/^4\.\s*/, '') ?? ''

  return (
    <Section>
      <SectionHeading>
        One clause, ruled on. <br className="hidden sm:block" />
        <span className="text-muted-foreground">Gemini never saw who signed it.</span>
      </SectionHeading>
      <Frame bar="Freelance Software Architecture Agreement">
        <div className="grid lg:grid-cols-[3fr_2fr]">
          <figure className="flex flex-col gap-6 p-6 lg:border-r lg:p-10">
            <figcaption className="flex items-center justify-between gap-3">
              <Pill>Clause 3</Pill>
              <span className="font-mono text-xs text-muted-foreground">0 identifiers in the request</span>
            </figcaption>
            <blockquote className="text-pretty text-2xl font-medium tracking-tight lg:text-3xl">{`“${clause}”`}</blockquote>
            <div className="flex gap-3 rounded-xl bg-background p-4 ring-1 ring-border">
              <AlertTriangle className="mt-0.5 size-4 shrink-0 text-destructive" aria-hidden="true" />
              <p className="text-sm leading-relaxed">
                <span className="font-medium">Red flag. </span>
                {doc.redFlagsAndRisks[0]} Gemini read this from placeholder text. It never learned the consultant&apos;s name, and
                did not need to.
              </p>
            </div>
          </figure>
          <figure className="flex flex-col gap-6 border-t p-6 lg:border-t-0 lg:p-10">
            <figcaption className="flex items-center justify-between gap-3">
              <Pill>Clause 4</Pill>
              <span className="font-mono text-xs text-muted-foreground">deadline extracted</span>
            </figcaption>
            <blockquote className="text-pretty text-lg leading-relaxed">{`“${renewal}”`}</blockquote>
            <p className="text-sm leading-relaxed text-muted-foreground">
              {doc.redFlagsAndRisks[1]} VaultLens puts the 45-day notice window on the vault&apos;s deadline list, next to the amount
              it protects.
            </p>
          </figure>
        </div>
      </Frame>
    </Section>
  )
}
