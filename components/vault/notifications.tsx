'use client'

import { useState } from 'react'
import { Bell, CalendarClock, CheckCheck, CloudUpload, ScanLine, ShieldAlert, ShieldCheck, Sparkles } from 'lucide-react'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { useVault, type VaultEvent } from '@/lib/vault/store'
import { relativeTime } from '@/lib/vault/format'
import { cn } from '@/lib/utils'

const EVENT_ICON: Record<VaultEvent['kind'], typeof Bell> = {
  redacted: ScanLine,
  insight: Sparkles,
  queued: CloudUpload,
  synced: ShieldCheck,
  deadline: CalendarClock,
  refused: ShieldAlert,
}

type Filter = 'all' | 'risks'

export function Notifications() {
  const { events, markAllRead } = useVault()
  const [filter, setFilter] = useState<Filter>('all')
  const unread = events.filter((event) => !event.read).length
  const riskEvents = events.filter((event) => event.kind === 'insight' || event.kind === 'deadline' || event.kind === 'refused')
  const visible = (filter === 'all' ? events : riskEvents).slice(0, 4)
  const total = filter === 'all' ? events.length : riskEvents.length

  return (
    <Popover>
      <PopoverTrigger
        aria-label={`Notifications, ${unread} unread`}
        className="relative flex size-10 items-center justify-center rounded-full text-foreground transition-colors hover:bg-muted focus-visible:outline-2 focus-visible:outline-offset-2"
      >
        <Bell className="size-[18px]" aria-hidden="true" />
        {unread > 0 && (
          <span className="absolute top-1 right-1 flex size-4 items-center justify-center rounded-full bg-primary text-[10px] font-medium text-primary-foreground">
            {unread}
          </span>
        )}
      </PopoverTrigger>
      <PopoverContent align="end" sideOffset={10} className="w-[22rem] gap-0 rounded-2xl p-0 shadow-xl">
        <div className="flex items-center gap-1 border-b px-3 pt-2">
          {(
            [
              { id: 'all', label: 'All', count: events.length },
              { id: 'risks', label: 'Risks', count: riskEvents.length },
            ] as const
          ).map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setFilter(tab.id)}
              aria-pressed={filter === tab.id}
              className={cn(
                '-mb-px flex items-center gap-1.5 border-b-2 px-2 py-2.5 text-sm transition-colors',
                filter === tab.id ? 'border-primary font-medium text-foreground' : 'border-transparent text-muted-foreground hover:text-foreground',
              )}
            >
              {tab.label}
              <span
                className={cn(
                  'rounded-full px-1.5 text-[11px] leading-4',
                  filter === tab.id ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground',
                )}
              >
                {tab.count}
              </span>
            </button>
          ))}
          <button
            type="button"
            onClick={markAllRead}
            className="ml-auto flex items-center gap-1 py-2.5 text-xs text-muted-foreground transition-colors hover:text-foreground"
          >
            <CheckCheck className="size-3.5" aria-hidden="true" />
            Mark all read
          </button>
        </div>
        <ul className="flex flex-col px-2 py-1">
          {visible.map((event) => {
            const Icon = EVENT_ICON[event.kind]
            return (
              <li key={event.id} className="flex items-start gap-3 border-b border-border/70 px-2 py-3 last:border-b-0">
                <span className="relative flex size-9 shrink-0 items-center justify-center rounded-xl border bg-card">
                  <Icon className="size-4 text-foreground" aria-hidden="true" />
                  {!event.read && <span className="absolute -top-0.5 -right-0.5 size-2 rounded-full bg-primary" aria-label="Unread" />}
                </span>
                <span className="flex min-w-0 flex-1 flex-col">
                  <span className="truncate text-sm font-medium text-foreground">{event.title}</span>
                  <span className="truncate text-xs text-muted-foreground">{event.detail}</span>
                </span>
                <span className="font-mono text-[11px] text-muted-foreground">{relativeTime(event.atIso)}</span>
              </li>
            )
          })}
          {visible.length === 0 && <li className="px-2 py-6 text-center text-sm text-muted-foreground">Nothing here yet.</li>}
        </ul>
        <div className="flex items-center justify-between border-t px-4 py-3 text-xs text-muted-foreground">
          <span>
            Showing {visible.length} of {total}
          </span>
          <span className="font-mono">Raw text never logged</span>
        </div>
      </PopoverContent>
    </Popover>
  )
}
