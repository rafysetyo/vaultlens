import { generateText, Output } from 'ai'
import { z } from 'zod'
import { CLOUD_MODEL, cloudErrorResponse, guardSanitizedPayload, PLACEHOLDER_RULES } from '@/lib/vault/server-guards'

export const maxDuration = 60

const insightSchema = z.object({
  documentSummary: z.string().describe('2-4 sentence summary of scope, parties (as placeholders) and obligations.'),
  keyDatesAndDeadlines: z.array(z.string()).describe('Up to 6 concrete dates, terms, notice windows or deadlines.'),
  financialAmounts: z.array(z.string()).describe('Up to 6 fees, totals, penalties, caps, deposits or payment terms.'),
  redFlagsAndRisks: z.array(z.string()).describe('Up to 6 asymmetric clauses, liability gaps, auto-renewal traps or payment risks.'),
  suggestedTags: z.array(z.string()).describe('Up to 5 concise 1-3 word tags containing zero PII.'),
})

export async function POST(request: Request) {
  const guarded = guardSanitizedPayload(await request.json().catch(() => null))
  if (!guarded.ok) {
    return Response.json({ error: guarded.error }, { status: guarded.status })
  }
  const { sanitizedText, category, title } = guarded.value

  try {
    const { output } = await generateText({
      model: CLOUD_MODEL,
      instructions: [
        'You are VaultLens Cloud Insight, a precise legal, financial and compliance analyst.',
        PLACEHOLDER_RULES,
        'Return concise, high-signal structured insights.',
      ].join('\n'),
      prompt: [`Document title: ${title}`, `Category: ${category}`, '', 'Sanitized document text:', sanitizedText].join('\n'),
      output: Output.object({ schema: insightSchema }),
    })

    return Response.json({
      documentSummary: output.documentSummary.slice(0, 4000),
      keyDatesAndDeadlines: output.keyDatesAndDeadlines.slice(0, 8),
      financialAmounts: output.financialAmounts.slice(0, 8),
      redFlagsAndRisks: output.redFlagsAndRisks.slice(0, 8),
      suggestedTags: output.suggestedTags
        .map((tag) => tag.trim().slice(0, 40))
        .filter((tag) => tag && !tag.includes('REDACTED'))
        .slice(0, 6),
      model: CLOUD_MODEL,
    })
  } catch (error) {
    return cloudErrorResponse(error, '/api/analyze')
  }
}
