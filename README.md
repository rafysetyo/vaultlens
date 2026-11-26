# VaultLens

**Every clause read by Gemini. Not one name sent to the cloud.**

VaultLens scans contracts, medical bills and IDs on your phone, finds every personal identifier on device with Gemini Nano, and lets Gemini 3.8 Flash reason over placeholders only. Built for #BuildHybridAI.

**Submission platform: Android.** The AI runs through Firebase AI Logic hybrid inference in `android/`. The web vault is a companion demo of the same redaction engine; its cloud step uses Vercel AI Gateway, not Firebase AI Logic.

- `/` — the story, with a live run of the on-device redaction engine
- `/vault` — the working web vault (scan, redact, inspect payload, ask Gemini, offline queue)
- `android/` — the native Kotlin + Jetpack Compose app (Firebase AI Logic, ML Kit, Firestore, App Check)

---

## Inspiration

Marcus Vance (fictional) has a contract to sign by Friday. Two of its four clauses can cost him more than the contract pays. He wants an AI to read it, but the first paragraph holds his SSN, two phone numbers, his client's home address and her email. Pasting it into a chatbot sends all seven to a server he will never audit.

Everyone assumes the hard part of AI document review is the reasoning. It is not. The risk is in the upload, and it happens before the model reads a single clause.

## What it does

Models find. Code redacts. The cloud reasons on placeholders. The original never leaves.

1. **The phone reads.** ML Kit OCR (Android) or Tesseract (browser) turns the page into text on device.
2. **Models find.** Gemini Nano, via Firebase AI Logic `InferenceMode.ONLY_ON_DEVICE`, lists the people a document is about. Raw text is never allowed to fall back to the cloud: if Nano is missing, the SDK throws and the deterministic rules take over.
3. **Code redacts.** Deterministic rules catch SSNs, IDs, phones, emails, addresses and dates of birth and swap each for a typed placeholder like `[REDACTED_SSN]`.
4. **The cloud reasons on placeholders.** Gemini 3.8 Flash returns a summary, deadlines, amounts and red flags, and answers follow-up questions.

| Device | Finds identifiers | Reasons over clauses |
| --- | --- | --- |
| Pixel with AICore | Gemini Nano + rules | Gemini 3.8 Flash on placeholders |
| Entry-level, no NPU | Deterministic local filter | Gemini 3.8 Flash on placeholders |
| No signal | Nano or rules | Queued in Firestore cache, drains on reconnect |

## What a judge can check

| Claim | How to verify | Where |
| --- | --- | --- |
| Raw identifiers never reach the model | Scan tab shows the exact outbound JSON and counts raw values in it | `components/vault/scanner.tsx` |
| The server double-checks | Payloads with a raw SSN, email or 16-digit ID get `422` before Gemini is called | `lib/vault/server-guards.ts` |
| Hybrid routing is real | `ONLY_ON_DEVICE` for raw text (throws when Nano is missing, then rules), cloud only for sanitized text, re-scrubbed right before sending | `android/.../HybridPrivacyRouter.kt` |
| Storage cannot hold raw text | Per-user rules reject raw-text shadow fields | `firestore.rules`, `security_spec.md` |
| Works offline | Toggle *Simulate offline*, scan, reconnect | `android/.../NetworkMonitor.kt` |

On the three sample documents the engine catches **20 of 20** identifiers.

## How we built it

- **Android:** Kotlin, Jetpack Compose, Firebase AI Logic hybrid inference (`firebase-ai` 18.1.0 + `firebase-ai-ondevice` 16.0.0-beta07: Gemini Nano on device, `gemini-3.8-flash` in the cloud), ML Kit text recognition, Firestore with a 100 MB persistent cache, App Check (Play Integrity / debug provider).
- **Web vault:** Next.js 16, the AI SDK through Vercel AI Gateway (`google/gemini-3.8-flash`), Tesseract.js, Recharts. The PII engine (`lib/vault/pii-engine.ts`) mirrors `LocalPiiSanitizer.kt`.

## Challenges

- **Regexes that cross lines leak.** An early address rule used `\s`, matched across a line break and swallowed `$335.00` into `[REDACTED_ADDRESS]`. A name rule swallowed the next line's `Patient ID` label. Both now use horizontal whitespace only, in TypeScript and Kotlin.
- **`PREFER_ON_DEVICE` falls back to the cloud by itself.** That is right for most apps and wrong for raw documents, so the raw-text step uses `ONLY_ON_DEVICE` and handles the exception locally.
- **Model naming drift.** The cloud route called a different model than the app and docs described; everything now uses `gemini-3.8-flash`.

## What's next

On-device name detection for unlabeled names in the browser via the Prompt API, a per-document redaction audit export, and multi-page PDF scanning.

---

## Running it

**Web:** `pnpm install && pnpm dev`, then open `http://localhost:3000`. Cloud insight uses Vercel AI Gateway, which authenticates automatically on Vercel deployments. The vault keeps documents in memory only; a refresh resets the demo.

**Android:** open `android/` in Android Studio (JDK 17), add `google-services.json` to `android/app/` (git-ignored), enable Firebase AI Logic, Firestore, Auth and App Check, and register the debug token printed in Logcat.

All people and companies in the sample documents are fictional.

Gemini Nano runs through the ML Kit Prompt API and only works on supported devices (see the ML Kit documentation). The app checks the on-device model on launch and downloads it if needed; on other devices it keeps working with the deterministic local filter.

## License

Apache License 2.0. See [LICENSE](LICENSE).
