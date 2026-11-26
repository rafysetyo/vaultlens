'use client'

import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from 'recharts'
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from '@/components/ui/chart'

export type ChartPoint = { day: string; value: number }

export function OverviewChart({ data, label, suffix = '' }: { data: ChartPoint[]; label: string; suffix?: string }) {
  const config = { value: { label, color: 'var(--chart-1)' } } satisfies ChartConfig

  return (
    <ChartContainer config={config} className="aspect-auto h-64 w-full">
      <AreaChart data={data} margin={{ top: 8, right: 8, bottom: 0, left: 0 }}>
        <defs>
          <linearGradient id="fill-value" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="var(--color-value)" stopOpacity={0.16} />
            <stop offset="100%" stopColor="var(--color-value)" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid vertical={false} strokeDasharray="3 4" />
        <XAxis
          dataKey="day"
          tickLine={false}
          axisLine={false}
          tickMargin={10}
          interval={6}
          tick={{ fontFamily: 'var(--font-geist-mono)', fontSize: 11 }}
        />
        <YAxis
          tickLine={false}
          axisLine={false}
          width={40}
          tickCount={4}
          tickFormatter={(value: number) => `${value}${suffix}`}
          tick={{ fontFamily: 'var(--font-geist-mono)', fontSize: 11 }}
        />
        <ChartTooltip cursor={false} content={<ChartTooltipContent indicator="line" />} />
        <Area
          dataKey="value"
          type="monotone"
          stroke="var(--color-value)"
          strokeWidth={2}
          fill="url(#fill-value)"
          isAnimationActive={false}
        />
      </AreaChart>
    </ChartContainer>
  )
}
