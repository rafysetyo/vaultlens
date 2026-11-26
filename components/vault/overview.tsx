'use client'

import { useMemo, useState } from 'react'
import { ArrowRight } from 'lucide-react'
import { OverviewChart, type ChartPoint } from '@/components/vault/overview-chart'
import { DocumentsTable } from '@/components/vault/documents-table'
import { useVault } from '@/lib/vault/store'
import { nextDeadline, primaryAmount, shortDate } from '@/lib/vault/format'
import { cn } from '@/lib/utils'

type Metric = 'identifiers' | 'documents' | 'onDevice' | 'flags'

/** Smooth, deterministic November curve ending at 1.0 so the chart lands on today's live total. */
const CURVE = Array.from({ length: 26 }, (_, index) => {
  const t = index / 25
  return Math.min(1, 0.18 + 0.82 * t + 0.035 * Math.sin(index * 1.3))
})

export function Overview({ onScan }: { onScan: () => void }) {
  const { documents, isOnline, isSyncing, openDocument } = useVault()
  const [metric, setMetric] = useState<Metric>('identifiers')

  const totals = useMemo(() => {
    const identifiers = documents.reduce((sum, doc) => sum + doc.piiRedactedCount, 0)
    const onDevice = documents.filter((doc) => doc.inferenceRoute === 'on_device_nano').length
    const flags = documents.reduce((sum, doc) => sum + doc.redFlagsAndRisks.length, 0)
    return {
      identifiers,
      documents: documents.length,
      onDevice: documents.length ? Math.round((onDevice / documents.length) * 100) : 0,
      flags,
    }
  }, [documents])

  const cards: { id: Metric; label: string; value: string; note: string }[] = [
    { id: 'identifiers', label: 'Identifiers redacted', value: String(totals.identifiers), note: '0 sent raw' },
    { id: 'documents', label: 'Documents in vault', value: String(totals.documents), note: 'in memory only' },
    { id: 'onDevice', label: 'Read on device', value: `${totals.onDevice}%`, note: 'Gemini Nano' },
    { id: 'flags', label: 'Red flags found', value: String(totals.flags), note: 'by Gemini Flash' },
  ]

  const chartData: ChartPoint[] = useMemo(() => {
    const target = metric === 'onDevice' ? totals.onDevice : totals[metric]
    return CURVE.map((ratio, index) => ({
      day: `Nov ${index + 1}`,
      value: metric === 'onDevice' ? Math.round(Math.max(40, target - (1 - ratio) * 30)) : Math.round(target * ratio * 10) / 10,
    }))
  }, [metric, totals])

  const deadlines = useMemo(
    () =>
      documents
        .map((doc) => ({ doc, deadline: nextDeadline(doc) }))
        .filter((item): item is { doc: (typeof documents)[number]; deadline: NonNullable<ReturnType<typeof nextDeadline>> } => item.deadline !== null)
        .sort((a, b) => a.deadline.date.getTime() - b.deadline.date.getTime())
        .slice(0, 5),
    [documents],
  )

  const active = cards.find((card) => card.id === metric) ?? cards[0]

  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="flex flex-col gap-1.5">
          <p className="flex items-center gap-2 text-sm text-muted-foreground">
            <span className={cn('size-2 rounded-full ring-4', isOnline ? 'bg-primary ring-primary/15' : 'bg-destructive ring-destructive/15')} aria-hidden="true" />
            {isSyncing ? 'Draining offline queue…' : isOnline ? 'Online · cloud sees placeholders only' : 'Offline · new scans queue locally'}
          </p>
          <h1 className="text-balance text-4xl font-medium tracking-tight text-foreground md:text-5xl">
            November <span className="text-muted-foreground">vault</span>
          </h1>
        </div>
        <button
          type="button"
          onClick={onScan}
          className="inline-flex h-10 items-center gap-2 rounded-xl bg-ink px-4 text-sm font-medium text-primary-foreground transition-opacity hover:opacity-90"
        >
          Scan a document
          <ArrowRight className="size-4" aria-hidden="true" />
        </button>
      </div>

      <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_20rem]">
        <section aria-label="Vault metrics" className="overflow-hidden rounded-2xl border bg-card">
          <div role="tablist" aria-label="Chart metric" className="grid grid-cols-2 border-b md:grid-cols-4">
            {cards.map((card) => (
              <button
                key={card.id}
                role="tab"
                type="button"
                aria-selected={metric === card.id}
                onClick={() => setMetric(card.id)}
                className={cn(
                  'relative flex flex-col gap-2 border-r px-5 py-4 text-left transition-colors last:border-r-0 hover:bg-muted/50',
                  metric === card.id && 'bg-background',
                )}
              >
                <span className="flex items-center gap-2 text-sm text-muted-foreground">
                  <span className={cn('size-2 rounded-[3px]', metric === card.id ? 'bg-primary' : 'bg-chart-5')} aria-hidden="true" />
                  {card.label}
                </span>
                <span className="flex items-baseline gap-2">
                  <span className="text-3xl font-medium tracking-tight text-foreground tabular-nums">{card.value}</span>
                  <span className="font-mono text-xs text-primary">{card.note}</span>
                </span>
                {metric === card.id && <span className="absolute inset-x-5 bottom-0 h-0.5 rounded-full bg-primary" aria-hidden="true" />}
              </button>
            ))}
          </div>
          <div className="px-3 pt-4 pb-2">
            <OverviewChart data={chartData} label={active.label} suffix={metric === 'onDevice' ? '%' : ''} />
          </div>
        </section>

        <section aria-labelledby="deadlines-heading" className="flex flex-col rounded-2xl border bg-card p-5">
          <div className="flex items-center justify-between">
            <h2 id="deadlines-heading" className="text-sm font-medium">
              Next deadlines
            </h2>
            <span className="font-mono text-xs text-muted-foreground">from Gemini</span>
          </div>
          <ul className="mt-4 flex flex-col gap-1">
            {deadlines.map(({ doc, deadline }) => (
              <li key={doc.id}>
                <button
                  type="button"
                  onClick={() => openDocument(doc.id)}
                  className="flex w-full items-center gap-3 rounded-xl px-2 py-2.5 text-left transition-colors hover:bg-muted"
                >
                  <span className="flex w-11 shrink-0 flex-col items-center rounded-lg border bg-background py-1">
                    <span className="font-mono text-[10px] text-muted-foreground uppercase">{shortDate(deadline.date).split(' ')[0]}</span>
                    <span className="text-sm font-medium tabular-nums">{shortDate(deadline.date).split(' ')[1]}</span>
                  </span>
                  <span className="flex min-w-0 flex-1 flex-col">
                    <span className="truncate text-sm text-foreground">{doc.title}</span>
                    <span className="truncate text-xs text-muted-foreground">{deadline.label.split(':')[0]}</span>
                  </span>
                  <span className="font-mono text-xs text-primary">{primaryAmount(doc)}</span>
                </button>
              </li>
            ))}
            {deadlines.length === 0 && <li className="py-6 text-center text-sm text-muted-foreground">No dated obligations yet.</li>}
          </ul>
          <p className="mt-auto border-t pt-4 text-xs leading-relaxed text-muted-foreground">
            Dates and amounts are extracted from placeholder text. Names, IDs and addresses never reach the model.
          </p>
        </section>
      </div>

      <DocumentsTable />
    </div>
  )
}
