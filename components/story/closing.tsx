import Link from 'next/link'
import { ArrowRight, Download } from 'lucide-react'
import { Logo } from '@/components/brand/logo'
import { BUTTON_PRIMARY, BUTTON_SECONDARY, Frame, Section, SectionHeading } from './frame'

export function Closing() {
  return (
    <>
      <Section id="android">
        <Frame>
          <div className="flex flex-col items-center gap-6 px-6 py-14 text-center lg:py-20">
            <SectionHeading>
              Scan a document in the browser. <span className="text-muted-foreground">Build the phone app in Android Studio.</span>
            </SectionHeading>
            <p className="max-w-2xl text-pretty leading-relaxed text-muted-foreground">
              The web vault mirrors the native app so anyone can try the redaction, the payload inspector, the offline queue and
              the questions in a minute. The Kotlin source runs Gemini Nano on the NPU through Firebase AI Logic.
            </p>
            <div className="flex flex-wrap justify-center gap-3 pt-2">
              <Link href="/vault" className={BUTTON_PRIMARY}>
                Open the live vault
                <ArrowRight className="size-4" aria-hidden="true" />
              </Link>
              <a href="/vaultlens-android.zip" download className={BUTTON_SECONDARY}>
                <Download className="size-4" aria-hidden="true" />
                Android source
              </a>
            </div>
          </div>
        </Frame>
      </Section>
      <footer className="border-t bg-background px-5 py-8 lg:px-8">
        <div className="mx-auto flex w-full max-w-6xl flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex flex-col gap-2">
            <Logo />
            <p className="max-w-md text-sm text-muted-foreground">
              A private document vault for people who want AI to read their paperwork without handing it over.
            </p>
          </div>
          <p className="font-mono text-xs text-muted-foreground">Built for #BuildHybridAI · Gemini Nano + Gemini 3.8 Flash · Firebase</p>
        </div>
      </footer>
    </>
  )
}
