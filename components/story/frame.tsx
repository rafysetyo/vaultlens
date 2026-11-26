import type { ReactNode } from 'react'

/**
 * Shared layout for the story page. Every section sits in the same centered column as the hero card,
 * so each box shares the same left and right edges.
 */
export function Section({ id, children }: { id?: string; children: ReactNode }) {
  return (
    <section id={id} className="px-5 py-14 lg:px-8 lg:py-20">
      <div className="mx-auto flex w-full max-w-6xl flex-col items-center gap-10 lg:gap-14">{children}</div>
    </section>
  )
}

/** Centered section title. Wrap the quieter half of the sentence in <span className="text-muted-foreground">. */
export function SectionHeading({ children }: { children: ReactNode }) {
  return (
    <h2 className="max-w-4xl text-balance text-center text-3xl leading-[1.1] font-medium tracking-[-0.03em] text-foreground sm:text-4xl lg:text-[2.75rem]">
      {children}
    </h2>
  )
}

/** The hero's double frame: a soft outer ring around one bordered card. Dividers go inside, not between cards. */
export function Frame({ children, bar }: { children: ReactNode; bar?: string }) {
  return (
    <div className="w-full rounded-[28px] border bg-muted/50 p-2 sm:p-3">
      <div className="overflow-hidden rounded-[20px] border bg-card shadow-sm">
        {bar ? (
          <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-3 border-b px-4 py-3">
            <div className="flex gap-1.5" aria-hidden="true">
              <span className="size-2.5 rounded-full bg-border" />
              <span className="size-2.5 rounded-full bg-border" />
              <span className="size-2.5 rounded-full bg-border" />
            </div>
            <span className="max-w-[16rem] truncate text-center font-mono text-xs text-muted-foreground sm:max-w-none">{bar}</span>
            <span />
          </div>
        ) : null}
        {children}
      </div>
    </div>
  )
}

export function Pill({ children }: { children: ReactNode }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border bg-background px-2.5 py-1 text-xs font-medium text-foreground">
      {children}
    </span>
  )
}

export const BUTTON_PRIMARY =
  'inline-flex h-11 items-center gap-2 rounded-full bg-foreground px-5 text-sm font-medium text-background transition-opacity hover:opacity-90 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring'

export const BUTTON_SECONDARY =
  'inline-flex h-11 items-center gap-2 rounded-full border bg-background px-5 text-sm font-medium text-foreground transition-colors hover:bg-muted focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring'
