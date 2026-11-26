package com.vaultlens.app.data.model

enum class DocumentCategory(val label: String) {
    CONTRACT("Contract"),
    ID_PASSPORT("ID/Passport"),
    INVOICE("Invoice"),
    LEGAL("Legal")
}

enum class InferenceRoute {
    ON_DEVICE_NANO,
    ON_DEVICE_RULES,
    CLOUD_FALLBACK_FLASH
}

enum class AnalysisStatus {
    ANALYZED,
    QUEUED_OFFLINE,
    LOCAL_ONLY
}

data class RedactionEntry(
    val entityType: String = "",
    val placeholder: String = "",
    val engine: String = ""
)

data class RedactionResult(
    val originalText: String,
    val sanitizedText: String,
    val redactionLog: List<RedactionEntry>,
    val engineUsed: String
)

data class CloudInsight(
    val documentSummary: String = "",
    val keyDatesAndDeadlines: List<String> = emptyList(),
    val financialAmounts: List<String> = emptyList(),
    val redFlagsAndRisks: List<String> = emptyList(),
    val suggestedTags: List<String> = emptyList()
)

data class VaultFolder(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val color: String = "indigo",
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class VaultDocument(
    val id: String = "",
    val ownerId: String = "",
    val title: String = "",
    val category: DocumentCategory = DocumentCategory.CONTRACT,
    val folderId: String = "",
    val sanitizedText: String = "",
    val piiRedactedCount: Int = 0,
    val redactionLog: List<RedactionEntry> = emptyList(),
    val inferenceRoute: InferenceRoute = InferenceRoute.ON_DEVICE_NANO,
    val analysisStatus: AnalysisStatus = AnalysisStatus.ANALYZED,
    val documentSummary: String = "",
    val keyDatesAndDeadlines: List<String> = emptyList(),
    val financialAmounts: List<String> = emptyList(),
    val redFlagsAndRisks: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis()
)
