import { generateText } from 'ai'
import { CLOUD_MODEL, cloudErrorResponse, guardSanitizedPayload, PLACEHOLDER_RULES } from '@/lib/vault/server-guards'

export const maxDuration = 60

type HistoryItem = { role: 'user' | 'assistant'; text: string }

export async function POST(request: Request) {
  const body = (await request.json().catch(() => null)) as Record<string, unknown> | null
  const guarded = guardSanitizedPayload(body)
  if (!guarded.ok) {
    return Response.json({ error: guarded.error }, { status: guarded.status })
  }

  const question = typeof body?.question === 'string' ? body.question.trim() : ''
  if (question.length < 2 || question.length > 1000) {
    return Response.json({ error: 'Ask a question between 2 and 1,000 characters.' }, { status: 400 })
  }

  const history = Array.isArray(body?.history)
    ? (body.history as unknown[])
        .filter(
          (item): item is HistoryItem =>
            Boolean(item) &&
            typeof item === 'object' &&
            ((item as HistoryItem).role === 'user' || (item as HistoryItem).role === 'assistant') &&
            typeof (item as HistoryItem).text === 'string',
        )
        .slice(-6)
        .map((item) => `${item.role === 'user' ? 'User' : 'Assistant'}: ${item.text.slice(0, 800)}`)
        .join('\n')
    : ''

  const { sanitizedText, category, title } = guarded.value

  try {
    const { text } = await generateText({
      model: CLOUD_MODEL,
      instructions: [
        'You are VaultLens Cloud Insight, a concise legal, financial and compliance analyst.',
        PLACEHOLDER_RULES,
        'Answer in 2-5 crisp sentences grounded strictly in the sanitized text. If the answer depends on a redacted value, say so.',
      ].join('\n'),
      prompt: [
        `Document title: ${title}`,
        `Category: ${category}`,
        '',
        'Sanitized document text:',
        sanitizedText,
        ...(history ? ['', 'Recent conversation:', history] : []),
        '',
        `User question: ${question}`,
      ].join('\n'),
    })

    const answer = text.trim()
    if (!answer) {
      return Response.json({ error: 'No answer was generated for this question.' }, { status: 502 })
    }
    return Response.json({ answer: answer.slice(0, 4000), model: CLOUD_MODEL })
  } catch (error) {
    return cloudErrorResponse(error, '/api/ask')
  }
}
