export function Problem() {
  return (
    <section id="problem" className="grid gap-10 border-b px-5 py-16 lg:grid-cols-[14rem_minmax(0,1fr)] lg:px-8">
      <p className="text-sm text-muted-foreground">The problem</p>
      <div className="flex max-w-3xl flex-col gap-6 text-pretty text-lg leading-relaxed text-foreground">
        <p>
          Marcus Vance has a contract to sign by Friday. Four clauses: the scope, a fee paid Net-60, an indemnity and an
          auto-renewal. Two of them can cost him more than the contract pays.
        </p>
        <p className="text-muted-foreground">
          He wants an AI to read it. But the first paragraph holds his Social Security number, two phone numbers, his
          client&apos;s home address and her email. Pasting it into a chatbot sends all seven to a server he will never audit.
          So he signs without reading closely, like most people do.
        </p>
        <p className="text-2xl font-medium tracking-tight text-balance">
          Everyone assumes the hard part of AI document review is the reasoning. It is not. The risk is in the upload, and it
          happens before the model reads a single clause.
        </p>
        <p className="text-muted-foreground">
          Phones can now run a capable model locally. Gemini Nano is good at spotting who a document is about, and bad at
          long legal reasoning. Gemini 3.8 Flash is the opposite. VaultLens gives each model the half of the job it is good
          at, and draws the privacy line between them.
        </p>
      </div>
    </section>
  )
}
