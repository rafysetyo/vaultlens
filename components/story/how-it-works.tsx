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
    <section id="how" className="flex flex-col gap-10 border-b px-5 py-16 lg:px-8">
      <div className="grid gap-6 lg:grid-cols-[14rem_minmax(0,1fr)]">
        <p className="text-sm text-muted-foreground">How it works</p>
        <h2 className="max-w-3xl text-balance text-3xl font-medium tracking-tight lg:text-4xl">
          Models find. Code redacts. The cloud reasons on placeholders. <span className="text-muted-foreground">The original never leaves.</span>
        </h2>
      </div>
      <ol className="grid overflow-hidden rounded-2xl border bg-card md:grid-cols-2 xl:grid-cols-4">
        {STEPS.map((step) => (
          <li key={step.n} className="flex flex-col gap-3 border-b p-5 md:border-r xl:border-b-0 xl:last:border-r-0">
            <span className="font-mono text-xs text-primary">{step.n}</span>
            <h3 className="text-lg font-medium tracking-tight">{step.title}</h3>
            <p className="text-sm leading-relaxed text-muted-foreground">{step.body}</p>
            <p className="mt-auto pt-2 font-mono text-[11px] text-muted-foreground">{step.where}</p>
          </li>
        ))}
      </ol>
    </section>
  )
}
