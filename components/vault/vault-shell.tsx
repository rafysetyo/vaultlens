'use client'

import { useState } from 'react'
import { AndroidPanel } from '@/components/vault/android-panel'
import { DocumentSheet } from '@/components/vault/document-sheet'
import { DocumentsTable } from '@/components/vault/documents-table'
import { Overview } from '@/components/vault/overview'
import { Scanner } from '@/components/vault/scanner'
import { VaultHeader, type VaultView } from '@/components/vault/vault-header'
import { VaultProvider } from '@/lib/vault/store'

export function VaultShell() {
  const [view, setView] = useState<VaultView>('overview')

  return (
    <VaultProvider>
      <div className="min-h-dvh p-2 sm:p-4 lg:p-8">
        <div className="mx-auto max-w-[1440px] overflow-hidden rounded-[28px] border bg-background shadow-[0_1px_2px_rgba(0,0,0,0.04),0_12px_40px_-12px_rgba(0,0,0,0.12)]">
          <VaultHeader view={view} onViewChange={setView} />
          <main className="px-4 py-8 lg:px-8">
            {view === 'overview' && <Overview onScan={() => setView('scan')} />}
            {view === 'scan' && <Scanner />}
            {view === 'documents' && (
              <div className="flex flex-col gap-8">
                <h1 className="text-balance text-4xl font-medium tracking-tight md:text-5xl">
                  Documents <span className="text-muted-foreground">in the vault</span>
                </h1>
                <DocumentsTable />
              </div>
            )}
            {view === 'android' && <AndroidPanel />}
          </main>
          <footer className="flex flex-wrap items-center justify-between gap-2 border-t px-4 py-4 text-xs text-muted-foreground lg:px-8">
            <span>Demo vault. Names in sample documents are fictional. Everything lives in this tab&apos;s memory and is gone on refresh.</span>
            <span className="font-mono">cloud model · google/gemini-2.5-flash</span>
          </footer>
        </div>
      </div>
      <DocumentSheet />
    </VaultProvider>
  )
}
