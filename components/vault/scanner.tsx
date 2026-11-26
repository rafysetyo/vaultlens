'use client'

import { useMemo, useRef, useState } from 'react'
import { CheckCircle2, CloudOff, Cpu, ImageUp, Loader2, ScanLine, Send, ShieldCheck } from 'lucide-react'
import { PlaceholderText, RedactedOriginal } from '@/components/vault/redacted-text'
import { redactPiiLocally, suggestTagsLocally } from '@/lib/vault/pii-engine'
import { SAMPLE_CONTRACT_TEXT, SAMPLE_ID_TEXT, SAMPLE_INVOICE_TEXT } from '@/lib/vault/samples'
import { buildOutboundPayload } from '@/lib/vault/format'
import { requestCloudInsight, useVault } from '@/lib/vault/store'
import type { DocumentCategory, LocalRedactionResult, VaultDocument } from '@/lib/vault/types'
import { cn } from '@/lib/utils'

const SAMPLES: { id: string; label: string; title: string; category: DocumentCategory; text: string }[] = [
  { id: 'contract', label: 'Freelance contract', title: 'Freelance Software Architecture Agreement', category: 'Contract', text: SAMPLE_CONTRACT_TEXT },
  { id: 'invoice', label: 'Lab invoice', title: 'Oakwood Medical Lab Invoice', category: 'Invoice', text: SAMPLE_INVOICE_TEXT },
  { id: 'id', label: 'ID record', title: 'Government Identity Record', category: 'ID/Passport', text: SAMPLE_ID_TEXT },
]

const CATEGORIES: DocumentCategory[] = ['Contract', 'Invoice', 'ID/Passport', 'Legal']

type Phase = 'idle' | 'redacting' | 'redacted' | 'sending'

