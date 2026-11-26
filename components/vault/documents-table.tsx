'use client'

import { useMemo, useState } from 'react'
import { ArrowUpDown, CloudOff, Cpu, FileSignature, IdCard, Receipt, Scale, Cloud } from 'lucide-react'
import { useVault } from '@/lib/vault/store'
import { nextDeadline, primaryAmount, riskLabel, riskScore, ROUTE_SHORT, shortDate } from '@/lib/vault/format'
import type { DocumentCategory, VaultDocument } from '@/lib/vault/types'
import { cn } from '@/lib/utils'

const CATEGORY_ICON: Record<DocumentCategory, typeof Receipt> = {
  Contract: FileSignature,
  Invoice: Receipt,
  'ID/Passport': IdCard,
  Legal: Scale,
}

type Tab = 'all' | 'risk' | 'queued'
type SortKey = 'created' | 'identifiers' | 'risk'

function matchesSearch(doc: VaultDocument, query: string) {
  if (!query) return true
  const haystack = [doc.title, doc.category, doc.sanitizedText, ...doc.tags].join(' ').toLowerCase()
  return haystack.includes(query.toLowerCase())
}

export function DocumentsTable() {
  const { documents, search, openDocument } = useVault()
  const [tab, setTab] = useState<Tab>('all')
  const [sortKey, setSortKey] = useState<SortKey>('created')

  const searched = useMemo(() => documents.filter((doc) => matchesSearch(doc, search.trim())), [documents, search])
  const atRisk = searched.filter((doc) => riskScore(doc) >= 0.3)
  const queued = searched.filter((doc) => doc.analysisStatus === 'queued_offline')

  const rows = useMemo(() => {
    const base = tab === 'risk' ? atRisk : tab === 'queued' ? queued : searched
    return [...base].sort((a, b) => {
      if (sortKey === 'identifiers') return b.piiRedactedCount - a.piiRedactedCount
      if (sortKey === 'risk') return riskScore(b) - riskScore(a)
      return b.createdAtIso.localeCompare(a.createdAtIso)
    })
  }, [tab, atRisk, queued, searched, sortKey])

  const tabs: { id: Tab; label: string; count: number }[] = [
    { id: 'all', label: 'All documents', count: searched.length },
    { id: 'risk', label: 'At risk', count: atRisk.length },
    { id: 'queued', label: 'Queued offline', count: queued.length },
  ]

  return (
    <section aria-labelledby="documents-heading" className="flex flex-col">
      <h2 id="documents-heading" className="sr-only">
        Documents
      </h2>
      <div className="flex flex-wrap items-end justify-between gap-2">
        <div role="tablist" aria-label="Filter documents" className="flex items-end">
          {tabs.map((item) => (
            <button
              key={item.id}
              role="tab"
              type="button"
              aria-selected={tab === item.id}
              onClick={() => setTab(item.id)}
              className={cn(
                'flex items-center gap-2 rounded-t-2xl px-4 py-3 text-sm transition-colors',
                tab === item.id ? 'border border-b-0 bg-card font-medium text-foreground' : 'text-muted-foreground hover:text-foreground',
              )}
            >
              {item.label}
              <span
                className={cn(
                  'rounded-full px-1.5 font-mono text-[11px] leading-5',
                  tab === item.id ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground',
                )}
              >
                {item.count}
              </span>
            </button>
          ))}
        </div>
        {search && (
          <p className="pb-3 text-xs text-muted-foreground">
            Filtering by <span className="font-mono text-foreground">{`"${search}"`}</span>
          </p>
        )}
      </div>

      <div className="overflow-x-auto rounded-2xl rounded-tl-none border bg-card">
        <table className="w-full min-w-[860px] text-sm">
          <thead>
            <tr className="border-b text-left text-xs text-muted-foreground">
              <th scope="col" className="w-14 px-5 py-3.5 font-medium">
                No
              </th>
              <th scope="col" className="px-3 py-3.5 font-medium">
                Document
              </th>
              <th scope="col" className="px-3 py-3.5 font-medium">
                Category
              </th>
              <th scope="col" className="px-3 py-3.5 font-medium">
                <button type="button" onClick={() => setSortKey('identifiers')} className="inline-flex items-center gap-1 hover:text-foreground">
                  Identifiers <ArrowUpDown className="size-3" aria-hidden="true" />
                </button>
              </th>
              <th scope="col" className="px-3 py-3.5 font-medium">
                Route
              </th>
              <th scope="col" className="px-3 py-3.5 font-medium">
                Amount
              </th>
              <th scope="col" className="px-3 py-3.5 font-medium">
                Deadline
              </th>
              <th scope="col" className="px-5 py-3.5 font-medium">
                <button type="button" onClick={() => setSortKey('risk')} className="inline-flex items-center gap-1 hover:text-foreground">
                  Risk <ArrowUpDown className="size-3" aria-hidden="true" />
                </button>
              </th>
            </tr>
          </thead>
          <tbody>
            {rows.map((doc, index) => {
              const Icon = CATEGORY_ICON[doc.category]
              const score = riskScore(doc)
              const deadline = nextDeadline(doc)
              const queuedDoc = doc.analysisStatus === 'queued_offline'
              return (
                <tr
                  key={doc.id}
                  onClick={() => openDocument(doc.id)}
                  className="cursor-pointer border-b transition-colors last:border-b-0 hover:bg-muted/60"
                >
                  <td className="px-5 py-3.5 font-mono text-xs text-muted-foreground">#{index + 1}</td>
                  <td className="px-3 py-3.5">
                    <button
                      type="button"
                      onClick={(event) => {
                        event.stopPropagation()
                        openDocument(doc.id)
                      }}
                      className="flex items-center gap-3 text-left focus-visible:outline-2 focus-visible:outline-offset-2"
                    >
                      <span className="flex size-8 shrink-0 items-center justify-center rounded-full border bg-background">
                        <Icon className="size-4" aria-hidden="true" />
                      </span>
                      <span className="flex min-w-0 flex-col">
                        <span className="max-w-72 truncate font-medium text-foreground">{doc.title}</span>
                        <span className="max-w-72 truncate text-xs text-muted-foreground">{doc.tags.slice(0, 2).join(' · ') || 'No tags yet'}</span>
                      </span>
                    </button>
                  </td>
                  <td className="px-3 py-3.5 text-muted-foreground">{doc.category}</td>
                  <td className="px-3 py-3.5">
                    <span className="inline-flex items-center gap-1 rounded-md bg-accent px-2 py-0.5 font-mono text-xs text-accent-foreground">
                      {doc.piiRedactedCount} redacted
                    </span>
                  </td>
                  <td className="px-3 py-3.5">
                    <span className="inline-flex items-center gap-1.5 text-muted-foreground">
                      {queuedDoc ? (
                        <CloudOff className="size-3.5" aria-hidden="true" />
                      ) : doc.inferenceRoute === 'on_device_nano' ? (
                        <Cpu className="size-3.5" aria-hidden="true" />
                      ) : (
                        <Cloud className="size-3.5" aria-hidden="true" />
                      )}
                      {queuedDoc ? 'Queued' : ROUTE_SHORT[doc.inferenceRoute]}
                    </span>
                  </td>
                  <td className="px-3 py-3.5 font-medium tabular-nums">{primaryAmount(doc)}</td>
                  <td className="px-3 py-3.5 font-mono text-xs text-muted-foreground">{deadline ? shortDate(deadline.date) : '—'}</td>
                  <td className="px-5 py-3.5">
                    <span className="flex items-center gap-3">
                      <span className="h-1.5 w-28 overflow-hidden rounded-full bg-muted" aria-hidden="true">
                        <span
                          className={cn('block h-full rounded-full', score >= 0.6 ? 'bg-destructive' : score >= 0.3 ? 'bg-primary' : 'bg-ink')}
                          style={{ width: `${Math.max(score * 100, queuedDoc ? 0 : 8)}%` }}
                        />
                      </span>
                      <span className="w-14 text-xs text-muted-foreground">{queuedDoc ? 'Pending' : riskLabel(score)}</span>
                    </span>
                  </td>
                </tr>
              )
            })}
            {rows.length === 0 && (
              <tr>
                <td colSpan={8} className="px-5 py-12 text-center text-sm text-muted-foreground">
                  No documents match this view.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
