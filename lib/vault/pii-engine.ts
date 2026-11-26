import type {
  DeviceCapability,
  DocumentCategory,
  LocalRedactionResult,
  RedactionLogEntry,
  RedactionSpan,
} from './types';

interface PatternRule {
  entityType: string;
  placeholder: string;
  regex: RegExp;
  groupIndex?: number;
}

const PII_RULES: PatternRule[] = [
  {
    entityType: 'SSN',
    placeholder: '[REDACTED_SSN]',
    regex: /\b\d{3}-\d{2}-\d{4}\b/g,
  },
  {
    entityType: 'NATIONAL_ID',
    placeholder: '[REDACTED_ID]',
    regex: /\b(?:National ID|Tax ID|Passport|ID\s*#|Patient ID|Driver'?s License)\s*[:#]?\s*([A-Z0-9-]{6,20})\b/gi,
    groupIndex: 1,
  },
  {
    entityType: 'GOV_ID_16_DIGIT',
    placeholder: '[REDACTED_ID]',
    regex: /\b\d{16}\b/g,
  },
  {
    entityType: 'EMAIL',
    placeholder: '[REDACTED_EMAIL]',
    regex: /\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b/g,
  },
  {
    entityType: 'PHONE',
    placeholder: '[REDACTED_PHONE]',
    regex: /(?:\+?\d{1,3}[\s.-]?)?\(?\d{3}\)?[\s.-]?\d{3}[\s.-]?\d{4}\b/g,
  },
  {
    entityType: 'ACCOUNT_CARD',
    placeholder: '[REDACTED_ACCOUNT]',
    regex: /\b(?:\d{4}[-\s]?){3}\d{4}\b|\b(?:Account|Routing|IBAN)\s*[:#]?\s*([A-Z0-9-]{8,24})\b/gi,
  },
  {
    entityType: 'DOB',
    placeholder: '[REDACTED_DOB]',
    regex: /\b(?:DOB|Date of Birth|Birth Date)\s*[:#]?\s*(\d{1,2}[/-]\d{1,2}[/-]\d{2,4}|[A-Za-z]+\s+\d{1,2},\s*\d{4})/gi,
    groupIndex: 1,
  },
  {
    entityType: 'ADDRESS',
    placeholder: '[REDACTED_ADDRESS]',
    // Horizontal whitespace only: `\s` let matches cross line breaks and swallow amounts on the next line.
    regex: /\b\d{1,5}[ \t]+(?:[A-Za-z0-9.'-]+[ \t]+){0,4}(?:Drive|Dr|Street|St|Avenue|Ave|Road|Rd|Boulevard|Blvd|Lane|Ln|Way|Court|Ct|Parkway|Pkwy|Terrace|Ter|Place|Pl|Circle|Cir|Plaza|Highway|Hwy)\b\.?(?:,[ \t]*(?:Suite|Ste|Apt|Unit|#)[ \t]*[A-Za-z0-9-]+)?(?:,[ \t]*[A-Za-z][A-Za-z \t]*,[ \t]*[A-Z]{2}[ \t]*\d{5}(?:-\d{4})?)?/gi,
  },
  {
    entityType: 'LABELED_NAME',
    placeholder: '[REDACTED_NAME]',
    regex: /\b(?:Patient|Client|Consultant|Contractor|Tenant|Landlord|Employee|Subscriber|Signed by|Authorized Representative|Attention|Attn|Full Name|Name)[ \t]*:[ \t]*([A-Z][a-zA-Z.'-]+(?:[ \t]+[A-Z]\.?)?(?:[ \t]+[A-Z][a-zA-Z.'-]+){1,2})\b/g,
    groupIndex: 1,
  },
  {
    entityType: 'PARTY_NAME',
    placeholder: '[REDACTED_NAME]',
    regex: /\b(?:between|by and between|and)[ \t]+([A-Z][a-z]+(?:[ \t]+[A-Z]\.)?[ \t]+[A-Z][a-z]+)(?=[ \t]*\(|[ \t]*,|[ \t]+and\b)/g,
    groupIndex: 1,
  },
];

interface BrowserLanguageModel {
  create?: (opts?: { systemPrompt?: string }) => Promise<{
    prompt: (input: string) => Promise<string>;
    destroy?: () => void;
  }>;
  availability?: () => Promise<string>;
}

export async function checkBrowserGeminiNanoSupport(): Promise<boolean> {
  try {
    const win = window as unknown as {
      LanguageModel?: BrowserLanguageModel;
      ai?: { languageModel?: BrowserLanguageModel };
    };
    const model = win.LanguageModel ?? win.ai?.languageModel;
    if (!model?.availability) return false;
    const status = await model.availability();
    return status === 'available' || status === 'readily';
  } catch {
    return false;
  }
}

async function tryNanoExtraNames(text: string): Promise<string[]> {
  try {
    const win = window as unknown as {
      LanguageModel?: BrowserLanguageModel;
      ai?: { languageModel?: BrowserLanguageModel };
    };
    const model = win.LanguageModel ?? win.ai?.languageModel;
    if (!model?.create || !model?.availability) return [];
    const status = await model.availability();
    if (status !== 'available' && status !== 'readily') return [];

    const session = await model.create({
      systemPrompt:
        'Extract only full human person names from the text as a JSON array of strings. Return [] if none.',
    });
    const raw = await session.prompt(text.slice(0, 4000));
    session.destroy?.();
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed)
      ? parsed.filter((item): item is string => typeof item === 'string' && item.trim().length > 3)
      : [];
  } catch {
    return [];
  }
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export async function redactPiiLocally(
  inputText: string,
  deviceCapability: DeviceCapability,
  customTerms: string[] = []
): Promise<LocalRedactionResult> {
  const startedAt = performance.now();
  const candidateSpans: RedactionSpan[] = [];

  const cleanCustomTerms = Array.from(
    new Set(customTerms.map((term) => term.trim()).filter((term) => term.length >= 2))
  );
  for (const term of cleanCustomTerms) {
    const pattern = new RegExp(`\\b${escapeRegExp(term)}\\b`, 'gi');
    let match: RegExpExecArray | null;
    while ((match = pattern.exec(inputText)) !== null) {
      candidateSpans.push({
        start: match.index,
        end: match.index + match[0].length,
        originalValue: match[0],
        entityType: 'CUSTOM_TERM',
        placeholder: '[REDACTED_CUSTOM]',
      });
    }
  }

  for (const rule of PII_RULES) {
    rule.regex.lastIndex = 0;
    let match: RegExpExecArray | null;
    while ((match = rule.regex.exec(inputText)) !== null) {
      let start = match.index;
      let originalValue = match[0];
      if (rule.groupIndex && match[rule.groupIndex]) {
        const sub = match[rule.groupIndex];
        const offset = match[0].indexOf(sub);
        if (offset >= 0) {
          start = match.index + offset;
          originalValue = sub;
        }
      }
      const end = start + originalValue.length;
      if (originalValue.trim().length > 0) {
        candidateSpans.push({
          start,
          end,
          originalValue,
          entityType: rule.entityType === 'LABELED_NAME' || rule.entityType === 'PARTY_NAME' ? 'NAME' : rule.entityType,
          placeholder: rule.placeholder,
        });
      }
    }
  }

  let usedBrowserNano = false;
  if (deviceCapability === 'gemini_nano_npu') {
    const extraNames = await tryNanoExtraNames(inputText);
    if (extraNames.length > 0) {
      usedBrowserNano = true;
      for (const name of extraNames) {
        let searchFrom = 0;
        while (searchFrom < inputText.length) {
          const idx = inputText.indexOf(name, searchFrom);
          if (idx === -1) break;
          candidateSpans.push({
            start: idx,
            end: idx + name.length,
            originalValue: name,
            entityType: 'NAME',
            placeholder: '[REDACTED_NAME]',
          });
          searchFrom = idx + name.length;
        }
      }
    }
  }

  // Sort by start asc, then longer span first, and deduplicate overlapping spans
  candidateSpans.sort((a, b) => a.start - b.start || b.end - a.end);
  const nonOverlapping: RedactionSpan[] = [];
  for (const span of candidateSpans) {
    const last = nonOverlapping[nonOverlapping.length - 1];
    if (!last || span.start >= last.end) {
      nonOverlapping.push(span);
    }
  }

  let sanitizedText = '';
  let cursor = 0;
  for (const span of nonOverlapping) {
    sanitizedText += inputText.slice(cursor, span.start) + span.placeholder;
    cursor = span.end;
  }
  sanitizedText += inputText.slice(cursor);

  const engineLabel =
    usedBrowserNano
      ? 'Gemini Nano (On-Device)'
      : deviceCapability === 'gemini_nano_npu'
      ? 'Gemini Nano Shield (On-Device)'
      : 'Local Privacy Filter (Pre-Cloud)';

  const redactionLog: RedactionLogEntry[] = nonOverlapping.slice(0, 50).map((span) => ({
    entityType: span.entityType,
    placeholder: span.placeholder,
    engine: engineLabel,
  }));

  const latencyMs = Math.max(4, Math.round(performance.now() - startedAt));

  return {
    originalText: inputText,
    sanitizedText,
    spans: nonOverlapping,
    redactionLog,
    engineUsed: engineLabel,
    latencyMs,
  };
}

const CONTENT_TAG_RULES: Array<{ regex: RegExp; tag: string }> = [
  { regex: /\b(?:indemnif|unlimited\s+(?:personal\s+)?liability|hold\s+harmless)\b/i, tag: 'Liability Risk' },
  { regex: /\b(?:auto-?renew|automatically\s+renews|successive\s+\d+-month)\b/i, tag: 'Auto-Renewal' },
  { regex: /\b(?:net-?30|net-?45|net-?60|net-?90|milestone\s+fee|compensation)\b/i, tag: 'Payment Terms' },
  { regex: /\b(?:confidential|non-disclosure|nda|proprietary)\b/i, tag: 'Confidentiality' },
  { regex: /\b(?:intellectual\s+property|copyright|work\s+for\s+hire)\b/i, tag: 'IP Rights' },
  { regex: /\b(?:medical|clinic|patient|laboratory|cpt\s*\d+|hemoglobin|lipid\s+panel|insurance)\b/i, tag: 'Healthcare Billing' },
  { regex: /\b(?:collections|surcharge|overdue|late\s+fee|interest)\b/i, tag: 'Late Penalty' },
  { regex: /\b(?:passport|national\s+id|tax\s+id|notary|driver'?s\s+license|expiration\s+date)\b/i, tag: 'Identity Verification' },
  { regex: /\b(?:lease|tenant|landlord|security\s+deposit|escrow|move-out)\b/i, tag: 'Lease & Escrow' },
  { regex: /\b(?:terminat|notice\s+window|prior\s+written\s+notice|\d+\s+days\s+prior)\b/i, tag: 'Notice Deadline' },
  { regex: /(?:\$|USD|EUR|GBP)\s*[\d,]+(?:\.\d{2})?/i, tag: 'Financial Value' },
];

export function suggestTagsLocally(
  sanitizedText: string,
  category: DocumentCategory,
  redactionLog: RedactionLogEntry[]
): string[] {
  const tags = new Set<string>();

  for (const rule of CONTENT_TAG_RULES) {
    if (rule.regex.test(sanitizedText)) {
      tags.add(rule.tag);
    }
  }

  const redactedTypes = new Set(redactionLog.map((entry) => entry.entityType));
  if (redactedTypes.has('SSN')) tags.add('Tax / SSN Scrubbed');
  if (redactedTypes.has('NATIONAL_ID') || redactedTypes.has('GOV_ID_16_DIGIT')) {
    tags.add('Gov ID Scrubbed');
  }
  if (redactedTypes.has('ACCOUNT_CARD')) tags.add('Financial Account');

  if (tags.size < 3) {
    if (category === 'Contract') tags.add('Service Agreement');
    if (category === 'Invoice') tags.add('Billing Statement');
    if (category === 'ID/Passport') tags.add('KYC Document');
    if (category === 'Legal') tags.add('Legal Addendum');
  }

  return Array.from(tags)
    .map((t) => t.slice(0, 40))
    .slice(0, 6);
}

