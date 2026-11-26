'use client'

import { Moon, Sun } from 'lucide-react'
import { useEffect, useState } from 'react'

const STORAGE_KEY = 'vl-theme'

/** Switches between light (default) and dark. The choice is stored in localStorage. */
export function ThemeToggle() {
  const [dark, setDark] = useState(false)

  useEffect(() => {
    setDark(document.documentElement.classList.contains('dark'))
  }, [])

  function toggle() {
    const next = !dark
    setDark(next)
    document.documentElement.classList.toggle('dark', next)
    try {
      localStorage.setItem(STORAGE_KEY, next ? 'dark' : 'light')
    } catch {
      // storage can be unavailable (private mode); the theme still switches for this session
    }
  }

  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={dark ? 'Switch to light mode' : 'Switch to dark mode'}
      title={dark ? 'Light mode' : 'Dark mode'}
      className="inline-flex size-10 shrink-0 items-center justify-center rounded-xl border bg-card text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
    >
      {dark ? <Sun className="size-4" aria-hidden="true" /> : <Moon className="size-4" aria-hidden="true" />}
    </button>
  )
}
