import type { RedactionSpan } from '@/lib/vault/types'
import { cn } from '@/lib/utils'

const TOKEN_RE = /(\[REDACTED_[A-Z_]+\])/g
const IS_TOKEN = /^\[REDACTED_[A-Z_]+\]$/

/** Sanitized text with placeholder tokens highlighted — exactly what the cloud model receives. */
export function PlaceholderText({ text, className }: { text: string; className?: string }) {
  const parts = text.split(TOKEN_RE)
  return (
    <p className={cn('font-mono text-[13px] leading-6 whitespace-pre-wrap text-foreground', className)}>
      {parts.map((part, index) =>
        IS_TOKEN.test(part) ? (
          <span key={index} className="rounded-[4px] bg-accent px-1 py-px text-accent-foreground">
            {part}
          </span>
        ) : (
          <span key={index}>{part}</span>
        ),
      )}
    </p>
  )
}

/** Original text with each detected identifier covered by a solid bar. Stays in device memory. */
export function RedactedOriginal({ text, spans, className }: { text: string; spans: RedactionSpan[]; className?: string }) {
  const segments: React.ReactNode[] = []
  let cursor = 0
  spans.forEach((span, index) => {
    if (span.start > cursor) segments.push(<span key={`t${index}`}>{text.slice(cursor, span.start)}</span>)
    segments.push(
      <span
        key={`s${index}`}
        title={`${span.entityType} → ${span.placeholder}`}
        className="relative rounded-[3px] bg-ink text-transparent select-none"
      >
        {text.slice(span.start, span.end)}
        <span className="sr-only">{`[${span.entityType} redacted]`}</span>
      </span>,
    )
    cursor = span.end
  })
  if (cursor < text.length) segments.push(<span key="tail">{text.slice(cursor)}</span>)

  return <p className={cn('font-mono text-[13px] leading-6 whitespace-pre-wrap text-foreground', className)}>{segments}</p>
}
