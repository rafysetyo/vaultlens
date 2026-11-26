import { ArrowRight } from 'lucide-react'
import { Frame, Section, SectionHeading } from './frame'

const STEPS = [
  {
    n: '01',
    title: 'The phone reads',
    body: 'ML Kit OCR turns the camera frame into text on device. On the web, Tesseract runs in the browser tab.',
    where: 'LocalOcrProcessor.kt',
  },
  {
    n: '02',
    title: 'Models find',
    body: 'Gemini Nano, through Firebase AI Logic with ONLY_ON_DEVICE, lists the people a document is about. If Nano is missing, the rules below take over. Raw text never goes to the cloud.',
    where: 'HybridPrivacyRouter.kt',
  },
  {
    n: '03',
    title: 'Code redacts',
    body: 'Deterministic rules catch SSNs, IDs, phones, emails, addresses and dates of birth, and swap each for a typed placeholder.',
    where: 'LocalPiiSanitizer.kt · pii-engine.ts',
  },
  {
    n: '04',
    title: 'The cloud reasons on placeholders',
    body: 'Gemini 3.8 Flash returns a summary, deadlines, amounts and red flags, referring to people only as [REDACTED_NAME].',
    where: 'app/api/analyze',
  },
]

export function HowItWorks() {
  return (
    <Section id="how">
      <SectionHeading>
        Models find. Code redacts. The cloud reasons on placeholders. <span className="text-muted-foreground">The original never leaves.</span>
      </SectionHeading>
      <Frame>
        <ol className="grid md:grid-cols-2 xl:grid-cols-4">
          {STEPS.map((step, index) => (
            <li
              key={step.n}
              className="relative flex flex-col gap-3 border-b p-6 last:border-b-0 md:odd:border-r md:[&:nth-last-child(-n+2)]:border-b-0 lg:p-8 xl:border-r xl:border-b-0 xl:last:border-r-0"
            >
              <span className="font-mono text-xs text-primary">{step.n}</span>
              <h3 className="text-lg font-medium tracking-tight">{step.title}</h3>
              <p className="text-sm leading-relaxed text-muted-foreground">{step.body}</p>
              <code className="mt-auto w-fit max-w-full break-words rounded-md bg-muted px-2 py-1 font-mono text-[11px] text-muted-foreground">
                {step.where}
              </code>
              {index < STEPS.length - 1 ? (
                <span
                  aria-hidden="true"
                  className="absolute top-[1.625rem] right-0 z-10 hidden size-7 translate-x-1/2 items-center justify-center rounded-full border bg-card text-muted-foreground shadow-sm xl:flex"
                >
                  <ArrowRight className="size-3.5" />
                </span>
              ) : null}
            </li>
          ))}
        </ol>
      </Frame>
    </Section>
  )
}
