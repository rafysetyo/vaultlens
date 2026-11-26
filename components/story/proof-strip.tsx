export function ProofStrip({ caught, total }: { caught: number; total: number }) {
  const items = [
    `${caught} of ${total} identifiers caught across 3 sample documents`,
    '0 raw values in the outbound payload',
    'Gemini Nano on device',
    'Gemini 3.8 Flash on placeholders',
    'Server refuses payloads with a raw SSN, email or ID',
    'Firestore rules refuse raw-text fields',
    'Offline queue drains on reconnect',
    'App Check on every cloud call',
  ]
  const loop = [...items, ...items]

  return (
    <div className="border-y bg-background py-3" aria-label="Verifiable claims">
      <ul className="sr-only">
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
      <div
        className="overflow-hidden [mask-image:linear-gradient(to_right,transparent,#000_8%,#000_92%,transparent)]"
        aria-hidden="true"
      >
        <div className="animate-marquee flex w-max gap-10 pr-10 font-mono text-xs text-muted-foreground hover:[animation-play-state:paused]">
          {loop.map((item, index) => (
            <span key={index} className="flex items-center gap-10 whitespace-nowrap">
              {item}
              <span className="size-1 rounded-full bg-primary" />
            </span>
          ))}
        </div>
      </div>
    </div>
  )
}
