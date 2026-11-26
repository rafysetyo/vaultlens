import { ArrowDown } from 'lucide-react'
import { Frame, Section, SectionHeading } from './frame'

const ROUTES = [
  {
    device: 'Pixel with AICore',
    finds: 'Gemini Nano + rules',
    reasons: 'Gemini 3.8 Flash on placeholders',
    note: 'Best recall on names that carry no label.',
  },
  {
    device: 'Entry-level phone, no NPU',
    finds: 'Deterministic local filter',
    reasons: 'Gemini 3.8 Flash on placeholders',
    note: 'Same privacy line. No phone is left out.',
  },
  {
    device: 'No signal',
    finds: 'Nano or rules, as above',
    reasons: 'Queued in Firestore cache',
    note: 'Drains automatically when the network returns.',
  },
]

export function Routing() {
  return (
    <Section>
      <SectionHeading>
        The privacy line never moves. <span className="text-muted-foreground">Only who draws it does.</span>
      </SectionHeading>
      <Frame>
        <ul className="grid lg:grid-cols-3">
          {ROUTES.map((route) => (
            <li
              key={route.device}
              className="flex flex-col gap-6 border-b p-6 last:border-b-0 lg:border-r lg:border-b-0 lg:p-8 lg:last:border-r-0"
            >
              <h3 className="text-lg font-medium tracking-tight">{route.device}</h3>
              <dl className="flex flex-col items-start gap-3">
                <div className="flex flex-col items-start gap-1.5">
                  <dt className="text-xs text-muted-foreground">Finds identifiers</dt>
                  <dd className="max-w-full break-words rounded-md bg-muted px-2 py-1 font-mono text-xs">{route.finds}</dd>
                </div>
                <ArrowDown className="ml-3 size-3.5 text-muted-foreground" aria-hidden="true" />
                <div className="flex flex-col items-start gap-1.5">
                  <dt className="text-xs text-muted-foreground">Reasons over clauses</dt>
                  <dd className="max-w-full break-words rounded-md bg-accent px-2 py-1 font-mono text-xs text-accent-foreground">
                    {route.reasons}
                  </dd>
                </div>
              </dl>
              <p className="mt-auto text-sm leading-relaxed text-muted-foreground">{route.note}</p>
            </li>
          ))}
        </ul>
      </Frame>
    </Section>
  )
}
