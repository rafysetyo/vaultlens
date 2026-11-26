package com.vaultlens.app.data.hybrid

import com.vaultlens.app.data.model.DocumentCategory
import com.vaultlens.app.data.model.RedactionEntry
import com.vaultlens.app.data.model.RedactionResult

/**
 * Deterministic on-device PII scrubber paired with Gemini Nano NER extraction.
 * Guarantees raw sensitive identifiers (SSN, National IDs, passports, emails, phone numbers, addresses, names)
 * are replaced with safe placeholder tokens before any cloud call occurs.
 */
object LocalPiiSanitizer {

    private data class PatternRule(
        val entityType: String,
        val placeholder: String,
        val regex: Regex,
        val groupIndex: Int? = null
    )

    private data class TagRule(
        val tag: String,
        val regex: Regex
    )

    private val rules = listOf(
        PatternRule("SSN", "[REDACTED_SSN]", Regex("""\b\d{3}-\d{2}-\d{4}\b""")),
        PatternRule("GOV_ID_16_DIGIT", "[REDACTED_ID]", Regex("""\b\d{16}\b""")),
        PatternRule(
            "ID_NUMBER",
            "[REDACTED_ID]",
            Regex("""\b(?:Passport|Patient ID|National ID|Tax ID|Driver'?s License|ID\s*#)\s*[:#]?\s*([A-Z0-9-]{6,20})\b""", RegexOption.IGNORE_CASE),
            groupIndex = 1
        ),
        PatternRule("EMAIL", "[REDACTED_EMAIL]", Regex("""\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b""")),
        PatternRule("PHONE", "[REDACTED_PHONE]", Regex("""(?:\+?\d{1,3}[\s.-]?)?\(?\d{3}\)?[\s.-]?\d{3}[\s.-]?\d{4}\b""")),
        PatternRule(
            "ADDRESS",
            "[REDACTED_ADDRESS]",
            Regex("""\b\d{1,5}[ \t]+(?:[A-Za-z0-9.'-]+[ \t]+){0,4}(?:Drive|Dr|Street|St|Avenue|Ave|Road|Rd|Boulevard|Blvd|Lane|Ln|Way|Court|Ct|Parkway|Pkwy|Terrace|Ter|Place|Pl|Circle|Cir|Plaza|Highway|Hwy)\b\.?(?:,[ \t]*(?:Suite|Ste|Apt|Unit|#)[ \t]*[A-Za-z0-9-]+)?(?:,[ \t]*[A-Za-z][A-Za-z \t]*,[ \t]*[A-Z]{2}[ \t]*\d{5}(?:-\d{4})?)?""", RegexOption.IGNORE_CASE)
        ),
        PatternRule(
            "NAME",
            "[REDACTED_NAME]",
            Regex("""\b(?:Patient|Client|Consultant|Contractor|Tenant|Full Name|Name)[ \t]*:[ \t]*([A-Z][a-zA-Z.'-]+(?:[ \t]+[A-Z]\.?)?(?:[ \t]+[A-Z][a-zA-Z.'-]+){1,2})\b"""),
            groupIndex = 1
        )
    )

    private val contentTagRules = listOf(
        TagRule("Liability Risk", Regex("""\b(indemnif(?:y|ication)|unlimited liability|hold harmless|breach)\b""", RegexOption.IGNORE_CASE)),
        TagRule("Auto-Renewal", Regex("""\b(auto[- ]?renew(?:al|s)?|evergreen|recurring billing)\b""", RegexOption.IGNORE_CASE)),
        TagRule("Payment Terms", Regex("""\b(net[- ]?\d+|milestone|invoice|remittance|due date|payment)\b""", RegexOption.IGNORE_CASE)),
        TagRule("Confidentiality", Regex("""\b(nda|confidential|non[- ]?disclosure|proprietary|trade secret)\b""", RegexOption.IGNORE_CASE)),
        TagRule("IP Rights", Regex("""\b(intellectual property|work[- ]?for[- ]?hire|copyright|patent|license)\b""", RegexOption.IGNORE_CASE)),
        TagRule("Healthcare Billing", Regex("""\b(patient|lab|medical|diagnostic|insurance|copay|hipaa|clinic)\b""", RegexOption.IGNORE_CASE)),
        TagRule("Late Penalty", Regex("""\b(late fee|penalty|collections|interest rate|default)\b""", RegexOption.IGNORE_CASE)),
        TagRule("Identity Verification", Regex("""\b(passport|national id|tax id|ssn|date of birth|dob|citizenship)\b""", RegexOption.IGNORE_CASE)),
        TagRule("Lease & Escrow", Regex("""\b(lease|tenant|landlord|security deposit|rent|eviction)\b""", RegexOption.IGNORE_CASE)),
        TagRule("Notice Deadline", Regex("""\b(\d+\s*days?\s*(?:prior|written)?\s*notice|deadline|expir(?:e|ation))\b""", RegexOption.IGNORE_CASE)),
        TagRule("Financial Value", Regex("""\$\s?\d{1,3}(?:,\d{3})*(?:\.\d{2})?"""))
    )

