import { Download, FileCode2 } from 'lucide-react'

const FILES = [
  {
    path: 'data/hybrid/HybridPrivacyRouter.kt',
    role: 'Firebase AI Logic. InferenceMode.ONLY_ON_DEVICE asks Gemini Nano for names and never falls back to the cloud for raw text; gemini-3.8-flash only sees sanitized text.',
  },
  {
    path: 'data/hybrid/LocalPiiSanitizer.kt',
    role: 'Deterministic rules for SSN, IDs, phones, emails, addresses, DOB and labeled names. Runs with or without an NPU.',
  },
  {
    path: 'data/ocr/LocalOcrProcessor.kt',
    role: 'ML Kit text recognition. The camera frame is read on device; pixels never upload.',
  },
  {
    path: 'data/network/NetworkMonitor.kt',
    role: 'ConnectivityManager callback. Drains QUEUED_OFFLINE documents the moment a network returns.',
  },
  {
    path: 'data/repository/VaultRepository.kt',
    role: 'Firestore with a 100 MB persistent cache. Only sanitized fields are written.',
  },
  {
    path: 'VaultLensApplication.kt',
    role: 'App Check: Play Integrity in release, debug provider for judging builds.',
  },
]

const ROUTER_EXCERPT = `// ONLY_ON_DEVICE throws when Nano is missing, so raw text can never reach the cloud.
private val nanoNameModel = ai.generativeModel(
    modelName = CLOUD_MODEL,
    onDeviceConfig = OnDeviceConfig(mode = InferenceMode.ONLY_ON_DEVICE, maxOutputTokens = 256)
)

suspend fun detectAndRedactLocally(rawText: String): RedactionResult {
    val names = detectNamesWithNano(rawText)   // null when Nano could not run
    return if (names != null)
        LocalPiiSanitizer.redactDeterministic(rawText, names, "Gemini Nano (On-Device)")
    else
        LocalPiiSanitizer.redactDeterministic(rawText, engineLabel = "On-device rules (Nano unavailable)")
}`

export function AndroidPanel() {
  return (
    <div className="flex flex-col gap-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="flex flex-col gap-1.5">
          <p className="text-sm text-muted-foreground">Kotlin · Jetpack Compose · Firebase AI Logic</p>
          <h1 className="text-balance text-4xl font-medium tracking-tight md:text-5xl">
            The phone <span className="text-muted-foreground">is the product</span>
          </h1>
          <p className="max-w-2xl text-pretty text-sm leading-relaxed text-muted-foreground">
            This web vault mirrors the native app so judges can try it in a browser. The Android build is where Gemini Nano runs
            on the NPU, ML Kit reads the camera, and Firestore keeps an offline queue.
          </p>
        </div>
        <a
          href="/vaultlens-android.zip"
          download
          className="inline-flex h-10 items-center gap-2 rounded-xl bg-ink px-4 text-sm font-medium text-primary-foreground transition-opacity hover:opacity-90"
        >
          <Download className="size-4" aria-hidden="true" />
          Download Android source
        </a>
      </div>

      <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
        <section aria-labelledby="files-heading" className="rounded-2xl border bg-card">
          <h2 id="files-heading" className="border-b px-5 py-4 text-sm font-medium">
            Where to look
          </h2>
          <ul>
            {FILES.map((file) => (
              <li key={file.path} className="flex gap-3 border-b px-5 py-4 last:border-b-0">
                <FileCode2 className="mt-0.5 size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
                <span className="flex flex-col gap-1">
                  <span className="font-mono text-xs text-foreground">{file.path}</span>
                  <span className="text-sm leading-relaxed text-muted-foreground">{file.role}</span>
                </span>
              </li>
            ))}
          </ul>
        </section>

        <section aria-labelledby="router-heading" className="flex flex-col rounded-2xl border bg-card">
          <h2 id="router-heading" className="border-b px-5 py-4 text-sm font-medium">
            HybridPrivacyRouter.kt <span className="font-normal text-muted-foreground">(excerpt)</span>
          </h2>
          <pre className="flex-1 overflow-x-auto p-5 font-mono text-[12px] leading-6 text-foreground">
            <code>{ROUTER_EXCERPT}</code>
          </pre>
          <ol className="flex flex-col gap-2 border-t px-5 py-4 text-sm text-muted-foreground">
            <li>
              <span className="font-mono text-foreground">1.</span> Open <span className="font-mono text-foreground">android/</span> in Android Studio (JDK 17).
            </li>
            <li>
              <span className="font-mono text-foreground">2.</span> Add <span className="font-mono text-foreground">google-services.json</span> to <span className="font-mono text-foreground">android/app/</span> (it is git-ignored).
            </li>
            <li>
              <span className="font-mono text-foreground">3.</span> Enable Firebase AI Logic, Firestore, Auth and App Check; register the debug token from Logcat.
            </li>
          </ol>
        </section>
      </div>
    </div>
  )
}
