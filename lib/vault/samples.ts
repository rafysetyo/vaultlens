import type { VaultDocument, VaultFolder } from './types';

export const SAMPLE_CONTRACT_TEXT = `FREELANCE SOFTWARE ARCHITECTURE AGREEMENT
Effective Date: November 15, 2026

This Agreement is made by and between Elena Rostova (Client), residing at 742 Evergreen Terrace, Suite 4B, Seattle, WA 98101, email: elena.rostova@northstarventures.io, phone: (206) 555-0148, and Marcus Vance (Consultant), SSN: 123-45-6789, phone: (415) 555-0192.

1. Scope & Deliverables: Consultant shall architect and deliver the encrypted telemetry ingestion module by December 20, 2026.
2. Compensation: Client agrees to pay a fixed milestone fee of $18,500.00 USD, payable Net-60 after final acceptance. Late invoices accrue 1.5% monthly interest.
3. Indemnification & Liability: Consultant agrees to unlimited personal indemnification for third-party IP claims regardless of contract fee cap.
4. Auto-Renewal & Termination: Agreement automatically renews for successive 12-month retainers at $4,200.00/month unless written notice is delivered 45 days prior to expiration.`;

export const SAMPLE_INVOICE_TEXT = `OAKWOOD MEDICAL CLINIC
145 Riverpoint Drive, Portland, OR 97201
Phone: (503) 555-0199 | Billing Email: billing@oakwoodmed.org

LABORATORY INVOICE
Invoice Date: October 26, 2026
Invoice #: INV-23-45812
Patient: Sarah J. Miller
Patient ID: P993041
DOB: 04/12/1988

Services Rendered:
- Comprehensive Metabolic Panel (CPT 80053): $75.00
- Lipid Panel (CPT 80061): $90.00
- Complete Blood Count (CPT 85025): $60.00
- Hemoglobin A1c (CPT 83036): $85.00
- Venipuncture Blood Draw (CPT 36415): $25.00

Subtotal: $335.00
Insurance Pending Adjustment: -$180.00
Total Amount Due: $155.00
Payment Due Date: November 26, 2026
Unpaid balances after 30 days are forwarded to collections with a $35.00 administrative surcharge.`;

export const SAMPLE_ID_TEXT = `GOVERNMENT IDENTITY & TRAVEL RECORD
Full Name: Daniel K. Bennett
National ID: 4920881439021184
Passport: X88492014
DOB: 04/15/1992
Address: 218 Harborview Road, San Diego, CA 92101
Phone: (619) 555-0137
Email: d.bennett@pacificmail.net
Issue Date: January 10, 2024
Expiration Date: January 10, 2029
Note: Required for international wire verification and notary attestation prior to December 5, 2026.`;

export const SAMPLE_FOLDERS: VaultFolder[] = [
  {
    id: 'folder_client_contracts',
    ownerId: 'local_vault',
    name: 'Client Contracts',
    color: 'indigo',
    createdAtIso: '2026-11-18T09:00:00.000Z',
    updatedAtIso: '2026-11-18T09:00:00.000Z',
  },
  {
    id: 'folder_medical_billing',
    ownerId: 'local_vault',
    name: 'Medical & Billing',
    color: 'emerald',
    createdAtIso: '2026-11-19T10:00:00.000Z',
    updatedAtIso: '2026-11-19T10:00:00.000Z',
  },
];

