const ROUTES = [
  {
    device: 'Pixel with AICore',
    finds: 'Gemini Nano + rules',
    reasons: 'Gemini 3.8 Flash on placeholders',
    note: 'Best recall on names that carry no label.',
  },
  {
    device: 'Entry-level phone, no NPU',
    finds: 'Deterministic local filter',
    reasons: 'Gemini 3.8 Flash on placeholders',
    note: 'Same privacy line. No phone is left out.',
  },
  {
    device: 'No signal',
    finds: 'Nano or rules, as above',
    reasons: 'Queued in Firestore cache',
    note: 'Drains automatically when the network returns.',
  },
]

export function Routing() {
  return (
    <section className="grid gap-10 border-b px-5 py-16 lg:grid-cols-[14rem_minmax(0,1fr)] lg:px-8">
      <p className="text-sm text-muted-foreground">Hybrid, by device</p>
      <div className="flex flex-col gap-6">
        <h2 className="max-w-3xl text-balance text-3xl font-medium tracking-tight">
          The privacy line never moves. <span className="text-muted-foreground">Only who draws it does.</span>
        </h2>
        <div className="overflow-x-auto rounded-2xl border bg-card">
          <table className="w-full min-w-[640px] text-sm">
            <thead>
              <tr className="border-b text-left text-xs text-muted-foreground">
                <th scope="col" className="px-5 py-3.5 font-medium">
                  Device
                </th>
                <th scope="col" className="px-3 py-3.5 font-medium">
                  Finds identifiers
                </th>
                <th scope="col" className="px-3 py-3.5 font-medium">
                  Reasons over clauses
                </th>
                <th scope="col" className="px-5 py-3.5 font-medium">
                  Why it matters
                </th>
              </tr>
            </thead>
            <tbody>
              {ROUTES.map((route) => (
                <tr key={route.device} className="border-b last:border-b-0">
                  <th scope="row" className="px-5 py-4 text-left font-medium">
                    {route.device}
                  </th>
                  <td className="px-3 py-4">
                    <span className="rounded-md bg-muted px-2 py-0.5 font-mono text-xs">{route.finds}</span>
                  </td>
                  <td className="px-3 py-4">
                    <span className="rounded-md bg-accent px-2 py-0.5 font-mono text-xs text-accent-foreground">{route.reasons}</span>
                  </td>
                  <td className="px-5 py-4 text-muted-foreground">{route.note}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  )
}