export function Scanner() {
  const { deviceCapability, isOnline, saveDocument, openDocument, pushEvent } = useVault()
  const [text, setText] = useState(SAMPLE_CONTRACT_TEXT)
  const [title, setTitle] = useState(SAMPLES[0].title)
  const [category, setCategory] = useState<DocumentCategory>('Contract')
  const [customTerms, setCustomTerms] = useState('')
  const [result, setResult] = useState<LocalRedactionResult | null>(null)
  const [phase, setPhase] = useState<Phase>('idle')
  const [ocrStatus, setOcrStatus] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const fileRef = useRef<HTMLInputElement>(null)

  const payload = result ? buildOutboundPayload({ title, category, sanitizedText: result.sanitizedText }) : null
  const payloadJson = payload ? JSON.stringify(payload, null, 2) : ''
  const leakedValues = useMemo(
    () => (result ? result.spans.filter((span) => payloadJson.includes(span.originalValue)).length : 0),
    [result, payloadJson],
  )

  function resetResult() {
    setResult(null)
    setPhase('idle')
    setError(null)
  }

  async function runRedaction() {
    setPhase('redacting')
    setError(null)
    const terms = customTerms.split(',').map((term) => term.trim()).filter(Boolean)
    const redaction = await redactPiiLocally(text, deviceCapability, terms)
    setResult(redaction)
    setPhase('redacted')
  }

  async function runOcr(file: File) {
    setOcrStatus('Loading on-device OCR…')
    setError(null)
    try {
      const { createWorker } = await import('tesseract.js')
      const worker = await createWorker('eng', 1, {
        logger: (message: { status: string; progress: number }) => {
          if (message.status === 'recognizing text') setOcrStatus(`Reading pixels locally · ${Math.round(message.progress * 100)}%`)
        },
      })
      const { data } = await worker.recognize(file)
      await worker.terminate()
      setText(data.text.trim())
      setTitle(file.name.replace(/\.[a-z0-9]+$/i, '').slice(0, 80) || 'Scanned document')
      resetResult()
      setOcrStatus(null)
    } catch (ocrError) {
      setOcrStatus(null)
      setError(ocrError instanceof Error ? ocrError.message : 'OCR failed on this image.')
    }
  }

  async function sendToCloud() {
    if (!result) return
    setPhase('sending')
    setError(null)
    const nowIso = new Date().toISOString()
    const base: VaultDocument = {
      id: `doc_${Date.now().toString(36)}`,
      ownerId: 'local_vault',
      title: title.trim().slice(0, 200) || 'Untitled document',
      category,
      sourceType: 'pasted_text',
      sanitizedText: result.sanitizedText,
      localOriginalText: result.originalText,
      piiRedactedCount: result.spans.length,
      redactionLog: result.redactionLog,
      inferenceRoute: deviceCapability === 'gemini_nano_npu' ? 'on_device_nano' : 'cloud_fallback_flash',
      analysisStatus: 'queued_offline',
      documentSummary: '',
      keyDatesAndDeadlines: [],
      financialAmounts: [],
      redFlagsAndRisks: [],
      tags: suggestTagsLocally(result.sanitizedText, category, result.redactionLog),
      createdAtIso: nowIso,
      updatedAtIso: nowIso,
      syncedToCloud: false,
      isPendingWrite: true,
    }

    pushEvent({ kind: 'redacted', title: `${category} redacted on device`, detail: `${result.spans.length} identifiers · ${result.engineUsed}` })

    if (!isOnline) {
      saveDocument(base)
      pushEvent({ kind: 'queued', title: 'Queued for cloud insight', detail: 'Will sync when you reconnect' })
      openDocument(base.id)
      resetResult()
      return
    }

    try {
      const insight = await requestCloudInsight(base)
      const analyzed: VaultDocument = {
        ...base,
        analysisStatus: 'analyzed',
        documentSummary: insight.documentSummary,
        keyDatesAndDeadlines: insight.keyDatesAndDeadlines,
        financialAmounts: insight.financialAmounts,
        redFlagsAndRisks: insight.redFlagsAndRisks,
        tags: Array.from(new Set([...base.tags, ...insight.suggestedTags])).slice(0, 8),
        syncedToCloud: true,
        isPendingWrite: false,
      }
      saveDocument(analyzed)
      pushEvent({
        kind: 'insight',
        title: `Insight ready: ${insight.redFlagsAndRisks.length} red flag${insight.redFlagsAndRisks.length === 1 ? '' : 's'}`,
        detail: insight.redFlagsAndRisks[0]?.slice(0, 60) ?? 'No risks found',
      })
      openDocument(analyzed.id)
      resetResult()
    } catch (sendError) {
      setPhase('redacted')
      const message = sendError instanceof Error ? sendError.message : 'Cloud analysis failed.'
      setError(message)
      if (message.startsWith('Refused')) pushEvent({ kind: 'refused', title: 'Server refused a payload', detail: 'Raw identifier detected' })
    }
  }

  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-col gap-1.5">
        <p className="flex items-center gap-2 text-sm text-muted-foreground">
          <Cpu className="size-4" aria-hidden="true" />
          {deviceCapability === 'gemini_nano_npu' ? 'Gemini Nano + rules, on device' : 'Deterministic local filter, on device'}
        </p>
        <h1 className="text-balance text-4xl font-medium tracking-tight md:text-5xl">
          Scan, <span className="text-muted-foreground">then redact</span>
        </h1>
        <p className="max-w-2xl text-pretty text-sm leading-relaxed text-muted-foreground">
          Paste text or photograph a page. Identifiers are found and replaced on this device first. You see the exact payload
          before anything is sent, and the cloud only ever receives placeholders.
        </p>
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <section aria-labelledby="source-heading" className="flex flex-col gap-4 rounded-2xl border bg-card p-5">
          <div className="flex items-center justify-between">
            <h2 id="source-heading" className="text-sm font-medium">
              1 · Source on device
            </h2>
            <span className="font-mono text-xs text-muted-foreground">{text.length.toLocaleString()} chars</span>
          </div>

          <div className="flex flex-wrap gap-2">
            {SAMPLES.map((sample) => (
              <button
                key={sample.id}
                type="button"
                onClick={() => {
                  setText(sample.text)
                  setTitle(sample.title)
                  setCategory(sample.category)
                  resetResult()
                }}
                className={cn(
                  'rounded-lg border px-3 py-1.5 text-xs transition-colors',
                  text === sample.text ? 'border-primary bg-accent text-accent-foreground' : 'text-muted-foreground hover:text-foreground',
                )}
              >
                {sample.label}
              </button>
            ))}
            <button
              type="button"
              onClick={() => fileRef.current?.click()}
              className="inline-flex items-center gap-1.5 rounded-lg border px-3 py-1.5 text-xs text-muted-foreground transition-colors hover:text-foreground"
            >
              <ImageUp className="size-3.5" aria-hidden="true" />
              Photo or scan
            </button>
            <input
              ref={fileRef}
              type="file"
              accept="image/*"
              className="sr-only"
              aria-label="Upload a document image for on-device OCR"
              onChange={(event) => {
                const file = event.target.files?.[0]
                if (file) void runOcr(file)
                event.target.value = ''
              }}
            />
          </div>

          {ocrStatus && (
            <p role="status" className="flex items-center gap-2 rounded-lg bg-muted px-3 py-2 text-xs text-muted-foreground">
              <Loader2 className="size-3.5 animate-spin" aria-hidden="true" />
              {ocrStatus}
            </p>
          )}

          <div className="grid gap-3 sm:grid-cols-[1fr_10rem]">
            <label className="flex flex-col gap-1.5 text-xs text-muted-foreground">
              Title
              <input
                value={title}
                onChange={(event) => setTitle(event.target.value)}
                className="h-9 rounded-lg border bg-background px-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />
            </label>
            <label className="flex flex-col gap-1.5 text-xs text-muted-foreground">
              Category
              <select
                value={category}
                onChange={(event) => setCategory(event.target.value as DocumentCategory)}
                className="h-9 rounded-lg border bg-background px-2 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                {CATEGORIES.map((item) => (
                  <option key={item}>{item}</option>
                ))}
              </select>
            </label>
          </div>

          <label className="flex flex-col gap-1.5 text-xs text-muted-foreground">
            Document text
            <textarea
              value={text}
              onChange={(event) => {
                setText(event.target.value)
                if (result) resetResult()
              }}
              rows={12}
              className="resize-y rounded-xl border bg-background p-3 font-mono text-[13px] leading-6 text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </label>

          <label className="flex flex-col gap-1.5 text-xs text-muted-foreground">
            Also hide these words (comma separated)
            <input
              value={customTerms}
              onChange={(event) => setCustomTerms(event.target.value)}
              placeholder="Company name, project codename"
              className="h-9 rounded-lg border bg-background px-3 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </label>

          <button
            type="button"
            onClick={runRedaction}
            disabled={text.trim().length < 10 || phase === 'redacting' || phase === 'sending'}
            className="inline-flex h-10 items-center justify-center gap-2 rounded-xl bg-ink px-4 text-sm font-medium text-primary-foreground transition-opacity hover:opacity-90 disabled:opacity-40"
          >
            {phase === 'redacting' ? <Loader2 className="size-4 animate-spin" aria-hidden="true" /> : <ScanLine className="size-4" aria-hidden="true" />}
            Redact on device
          </button>
        </section>

        <section aria-labelledby="result-heading" aria-live="polite" className="flex flex-col gap-4 rounded-2xl border bg-card p-5">
          <div className="flex items-center justify-between">
            <h2 id="result-heading" className="text-sm font-medium">
              2 · What stays, what leaves
            </h2>
            {result && (
              <span className="font-mono text-xs text-muted-foreground">
                {result.latencyMs} ms · {result.engineUsed}
              </span>
            )}
          </div>

          {!result ? (
            <div className="flex flex-1 flex-col items-center justify-center gap-2 rounded-xl border border-dashed p-10 text-center">
              <ShieldCheck className="size-6 text-muted-foreground" aria-hidden="true" />
              <p className="text-sm text-foreground">Nothing has left this device.</p>
              <p className="max-w-xs text-xs text-muted-foreground">Run the redaction to see the original with identifiers blacked out, and the exact payload the cloud would receive.</p>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-3 divide-x rounded-xl border">
                <Stat label="Identifiers found" value={String(result.spans.length)} />
                <Stat label="Raw values in payload" value={String(leakedValues)} tone={leakedValues === 0 ? 'good' : 'bad'} />
                <Stat label="Payload size" value={`${new Blob([payloadJson]).size} B`} />
              </div>

              <div className="flex flex-col gap-2">
                <p className="text-xs text-muted-foreground">Original · stays in device memory</p>
                <div className="max-h-56 overflow-y-auto rounded-xl bg-background p-3 ring-1 ring-border">
                  <RedactedOriginal text={result.originalText} spans={result.spans} />
                </div>
              </div>

              <div className="flex flex-col gap-2">
                <p className="text-xs text-muted-foreground">Outbound payload · the only thing Gemini sees</p>
                <div className="max-h-56 overflow-y-auto rounded-xl bg-background p-3 ring-1 ring-border">
                  <PlaceholderText text={payloadJson} />
                </div>
              </div>

              {error && (
                <p role="alert" className="rounded-lg bg-destructive/10 px-3 py-2 text-xs text-destructive">
                  {error}
                </p>
              )}

              <button
                type="button"
                onClick={sendToCloud}
                disabled={phase === 'sending' || leakedValues > 0}
                className="inline-flex h-10 items-center justify-center gap-2 rounded-xl bg-primary px-4 text-sm font-medium text-primary-foreground transition-opacity hover:opacity-90 disabled:opacity-40"
              >
                {phase === 'sending' ? (
                  <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                ) : isOnline ? (
                  <Send className="size-4" aria-hidden="true" />
                ) : (
                  <CloudOff className="size-4" aria-hidden="true" />
                )}
                {phase === 'sending' ? 'Gemini 2.5 Flash is reading placeholders…' : isOnline ? 'Send placeholders to Gemini' : 'Save and queue for reconnect'}
              </button>
              <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
                <CheckCircle2 className="size-3.5 text-primary" aria-hidden="true" />
                The server rejects any payload that still contains an SSN, email or 16-digit ID.
              </p>
            </>
          )}
        </section>
      </div>
    </div>
  )
}

function Stat({ label, value, tone }: { label: string; value: string; tone?: 'good' | 'bad' }) {
  return (
    <div className="flex flex-col gap-1 px-4 py-3">
      <span className="text-xs text-muted-foreground">{label}</span>
      <span className={cn('text-xl font-medium tabular-nums', tone === 'good' && 'text-primary', tone === 'bad' && 'text-destructive')}>{value}</span>
    </div>
  )
}
