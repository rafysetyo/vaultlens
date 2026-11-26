'use client'

import { createContext, useCallback, useContext, useMemo, useRef, useState, useSyncExternalStore } from 'react'
import { SAMPLE_DOCUMENTS, SAMPLE_FOLDERS } from './samples'
import type { DeviceCapability, VaultDocument, VaultFolder } from './types'

export type CloudInsight = {
  documentSummary: string
  keyDatesAndDeadlines: string[]
  financialAmounts: string[]
  redFlagsAndRisks: string[]
  suggestedTags: string[]
}

export type VaultEvent = {
  id: string
  kind: 'redacted' | 'insight' | 'queued' | 'synced' | 'deadline' | 'refused'
  title: string
  detail: string
  atIso: string
  read: boolean
}

type VaultContextValue = {
  documents: VaultDocument[]
  folders: VaultFolder[]
  events: VaultEvent[]
  deviceCapability: DeviceCapability
  setDeviceCapability: (value: DeviceCapability) => void
  isOnline: boolean
  simulateOffline: boolean
  setSimulateOffline: (value: boolean) => void
  isSyncing: boolean
  search: string
  setSearch: (value: string) => void
  selectedDocId: string | null
  openDocument: (id: string | null) => void
  saveDocument: (doc: VaultDocument) => void
  updateDocument: (id: string, patch: Partial<VaultDocument>) => void
  deleteDocument: (id: string) => void
  pushEvent: (event: Omit<VaultEvent, 'id' | 'atIso' | 'read'>) => void
  markAllRead: () => void
}

const VaultContext = createContext<VaultContextValue | null>(null)

function subscribeOnline(callback: () => void) {
  window.addEventListener('online', callback)
  window.addEventListener('offline', callback)
  return () => {
    window.removeEventListener('online', callback)
    window.removeEventListener('offline', callback)
  }
}

export async function requestCloudInsight(doc: Pick<VaultDocument, 'sanitizedText' | 'category' | 'title'>) {
  const response = await fetch('/api/analyze', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sanitizedText: doc.sanitizedText, category: doc.category, title: doc.title }),
  })
  const payload = await response.json()
  if (!response.ok) throw new Error(payload?.error ?? 'Cloud analysis failed.')
  return payload as CloudInsight & { model: string }
}

const seedEvents: VaultEvent[] = [
  {
    id: 'evt_seed_1',
    kind: 'redacted',
    title: 'Contract redacted on device',
    detail: '7 identifiers · Gemini Nano',
    atIso: '2026-11-22T09:30:00.000Z',
    read: false,
  },
  {
    id: 'evt_seed_2',
    kind: 'insight',
    title: 'Insight ready: 3 red flags',
    detail: 'Unlimited indemnity, 45-day auto-renewal',
    atIso: '2026-11-22T09:31:00.000Z',
    read: false,
  },
  {
    id: 'evt_seed_3',
    kind: 'deadline',
    title: 'Lab invoice due Nov 26',
    detail: '$155.00 · $35 surcharge after 30 days',
    atIso: '2026-11-20T14:15:00.000Z',
    read: false,
  },
  {
    id: 'evt_seed_4',
    kind: 'synced',
    title: 'Offline queue drained',
    detail: '1 document analyzed on reconnect',
    atIso: '2026-11-19T08:00:00.000Z',
    read: true,
  },
]

export function VaultProvider({ children }: { children: React.ReactNode }) {
  const [documents, setDocuments] = useState<VaultDocument[]>(SAMPLE_DOCUMENTS)
  const [folders] = useState<VaultFolder[]>(SAMPLE_FOLDERS)
  const [events, setEvents] = useState<VaultEvent[]>(seedEvents)
  const [deviceCapability, setDeviceCapability] = useState<DeviceCapability>('gemini_nano_npu')
  const [simulateOffline, setSimulateOfflineState] = useState(false)
  const [isSyncing, setIsSyncing] = useState(false)
  const [search, setSearch] = useState('')
  const [selectedDocId, setSelectedDocId] = useState<string | null>(null)
  const documentsRef = useRef(documents)
  documentsRef.current = documents

  const browserOnline = useSyncExternalStore(
    subscribeOnline,
    () => navigator.onLine,
    () => true,
  )
  const isOnline = browserOnline && !simulateOffline

  const pushEvent = useCallback((event: Omit<VaultEvent, 'id' | 'atIso' | 'read'>) => {
    setEvents((prev) => [
      { ...event, id: `evt_${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 6)}`, atIso: new Date().toISOString(), read: false },
      ...prev,
    ])
  }, [])

  const updateDocument = useCallback((id: string, patch: Partial<VaultDocument>) => {
    setDocuments((prev) => prev.map((doc) => (doc.id === id ? { ...doc, ...patch, updatedAtIso: new Date().toISOString() } : doc)))
  }, [])

  const drainQueue = useCallback(async () => {
    const queued = documentsRef.current.filter((doc) => doc.analysisStatus === 'queued_offline')
    if (queued.length === 0) return
    setIsSyncing(true)
    let drained = 0
    for (const doc of queued) {
      try {
        const insight = await requestCloudInsight(doc)
        updateDocument(doc.id, {
          analysisStatus: 'analyzed',
          documentSummary: insight.documentSummary,
          keyDatesAndDeadlines: insight.keyDatesAndDeadlines,
          financialAmounts: insight.financialAmounts,
          redFlagsAndRisks: insight.redFlagsAndRisks,
          tags: Array.from(new Set([...doc.tags, ...insight.suggestedTags])).slice(0, 8),
          syncedToCloud: true,
          isPendingWrite: false,
        })
        drained += 1
      } catch {
        // Stays queued; the next reconnect retries it.
      }
    }
    setIsSyncing(false)
    if (drained > 0) {
      pushEvent({ kind: 'synced', title: 'Offline queue drained', detail: `${drained} document${drained === 1 ? '' : 's'} analyzed on reconnect` })
    }
  }, [pushEvent, updateDocument])

  const setSimulateOffline = useCallback(
    (value: boolean) => {
      setSimulateOfflineState(value)
      if (!value && browserOnline) void drainQueue()
    },
    [browserOnline, drainQueue],
  )

  const saveDocument = useCallback((doc: VaultDocument) => {
    setDocuments((prev) => [doc, ...prev.filter((item) => item.id !== doc.id)])
  }, [])

  const deleteDocument = useCallback((id: string) => {
    setDocuments((prev) => prev.filter((doc) => doc.id !== id))
    setSelectedDocId((current) => (current === id ? null : current))
  }, [])

  const markAllRead = useCallback(() => {
    setEvents((prev) => prev.map((event) => ({ ...event, read: true })))
  }, [])

  const value = useMemo<VaultContextValue>(
    () => ({
      documents,
      folders,
      events,
      deviceCapability,
      setDeviceCapability,
      isOnline,
      simulateOffline,
      setSimulateOffline,
      isSyncing,
      search,
      setSearch,
      selectedDocId,
      openDocument: setSelectedDocId,
      saveDocument,
      updateDocument,
      deleteDocument,
      pushEvent,
      markAllRead,
    }),
    [documents, folders, events, deviceCapability, isOnline, simulateOffline, setSimulateOffline, isSyncing, search, selectedDocId, saveDocument, updateDocument, deleteDocument, pushEvent, markAllRead],
  )

  return <VaultContext.Provider value={value}>{children}</VaultContext.Provider>
}

export function useVault() {
  const context = useContext(VaultContext)
  if (!context) throw new Error('useVault must be used inside VaultProvider')
  return context
}
