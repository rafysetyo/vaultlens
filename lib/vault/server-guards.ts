import 'server-only'

export const CLOUD_MODEL = 'google/gemini-2.5-flash'

const ALLOWED_CATEGORIES = new Set(['Contract', 'ID/Passport', 'Invoice', 'Legal'])

const RAW_PII_PATTERNS: RegExp[] = [
  /\b\d{3}-\d{2}-\d{4}\b/, // SSN
  /\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b/, // email
  /\b\d{16}\b/, // 16-digit national id / card
]

export type GuardedPayload = {
  sanitizedText: string
  category: string
  title: string
}

/**
 * Defense in depth: the client already redacts, but the server refuses any payload
 * that still carries a raw SSN, email or 16-digit ID before it reaches Gemini.
 */
export function guardSanitizedPayload(body: unknown): { ok: true; value: GuardedPayload } | { ok: false; error: string; status: number } {
  const { sanitizedText, category, title } = (body ?? {}) as Record<string, unknown>

  if (typeof sanitizedText !== 'string' || sanitizedText.trim().length < 10) {
    return { ok: false, status: 400, error: 'Sanitized document text must be at least 10 characters.' }
  }
  if (sanitizedText.length > 50_000) {
    return { ok: false, status: 400, error: 'Sanitized document text exceeds the 50,000 character limit.' }
  }
  if (RAW_PII_PATTERNS.some((pattern) => pattern.test(sanitizedText))) {
    return {
      ok: false,
      status: 422,
      error: 'Refused: the payload still contains a raw identifier. Redact on device before sending to the cloud.',
    }
  }

  return {
    ok: true,
    value: {
      sanitizedText,
      category: typeof category === 'string' && ALLOWED_CATEGORIES.has(category) ? category : 'Contract',
      title: typeof title === 'string' && title.trim() ? title.trim().slice(0, 200) : 'Sanitized Document',
    },
  }
}

// eslint-disable-next-line no-control-regex
const ANSI_RE = /\u001b\[[0-9;]*m/g

export function cloudErrorResponse(error: unknown, route: string) {
  const raw = (error instanceof Error ? error.message : 'Cloud request failed.').replace(ANSI_RE, '')
  console.error(`[vaultlens] ${route} error:`, raw)
  if (/unauthenticated|api key|AI_GATEWAY/i.test(raw)) {
    return Response.json(
      { error: 'Cloud insight is not connected on this deployment yet. The redaction above ran fully on device; publish to Vercel to enable Gemini.' },
      { status: 503 },
    )
  }
  return Response.json({ error: raw.slice(0, 300) }, { status: 502 })
}

export const PLACEHOLDER_RULES = [
  'You ONLY receive pre-sanitized text where personal identifiers appear as placeholders like [REDACTED_NAME], [REDACTED_ID], [REDACTED_SSN], [REDACTED_PHONE], [REDACTED_EMAIL], [REDACTED_ADDRESS], [REDACTED_ACCOUNT], [REDACTED_DOB], [REDACTED_CUSTOM].',
  'Preserve placeholder tokens verbatim when referring to redacted parties. Never guess or reconstruct redacted values.',
].join('\n')
