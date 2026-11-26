# VaultLens Firestore Security Specification

## 1. Data Invariants
1. **Zero-Trust Ownership**: A `VaultDocument` at `/users/{userId}/documents/{documentId}` and a `VaultFolder` at `/users/{userId}/folders/{folderId}` can only be read, created, updated, or deleted by the verified authenticated user whose `request.auth.uid == userId` and `ownerId == request.auth.uid`.
2. **No Raw PII Persistence**: `VaultDocument` records only store `sanitizedText` and anonymized `redactionLog` metadata (`type`, `placeholder`, `engine`) — raw unredacted values are forbidden by schema keys (`hasOnly`).
3. **Immutable Identity & Creation Timestamps**: `ownerId`, `id`, and `createdAt` cannot be modified after creation (`incoming().ownerId == existing().ownerId`, `incoming().id == existing().id`, `incoming().createdAt == existing().createdAt`).
4. **Strict Server Time**: `createdAt` and `updatedAt` must equal `request.time`.
5. **Bounded Payloads**: Every string and array enforces strict size upper bounds (`tags <= 10`, folder `name <= 60`, `folderId <= 128`) to prevent Denial of Wallet attacks.

## 2. Adversarial "Dirty Dozen" Payloads (Expected: PERMISSION_DENIED)
1. Unauthenticated read/write to `/users/userA/documents/doc1`
2. Cross-user read (`userB` reading `/users/userA/documents/doc1`)
3. Cross-user list query (`userB` listing `/users/userA/documents`)
4. Owner spoofing on create (`userA` creating `/users/userA/documents/doc1` with `ownerId: "userB"`)
5. Shadow field injection (`userA` adding `rawOriginalText: "John SSN 123-45-6789"` or `isAdmin: true`)
6. Unverified email write (`userA` with `email_verified: false`)
7. Document ID poisoning (`documentId` containing spaces, `$` or >128 chars)
8. Immutable field tampering on update (modifying `createdAt` or `ownerId`)
9. Forged client timestamp (`createdAt` != `request.time`)
10. Invalid enum category (`category: "Secret"`) or invalid `analysisStatus`
11. Unbounded array injection (`redactionLog` with > 50 entries or `keyDatesAndDeadlines` > 25 entries)
12. Oversized string payload (`sanitizedText` > 50,000 chars or `title` > 200 chars)
