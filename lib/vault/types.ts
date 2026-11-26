export type DocumentCategory = 'Contract' | 'ID/Passport' | 'Invoice' | 'Legal';

export type SourceType = 'ocr_scan' | 'pasted_text' | 'sample';

export type InferenceRoute = 'on_device_nano' | 'cloud_fallback_flash';

export type AnalysisStatus = 'analyzed' | 'queued_offline' | 'local_only';

export type DeviceCapability = 'gemini_nano_npu' | 'cloud_fallback';

export interface RedactionLogEntry {
  entityType: string;
  placeholder: string;
  engine: string;
}

export interface RedactionSpan {
  start: number;
  end: number;
  originalValue: string;
  entityType: string;
  placeholder: string;
}

export interface LocalRedactionResult {
  originalText: string;
  sanitizedText: string;
  spans: RedactionSpan[];
  redactionLog: RedactionLogEntry[];
  engineUsed: string;
  latencyMs: number;
}

export type FolderColor = 'indigo' | 'emerald' | 'amber' | 'rose' | 'slate';

export interface VaultFolder {
  id: string;
  ownerId: string;
  name: string;
  color: FolderColor;
  createdAtIso: string;
  updatedAtIso: string;
}

export interface VaultDocument {
  id: string;
  ownerId: string;
  title: string;
  category: DocumentCategory;
  folderId?: string;
  sourceType: SourceType;
  sanitizedText: string;
  /** Kept strictly in browser local memory/storage — never written to Firestore. */
  localOriginalText?: string;
  piiRedactedCount: number;
  redactionLog: RedactionLogEntry[];
  inferenceRoute: InferenceRoute;
  analysisStatus: AnalysisStatus;
  documentSummary: string;
  keyDatesAndDeadlines: string[];
  financialAmounts: string[];
  redFlagsAndRisks: string[];
  tags: string[];
  createdAtIso: string;
  updatedAtIso: string;
  syncedToCloud: boolean;
  isPendingWrite?: boolean;
}
