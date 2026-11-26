import Link from 'next/link'
import { ArrowRight, Download } from 'lucide-react'
import { Logo } from '@/components/brand/logo'

export function Closing() {
  return (
    <>
      <section id="android" className="grid gap-10 px-5 py-16 lg:grid-cols-[14rem_minmax(0,1fr)] lg:px-8">
        <p className="text-sm text-muted-foreground">Try it</p>
        <div className="flex max-w-3xl flex-col gap-6">
          <h2 className="text-balance text-3xl font-medium tracking-tight lg:text-4xl">
            Scan a document in the browser. <span className="text-muted-foreground">Build the phone app in Android Studio.</span>
          </h2>
          <p className="text-pretty leading-relaxed text-muted-foreground">
            The web vault mirrors the native app so anyone can try the redaction, the payload inspector, the offline queue and the
            questions in a minute. The Kotlin source runs Gemini Nano on the NPU through Firebase AI Logic.
          </p>
          <div className="flex flex-wrap gap-3">
            <Link
              href="/vault"
              className="inline-flex h-11 items-center gap-2 rounded-xl bg-ink px-5 text-sm font-medium text-primary-foreground transition-opacity hover:opacity-90"
            >
              Open the live vault
              <ArrowRight className="size-4" aria-hidden="true" />
            </Link>
            <a
              href="/vaultlens-android.zip"
              download
              className="inline-flex h-11 items-center gap-2 rounded-xl border bg-card px-5 text-sm font-medium transition-colors hover:bg-muted"
            >
              <Download className="size-4" aria-hidden="true" />
              Android source
            </a>
          </div>
        </div>
      </section>
      <footer className="flex flex-col gap-4 border-t bg-background px-5 py-8 lg:flex-row lg:items-center lg:justify-between lg:px-8">
        <div className="flex flex-col gap-2">
          <Logo />
          <p className="text-sm text-muted-foreground">A private document vault for people who want AI to read their paperwork without handing it over.</p>
        </div>
        <p className="font-mono text-xs text-muted-foreground">Built for #BuildHybridAI · Gemini Nano + Gemini 3.8 Flash · Firebase</p>
      </footer>
    </>
  )
}
