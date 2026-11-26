'use client'

import Link from 'next/link'
import { useEffect, useRef } from 'react'
import { ChevronsUpDown, Cpu, FileText, LayoutGrid, ScanLine, Search, Smartphone, WifiOff } from 'lucide-react'
import { Logo } from '@/components/brand/logo'
import { ThemeToggle } from '@/components/brand/theme-toggle'
import { Notifications } from '@/components/vault/notifications'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { Switch } from '@/components/ui/switch'
import { useVault } from '@/lib/vault/store'
import { cn } from '@/lib/utils'

export type VaultView = 'overview' | 'scan' | 'documents' | 'android'

const NAV: { id: VaultView; label: string; icon: typeof LayoutGrid }[] = [
  { id: 'overview', label: 'Overview', icon: LayoutGrid },
  { id: 'scan', label: 'Scan', icon: ScanLine },
  { id: 'documents', label: 'Documents', icon: FileText },
  { id: 'android', label: 'Android', icon: Smartphone },
]

export function VaultHeader({ view, onViewChange }: { view: VaultView; onViewChange: (view: VaultView) => void }) {
  const { documents, deviceCapability, setDeviceCapability, simulateOffline, setSimulateOffline, isOnline, search, setSearch } = useVault()
  const searchRef = useRef<HTMLInputElement>(null)
  const onNano = deviceCapability === 'gemini_nano_npu'

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      const target = event.target as HTMLElement | null
      const typing = target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable)
      if (event.key === '/' && !typing) {
        event.preventDefault()
        searchRef.current?.focus()
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [])

  return (
    <header className="flex flex-wrap items-center gap-3 border-b px-4 py-3 lg:flex-nowrap lg:px-6">
      <Link href="/" className="rounded-xl focus-visible:outline-2 focus-visible:outline-offset-2" aria-label="VaultLens home">
        <Logo />
      </Link>

      <span className="hidden h-6 w-px bg-border md:block" aria-hidden="true" />

      <Popover>
        <PopoverTrigger className="hidden items-center gap-2 rounded-lg px-2 py-1.5 text-sm text-foreground transition-colors hover:bg-muted md:flex">
          <Cpu className="size-4 text-muted-foreground" aria-hidden="true" />
          <span>{onNano ? 'Pixel 9 · Gemini Nano' : 'Entry-level · No NPU'}</span>
          {!isOnline && <WifiOff className="size-3.5 text-destructive" aria-label="Offline" />}
          <ChevronsUpDown className="size-3.5 text-muted-foreground" aria-hidden="true" />
        </PopoverTrigger>
        <PopoverContent align="start" sideOffset={8} className="w-80 rounded-2xl p-2">
          <p className="px-2 pt-1 pb-2 text-xs text-muted-foreground">Simulate the device the vault is running on</p>
          {(
            [
              { id: 'gemini_nano_npu', title: 'Pixel 9 · Gemini Nano', body: 'Names found on device by Gemini Nano, then rules redact.' },
              { id: 'cloud_fallback', title: 'Entry-level · No NPU', body: 'Deterministic local filter redacts. Gemini Flash only sees placeholders.' },
            ] as const
          ).map((option) => (
            <button
              key={option.id}
              type="button"
              onClick={() => setDeviceCapability(option.id)}
              aria-pressed={deviceCapability === option.id}
              className={cn(
                'flex w-full flex-col items-start gap-0.5 rounded-xl px-3 py-2.5 text-left transition-colors',
                deviceCapability === option.id ? 'bg-accent' : 'hover:bg-muted',
              )}
            >
              <span className={cn('text-sm font-medium', deviceCapability === option.id ? 'text-accent-foreground' : 'text-foreground')}>
                {option.title}
              </span>
              <span className="text-xs text-muted-foreground">{option.body}</span>
            </button>
          ))}
          <label className="mt-1 flex items-center justify-between gap-3 rounded-xl border px-3 py-2.5">
            <span className="flex flex-col">
              <span className="text-sm font-medium">Simulate offline</span>
              <span className="text-xs text-muted-foreground">Scans queue locally and sync on reconnect.</span>
            </span>
            <Switch checked={simulateOffline} onCheckedChange={setSimulateOffline} aria-label="Simulate offline" />
          </label>
        </PopoverContent>
      </Popover>

      <nav aria-label="Vault sections" className="order-last flex w-full items-center gap-1 overflow-x-auto rounded-2xl bg-muted p-1 lg:order-none lg:mx-auto lg:w-auto">
        {NAV.map((item) => {
          const Icon = item.icon
          const active = view === item.id
          return (
            <button
              key={item.id}
              type="button"
              onClick={() => onViewChange(item.id)}
              aria-current={active ? 'page' : undefined}
              className={cn(
                'flex shrink-0 items-center gap-2 rounded-xl px-3.5 py-2 text-sm transition-all',
                active ? 'bg-card font-medium text-foreground shadow-sm ring-1 ring-border' : 'text-muted-foreground hover:text-foreground',
              )}
            >
              <Icon className="size-4" aria-hidden="true" />
              {item.label}
              {item.id === 'documents' && (
                <span className="rounded-md bg-background px-1.5 font-mono text-[11px] text-muted-foreground ring-1 ring-border">
                  {documents.length}
                </span>
              )}
            </button>
          )
        })}
      </nav>

      <div className="ml-auto flex items-center gap-2 lg:ml-0">
        <label className="relative hidden items-center sm:flex">
          <span className="sr-only">Search the vault</span>
          <Search className="pointer-events-none absolute left-3 size-4 text-muted-foreground" aria-hidden="true" />
          <input
            ref={searchRef}
            value={search}
            onChange={(event) => {
              setSearch(event.target.value)
              if (view !== 'documents' && view !== 'overview') onViewChange('documents')
            }}
            onKeyDown={(event) => {
              if (event.key === 'Escape') setSearch('')
            }}
            placeholder="Search"
            className="h-10 w-44 rounded-xl bg-muted pr-12 pl-9 text-sm outline-none placeholder:text-muted-foreground focus-visible:ring-2 focus-visible:ring-ring xl:w-56"
          />
          <kbd className="pointer-events-none absolute right-2.5 rounded-md bg-card px-1.5 font-mono text-[11px] text-muted-foreground ring-1 ring-border">
            /
          </kbd>
        </label>
        <ThemeToggle />
        <Notifications />
        <span
          className="flex size-10 items-center justify-center rounded-full bg-ink text-sm font-medium text-primary-foreground"
          aria-label="Signed in as demo user MV"
          role="img"
        >
          MV
        </span>
      </div>
    </header>
  )
}
