import { Frame, Section, SectionHeading } from './frame'

export function Problem() {
  return (
    <Section id="problem">
      <SectionHeading>
        <span className="text-muted-foreground">Everyone assumes the hard part of AI document review is the reasoning.</span> It is not. The risk
        is in the upload, and it happens before the model reads a single clause.
      </SectionHeading>
      <Frame>
        <div className="grid lg:grid-cols-[3fr_2fr]">
          <div className="flex flex-col gap-5 p-6 text-pretty text-base leading-relaxed sm:text-lg lg:border-r lg:p-10">
            <p>
              Marcus Vance has a contract to sign by Friday. Four clauses: the scope, a fee paid Net-60, an indemnity and an
              auto-renewal. Two of them can cost him more than the contract pays.
            </p>
            <p className="text-muted-foreground">
              He wants an AI to read it. But the first paragraph holds his Social Security number, two phone numbers, his
              client&apos;s home address and her email. Pasting it into a chatbot sends all seven to a server he will never
              audit. So he signs without reading closely, like most people do.
            </p>
          </div>
          <div className="flex flex-col gap-5 border-t p-6 text-pretty text-base leading-relaxed sm:text-lg lg:border-t-0 lg:p-10">
            <p>
              Phones can now run a capable model locally. Gemini Nano is good at spotting who a document is about, and bad at
              long legal reasoning. Gemini 3.8 Flash is the opposite. VaultLens gives each model the half of the job it is good
              at, and draws the privacy line between them.
            </p>
          </div>
        </div>
      </Frame>
    </Section>
  )
}
