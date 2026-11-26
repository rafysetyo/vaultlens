import type { InferenceRoute, VaultDocument } from './types'

export const ROUTE_LABEL: Record<InferenceRoute, string> = {
  on_device_nano: 'Gemini Nano · on device',
  cloud_fallback_flash: 'Local filter → Gemini Flash',
}

export const ROUTE_SHORT: Record<InferenceRoute, string> = {
  on_device_nano: 'On device',
  cloud_fallback_flash: 'Cloud fallback',
}

/** 0–1 risk from red-flag count, weighted up for liability and renewal language. */
export function riskScore(doc: VaultDocument): number {
  if (doc.analysisStatus === 'queued_offline') return 0
  const weight = doc.redFlagsAndRisks.reduce((sum, flag) => {
    const heavy = /unlimited|indemn|auto(?:matic)?[- ]?renew|collections|penalt/i.test(flag)
    return sum + (heavy ? 1.4 : 1)
  }, 0)
  return Math.min(1, weight / 5)
}

export function riskLabel(score: number) {
  if (score >= 0.6) return 'High'
  if (score >= 0.3) return 'Medium'
  if (score > 0) return 'Low'
  return 'None'
}

const MONTHS = 'January|February|March|April|May|June|July|August|September|October|November|December'
const DATE_RE = new RegExp(`(${MONTHS})\\s+(\\d{1,2}),\\s*(\\d{4})`)

/** The next concrete calendar date mentioned in the extracted deadlines, if any. */
export function nextDeadline(doc: VaultDocument): { label: string; date: Date } | null {
  const candidates = doc.keyDatesAndDeadlines
    .map((entry) => {
      const match = entry.match(DATE_RE)
      if (!match) return null
      const date = new Date(`${match[1]} ${match[2]}, ${match[3]}`)
      return Number.isNaN(date.getTime()) ? null : { label: entry, date }
    })
    .filter((item): item is { label: string; date: Date } => item !== null)
    .sort((a, b) => b.date.getTime() - a.date.getTime())
  return candidates[0] ?? null
}

export function primaryAmount(doc: VaultDocument): string {
  const first = doc.financialAmounts[0]
  if (!first) return '—'
  const match = first.match(/-?\$[\d,]+(?:\.\d{2})?/)
  return match ? match[0] : '—'
}

export function shortDate(date: Date) {
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', timeZone: 'UTC' })
}

export function relativeTime(iso: string, now = new Date()) {
  const diff = Math.max(0, now.getTime() - new Date(iso).getTime())
  const minutes = Math.round(diff / 60000)
  if (minutes < 1) return 'now'
  if (minutes < 60) return `${minutes}m`
  const hours = Math.round(minutes / 60)
  if (hours < 24) return `${hours}h`
  return `${Math.round(hours / 24)}d`
}

export function buildOutboundPayload(doc: Pick<VaultDocument, 'title' | 'category' | 'sanitizedText'>) {
  return { title: doc.title, category: doc.category, sanitizedText: doc.sanitizedText }
}
