'use client'

import { useState } from 'react'
import { AlertTriangle, CalendarClock, CloudOff, Cpu, DollarSign, Eye, EyeOff, Loader2, Send, Trash2 } from 'lucide-react'
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { PlaceholderText } from '@/components/vault/redacted-text'
import { useVault } from '@/lib/vault/store'
import { ROUTE_LABEL } from '@/lib/vault/format'
import type { VaultDocument } from '@/lib/vault/types'

type Turn = { role: 'user' | 'assistant'; text: string }

export function DocumentSheet() {
  const { documents, selectedDocId, openDocument } = useVault()
  const doc = documents.find((item) => item.id === selectedDocId) ?? null

  return (
    <Sheet open={doc !== null} onOpenChange={(open) => !open && openDocument(null)}>
      <SheetContent side="right" className="w-full gap-0 overflow-y-auto p-0 sm:max-w-xl">
        {doc && <DocumentBody key={doc.id} doc={doc} />}
      </SheetContent>
    </Sheet>
  )
}

function DocumentBody({ doc }: { doc: VaultDocument }) {
  const { deleteDocument } = useVault()
  const [showOriginal, setShowOriginal] = useState(false)
  const queued = doc.analysisStatus === 'queued_offline'

  return (
    <>
      <SheetHeader className="gap-3 border-b p-6">
        <div className="flex flex-wrap items-center gap-2 text-xs">
          <span className="rounded-md bg-muted px-2 py-0.5 text-muted-foreground">{doc.category}</span>
          <span className="inline-flex items-center gap-1 rounded-md bg-accent px-2 py-0.5 text-accent-foreground">
            {queued ? <CloudOff className="size-3" aria-hidden="true" /> : <Cpu className="size-3" aria-hidden="true" />}
            {queued ? 'Queued offline' : ROUTE_LABEL[doc.inferenceRoute]}
          </span>
          <span className="rounded-md bg-muted px-2 py-0.5 font-mono text-muted-foreground">{doc.piiRedactedCount} redacted</span>
        </div>
        <SheetTitle className="text-pretty text-xl font-medium tracking-tight">{doc.title}</SheetTitle>
        <SheetDescription className="text-pretty">
          {queued
            ? 'Redacted on device and saved. Gemini reads it as soon as you reconnect.'
            : doc.documentSummary}
        </SheetDescription>
      </SheetHeader>

      <div className="flex flex-col gap-6 p-6">
        {!queued && (
          <div className="grid gap-3">
            <InsightList icon={AlertTriangle} title="Red flags" items={doc.redFlagsAndRisks} tone="risk" />
            <InsightList icon={CalendarClock} title="Dates and deadlines" items={doc.keyDatesAndDeadlines} />
            <InsightList icon={DollarSign} title="Amounts" items={doc.financialAmounts} />
          </div>
        )}

        {doc.tags.length > 0 && (
          <ul className="flex flex-wrap gap-1.5" aria-label="Tags">
            {doc.tags.map((tag) => (
              <li key={tag} className="rounded-full border px-2.5 py-0.5 text-xs text-muted-foreground">
                {tag}
              </li>
            ))}
          </ul>
        )}

        <section aria-labelledby="text-heading" className="flex flex-col gap-2">
          <div className="flex items-center justify-between">
            <h3 id="text-heading" className="text-sm font-medium">
              {showOriginal ? 'Original · on this device only' : 'What the cloud saw'}
            </h3>
            {doc.localOriginalText && (
              <button
                type="button"
                onClick={() => setShowOriginal((value) => !value)}
                className="inline-flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground"
              >
                {showOriginal ? <EyeOff className="size-3.5" aria-hidden="true" /> : <Eye className="size-3.5" aria-hidden="true" />}
                {showOriginal ? 'Show placeholders' : 'Reveal original locally'}
              </button>
            )}
          </div>
          <div className="max-h-72 overflow-y-auto rounded-xl bg-background p-3 ring-1 ring-border">
            {showOriginal && doc.localOriginalText ? (
              <p className="font-mono text-[13px] leading-6 whitespace-pre-wrap">{doc.localOriginalText}</p>
            ) : (
              <PlaceholderText text={doc.sanitizedText} />
            )}
          </div>
        </section>

        {!queued && <AskDocument doc={doc} />}

        <button
          type="button"
          onClick={() => deleteDocument(doc.id)}
          className="inline-flex items-center gap-2 self-start text-xs text-muted-foreground transition-colors hover:text-destructive"
        >
          <Trash2 className="size-3.5" aria-hidden="true" />
          Delete from vault
        </button>
      </div>
    </>
  )
}

