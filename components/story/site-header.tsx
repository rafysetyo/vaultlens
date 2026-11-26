import Link from 'next/link'
import { ArrowRight } from 'lucide-react'
import { Logo } from '@/components/brand/logo'

const LINKS = [
  { href: '#problem', label: 'The problem' },
  { href: '#how', label: 'How it works' },
  { href: '#proof', label: 'Proof' },
  { href: '#android', label: 'Android' },
]

export function SiteHeader() {
  return (
    <header className="flex items-center gap-4 border-b px-5 py-3 lg:px-8">
      <Link href="/" aria-label="VaultLens home" className="rounded-xl focus-visible:outline-2 focus-visible:outline-offset-2">
        <Logo />
      </Link>
      <nav aria-label="Page sections" className="mx-auto hidden items-center gap-1 rounded-2xl bg-muted p-1 md:flex">
        {LINKS.map((link) => (
          <a key={link.href} href={link.href} className="rounded-xl px-3.5 py-2 text-sm text-muted-foreground transition-colors hover:bg-card hover:text-foreground">
            {link.label}
          </a>
        ))}
      </nav>
      <Link
        href="/vault"
        className="ml-auto inline-flex h-10 items-center gap-2 rounded-xl bg-ink px-4 text-sm font-medium text-primary-foreground transition-opacity hover:opacity-90 md:ml-0"
      >
        Open the vault
        <ArrowRight className="size-4" aria-hidden="true" />
      </Link>
    </header>
  )
}
