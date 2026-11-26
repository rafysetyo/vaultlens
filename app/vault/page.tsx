import type { Metadata } from 'next'
import { VaultShell } from '@/components/vault/vault-shell'

export const metadata: Metadata = {
  title: 'Vault — VaultLens',
  description: 'Scan a document, redact identifiers on device, and let Gemini read placeholders only.',
}

export default function VaultPage() {
  return <VaultShell />
}