function InsightList({ icon: Icon, title, items, tone }: { icon: typeof AlertTriangle; title: string; items: string[]; tone?: 'risk' }) {
  if (items.length === 0) return null
  return (
    <section className="rounded-xl border p-4">
      <h3 className="flex items-center gap-2 text-sm font-medium">
        <Icon className={tone === 'risk' ? 'size-4 text-destructive' : 'size-4 text-muted-foreground'} aria-hidden="true" />
        {title}
        <span className="font-mono text-xs text-muted-foreground">{items.length}</span>
      </h3>
      <ul className="mt-3 flex flex-col gap-2">
        {items.map((item) => (
          <li key={item} className="flex gap-2 text-sm leading-relaxed text-foreground">
            <span className={tone === 'risk' ? 'mt-2 size-1.5 shrink-0 rounded-full bg-destructive' : 'mt-2 size-1.5 shrink-0 rounded-full bg-primary'} aria-hidden="true" />
            {item}
          </li>
        ))}
      </ul>
    </section>
  )
}

function AskDocument({ doc }: { doc: VaultDocument }) {
  const [question, setQuestion] = useState('')
  const [turns, setTurns] = useState<Turn[]>([])
  const [pending, setPending] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { isOnline } = useVault()

  async function ask() {
    const trimmed = question.trim()
    if (trimmed.length < 2 || pending) return
    setPending(true)
    setError(null)
    setQuestion('')
    const history = turns
    setTurns((prev) => [...prev, { role: 'user', text: trimmed }])
    try {
      const response = await fetch('/api/ask', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ sanitizedText: doc.sanitizedText, category: doc.category, title: doc.title, question: trimmed, history }),
      })
      const payload = await response.json()
      if (!response.ok) throw new Error(payload?.error ?? 'Could not answer.')
      setTurns((prev) => [...prev, { role: 'assistant', text: payload.answer }])
    } catch (askError) {
      setError(askError instanceof Error ? askError.message : 'Could not answer.')
    } finally {
      setPending(false)
    }
  }

  return (
    <section aria-labelledby="ask-heading" className="flex flex-col gap-3 rounded-xl border p-4">
      <h3 id="ask-heading" className="text-sm font-medium">
        Ask about this document
      </h3>
      {turns.length === 0 && (
        <div className="flex flex-wrap gap-1.5">
          {['What is my biggest risk?', 'When must I give notice?', 'What do I owe in total?'].map((suggestion) => (
            <button
              key={suggestion}
              type="button"
              onClick={() => setQuestion(suggestion)}
              className="rounded-full bg-muted px-2.5 py-1 text-xs text-muted-foreground hover:text-foreground"
            >
              {suggestion}
            </button>
          ))}
        </div>
      )}
      <ol className="flex flex-col gap-2" aria-live="polite">
        {turns.map((turn, index) => (
          <li
            key={index}
            className={
              turn.role === 'user'
                ? 'self-end rounded-2xl rounded-br-md bg-ink px-3 py-2 text-sm text-primary-foreground'
                : 'self-start rounded-2xl rounded-bl-md bg-muted px-3 py-2 text-sm leading-relaxed'
            }
          >
            {turn.text}
          </li>
        ))}
      </ol>
      {error && (
        <p role="alert" className="text-xs text-destructive">
          {error}
        </p>
      )}
      <form
        onSubmit={(event) => {
          event.preventDefault()
          void ask()
        }}
        className="flex items-center gap-2"
      >
        <label htmlFor="ask-input" className="sr-only">
          Question
        </label>
        <input
          id="ask-input"
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          placeholder={isOnline ? 'Gemini answers from placeholders only' : 'Reconnect to ask Gemini'}
          disabled={!isOnline}
          className="h-10 flex-1 rounded-xl border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:opacity-50"
        />
        <button
          type="submit"
          disabled={pending || question.trim().length < 2 || !isOnline}
          aria-label="Send question"
          className="flex size-10 items-center justify-center rounded-xl bg-primary text-primary-foreground disabled:opacity-40"
        >
          {pending ? <Loader2 className="size-4 animate-spin" aria-hidden="true" /> : <Send className="size-4" aria-hidden="true" />}
        </button>
      </form>
    </section>
  )
}