export const SAMPLE_DOCUMENTS: VaultDocument[] = [
  {
    id: 'sample_contract_01',
    ownerId: 'local_vault',
    title: 'Freelance Software Architecture Agreement',
    category: 'Contract',
    folderId: 'folder_client_contracts',
    sourceType: 'sample',
    localOriginalText: SAMPLE_CONTRACT_TEXT,
    sanitizedText: `FREELANCE SOFTWARE ARCHITECTURE AGREEMENT
Effective Date: November 15, 2026

This Agreement is made by and between [REDACTED_NAME] (Client), residing at [REDACTED_ADDRESS], email: [REDACTED_EMAIL], phone: [REDACTED_PHONE], and [REDACTED_NAME] (Consultant), SSN: [REDACTED_SSN], phone: [REDACTED_PHONE].

1. Scope & Deliverables: Consultant shall architect and deliver the encrypted telemetry ingestion module by December 20, 2026.
2. Compensation: Client agrees to pay a fixed milestone fee of $18,500.00 USD, payable Net-60 after final acceptance. Late invoices accrue 1.5% monthly interest.
3. Indemnification & Liability: Consultant agrees to unlimited personal indemnification for third-party IP claims regardless of contract fee cap.
4. Auto-Renewal & Termination: Agreement automatically renews for successive 12-month retainers at $4,200.00/month unless written notice is delivered 45 days prior to expiration.`,
    piiRedactedCount: 7,
    redactionLog: [
      { entityType: 'NAME', placeholder: '[REDACTED_NAME]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'ADDRESS', placeholder: '[REDACTED_ADDRESS]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'EMAIL', placeholder: '[REDACTED_EMAIL]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'PHONE', placeholder: '[REDACTED_PHONE]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'NAME', placeholder: '[REDACTED_NAME]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'SSN', placeholder: '[REDACTED_SSN]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'PHONE', placeholder: '[REDACTED_PHONE]', engine: 'Gemini Nano Shield (On-Device)' },
    ],
    inferenceRoute: 'on_device_nano',
    analysisStatus: 'analyzed',
    documentSummary:
      'Fixed-milestone consulting contract between [REDACTED_NAME] (Client) and [REDACTED_NAME] (Consultant) for delivering an encrypted telemetry ingestion module. All personal identifiers, physical address, SSN, and contact numbers were stripped locally before cloud risk analysis.',
    keyDatesAndDeadlines: [
      'Effective Date: November 15, 2026',
      'Deliverable Milestone Deadline: December 20, 2026',
      'Payment Term: Net-60 following final acceptance',
      'Non-Renewal Notice Window: 45 days prior to expiration',
    ],
    financialAmounts: [
      '$18,500.00 USD fixed milestone fee',
      '$4,200.00/month automatic 12-month retainer renewal',
      '1.5% monthly late payment interest',
    ],
    redFlagsAndRisks: [
      'Unlimited personal indemnification clause without a liability cap tied to contract fees.',
      'Automatic 12-month retainer renewal ($4,200/mo) triggered if 45-day opt-out notice is missed.',
      'Extended Net-60 payment window delays cash flow after deliverable acceptance.',
    ],
    tags: ['Liability Risk', 'Auto-Renewal', 'Payment Terms', 'Tax / SSN Scrubbed'],
    createdAtIso: '2026-11-22T09:30:00.000Z',
    updatedAtIso: '2026-11-22T09:30:00.000Z',
    syncedToCloud: true,
  },
  {
    id: 'sample_invoice_02',
    ownerId: 'local_vault',
    title: 'Oakwood Medical Lab Invoice #INV-23-45812',
    category: 'Invoice',
    folderId: 'folder_medical_billing',
    sourceType: 'sample',
    localOriginalText: SAMPLE_INVOICE_TEXT,
    sanitizedText: `OAKWOOD MEDICAL CLINIC
[REDACTED_ADDRESS]
Phone: [REDACTED_PHONE] | Billing Email: [REDACTED_EMAIL]

LABORATORY INVOICE
Invoice Date: October 26, 2026
Invoice #: INV-23-45812
Patient: [REDACTED_NAME]
Patient ID: [REDACTED_ID]
DOB: [REDACTED_DOB]

Services Rendered:
- Comprehensive Metabolic Panel (CPT 80053): $75.00
- Lipid Panel (CPT 80061): $90.00
- Complete Blood Count (CPT 85025): $60.00
- Hemoglobin A1c (CPT 83036): $85.00
- Venipuncture Blood Draw (CPT 36415): $25.00

Subtotal: $335.00
Insurance Pending Adjustment: -$180.00
Total Amount Due: $155.00
Payment Due Date: November 26, 2026
Unpaid balances after 30 days are forwarded to collections with a $35.00 administrative surcharge.`,
    piiRedactedCount: 6,
    redactionLog: [
      { entityType: 'ADDRESS', placeholder: '[REDACTED_ADDRESS]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'PHONE', placeholder: '[REDACTED_PHONE]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'EMAIL', placeholder: '[REDACTED_EMAIL]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'NAME', placeholder: '[REDACTED_NAME]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'NATIONAL_ID', placeholder: '[REDACTED_ID]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'DOB', placeholder: '[REDACTED_DOB]', engine: 'Gemini Nano Shield (On-Device)' },
    ],
    inferenceRoute: 'cloud_fallback_flash',
    analysisStatus: 'analyzed',
    documentSummary:
      'Diagnostic laboratory invoice (#INV-23-45812) from Oakwood Medical Clinic covering five blood panel CPT codes for [REDACTED_NAME]. Patient name, ID, date of birth, clinic address, and contact lines were redacted locally prior to Gemini Flash extraction.',
    keyDatesAndDeadlines: [
      'Invoice Date: October 26, 2026',
      'Payment Due Date: November 26, 2026',
      'Collections Escalation Window: 30 days past due',
    ],
    financialAmounts: [
      'Gross Laboratory Charges: $335.00',
      'Pending Insurance Adjustment: -$180.00',
      'Net Patient Balance Due: $155.00',
      'Late Collections Surcharge: $35.00',
    ],
    redFlagsAndRisks: [
      'Insurance adjustment (-$180.00) is marked pending; confirm insurer EOB settles before November 26, 2026.',
      'Strict 30-day collections trigger adds a $35.00 administrative surcharge.',
    ],
    tags: ['Healthcare Billing', 'Late Penalty', 'Gov ID Scrubbed'],
    createdAtIso: '2026-11-20T14:15:00.000Z',
    updatedAtIso: '2026-11-20T14:15:00.000Z',
    syncedToCloud: true,
  },
  {
    id: 'sample_legal_03',
    ownerId: 'local_vault',
    title: 'Residential Lease Security Addendum',
    category: 'Legal',
    sourceType: 'sample',
    sanitizedText: `RESIDENTIAL LEASE ADDENDUM
Property: [REDACTED_ADDRESS]
Tenant: [REDACTED_NAME] | Contact: [REDACTED_EMAIL] | Phone: [REDACTED_PHONE]

Security Deposit of $3,400.00 held in non-interest escrow account. Tenant forfeits $450.00 mandatory deep-cleaning fee regardless of move-out condition. Lease converts to 6-month fixed renewal unless terminated 60 days prior to March 31, 2027.`,
    piiRedactedCount: 4,
    redactionLog: [
      { entityType: 'ADDRESS', placeholder: '[REDACTED_ADDRESS]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'NAME', placeholder: '[REDACTED_NAME]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'EMAIL', placeholder: '[REDACTED_EMAIL]', engine: 'Gemini Nano Shield (On-Device)' },
      { entityType: 'PHONE', placeholder: '[REDACTED_PHONE]', engine: 'Gemini Nano Shield (On-Device)' },
    ],
    inferenceRoute: 'on_device_nano',
    analysisStatus: 'analyzed',
    documentSummary:
      'Lease addendum governing escrow deposit handling and renewal notice terms for [REDACTED_NAME] at [REDACTED_ADDRESS]. All personal identifiers were redacted on-device.',
    keyDatesAndDeadlines: [
      'Lease Expiration Date: March 31, 2027',
      'Termination Notice Deadline: 60 days prior (January 30, 2027)',
    ],
    financialAmounts: [
      '$3,400.00 security deposit',
      '$450.00 non-refundable deep-cleaning deduction',
    ],
    redFlagsAndRisks: [
      'Non-refundable $450.00 cleaning deduction regardless of condition may conflict with local tenant deposit statutes.',
      '60-day notice window triggers automatic 6-month lock-in rather than month-to-month conversion.',
    ],
    tags: ['Lease & Escrow', 'Auto-Renewal', 'Notice Deadline'],
    createdAtIso: '2026-11-18T11:05:00.000Z',
    updatedAtIso: '2026-11-18T11:05:00.000Z',
    syncedToCloud: true,
  },
];
