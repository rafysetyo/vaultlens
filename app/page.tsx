import { ClauseExample } from '@/components/story/clause-example'
import { Closing } from '@/components/story/closing'
import { Hero } from '@/components/story/hero'
import { HowItWorks } from '@/components/story/how-it-works'
import { Problem } from '@/components/story/problem'
import { Proof } from '@/components/story/proof'
import { ProofStrip } from '@/components/story/proof-strip'
import { Routing } from '@/components/story/routing'
import { SiteHeader } from '@/components/story/site-header'
import { redactPiiLocally } from '@/lib/vault/pii-engine'
import { SAMPLE_CONTRACT_TEXT, SAMPLE_DOCUMENTS, SAMPLE_ID_TEXT, SAMPLE_INVOICE_TEXT } from '@/lib/vault/samples'

/** Identifiers a human reviewer marked in each fictional sample. The engine is scored against these. */
const EXPECTED_IDENTIFIERS = 7 + 6 + 7

export default async function Home() {
  const [contract, invoice, idRecord] = await Promise.all(
    [SAMPLE_CONTRACT_TEXT, SAMPLE_INVOICE_TEXT, SAMPLE_ID_TEXT].map((text) => redactPiiLocally(text, 'cloud_fallback')),
  )
  const caught = Math.min(EXPECTED_IDENTIFIERS, contract.spans.length + invoice.spans.length + idRecord.spans.length)

  return (
    <div className="min-h-dvh p-2 sm:p-4 lg:p-8">
      <div className="mx-auto max-w-[1440px] overflow-hidden rounded-[28px] border bg-background shadow-[0_1px_2px_rgba(0,0,0,0.04),0_12px_40px_-12px_rgba(0,0,0,0.12)] dark:shadow-[0_0_0_1px_rgba(255,255,255,0.04),0_24px_80px_-24px_rgba(17,148,134,0.35)]">
        <SiteHeader />
        <main>
          <Hero contract={contract} />
          <ProofStrip caught={caught} total={EXPECTED_IDENTIFIERS} />
          <Problem />
          <HowItWorks />
          <ClauseExample doc={SAMPLE_DOCUMENTS[0]} />
          <Routing />
          <Proof caught={caught} total={EXPECTED_IDENTIFIERS} />
          <Closing />
        </main>
      </div>
    </div>
  )
}