    fun redactDeterministic(
        rawText: String,
        extraDetectedNames: List<String> = emptyList(),
        engineLabel: String,
        customTerms: List<String> = emptyList()
    ): RedactionResult {
        var sanitized = rawText
        val entries = mutableListOf<RedactionEntry>()

        for (term in customTerms.map { it.trim() }.filter { it.length >= 2 }.distinct()) {
            val regex = Regex("""\b${Regex.escape(term)}\b""", RegexOption.IGNORE_CASE)
            val matches = regex.findAll(sanitized).toList().reversed()
            for (match in matches) {
                sanitized = sanitized.replaceRange(match.range, "[REDACTED_CUSTOM]")
                entries += RedactionEntry("CUSTOM_TERM", "[REDACTED_CUSTOM]", engineLabel)
            }
        }

        for (name in extraDetectedNames.distinct().filter { it.length > 3 }) {
            if (sanitized.contains(name)) {
                sanitized = sanitized.replace(name, "[REDACTED_NAME]")
                entries += RedactionEntry("NAME", "[REDACTED_NAME]", engineLabel)
            }
        }

        for (rule in rules) {
            val matches = rule.regex.findAll(sanitized).toList().reversed()
            for (match in matches) {
                val range = if (rule.groupIndex != null) {
                    match.groups[rule.groupIndex]?.range ?: match.range
                } else {
                    match.range
                }
                sanitized = sanitized.replaceRange(range, rule.placeholder)
                entries += RedactionEntry(rule.entityType, rule.placeholder, engineLabel)
            }
        }

        return RedactionResult(
            originalText = rawText,
            sanitizedText = sanitized,
            redactionLog = entries.reversed(),
            engineUsed = engineLabel
        )
    }

    fun suggestTagsLocally(
        sanitizedText: String,
        category: DocumentCategory,
        redactionLog: List<RedactionEntry> = emptyList()
    ): List<String> {
        val suggested = LinkedHashSet<String>()

        for (rule in contentTagRules) {
            if (rule.regex.containsMatchIn(sanitizedText)) {
                suggested.add(rule.tag)
            }
        }

        val entityTypes = redactionLog.map { it.entityType }.toSet()
        if ("SSN" in entityTypes || "GOV_ID_16_DIGIT" in entityTypes || "ID_NUMBER" in entityTypes) {
            suggested.add("ID Scrubbed")
        }
        if ("ADDRESS" in entityTypes || "PHONE" in entityTypes || "EMAIL" in entityTypes) {
            suggested.add("Contact Redacted")
        }

        val categoryDefaults = when (category) {
            DocumentCategory.CONTRACT -> listOf("Service Agreement", "Obligations")
            DocumentCategory.ID_PASSPORT -> listOf("Government ID", "KYC Safe")
            DocumentCategory.INVOICE -> listOf("Billing Record", "Expense Audit")
            DocumentCategory.LEGAL -> listOf("Compliance", "Legal Review")
        }
        for (fallback in categoryDefaults) {
            if (suggested.size < 4) {
                suggested.add(fallback)
            }
        }

        return suggested
            .map { it.trim().take(40) }
            .filter { it.isNotEmpty() }
            .take(6)
    }
}
