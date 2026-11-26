package com.vaultlens.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultlens.app.data.model.AnalysisStatus
import com.vaultlens.app.data.model.InferenceRoute
import com.vaultlens.app.data.model.VaultDocument
import com.vaultlens.app.data.model.VaultFolder
import com.vaultlens.app.ui.components.CircleAction
import com.vaultlens.app.ui.components.FindingLine
import com.vaultlens.app.ui.components.HairlineDivider
import com.vaultlens.app.ui.components.InferenceTrailCard
import com.vaultlens.app.ui.components.MonoTag
import com.vaultlens.app.ui.components.PillChip
import com.vaultlens.app.ui.components.SectionCaption
import com.vaultlens.app.ui.components.VaultCard
import com.vaultlens.app.ui.components.VaultTopBar
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DocumentDetailScreen(
    document: VaultDocument,
    folders: List<VaultFolder> = emptyList(),
    isOnline: Boolean,
    onBack: () -> Unit,
    onChangeFolder: (docId: String, folderId: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var jsonExportStatus by remember { mutableStateOf<String?>(null) }
    val folderName = remember(folders, document.folderId) {
        folders.find { it.id == document.folderId }?.name ?: "Unfiled"
    }

    fun buildSecureJsonString(): String {
        val root = JSONObject().apply {
            put("schemaVersion", "1.0")
            put("exportedAtMillis", System.currentTimeMillis())
            put("privacyGuarantee", JSONObject().apply {
                put("rawPiiExcluded", true)
                put("localOriginalTextIncluded", false)
                put("inferenceRoute", document.inferenceRoute.name)
                put("analysisStatus", document.analysisStatus.name)
            })
            put("document", JSONObject().apply {
                put("id", document.id)
                put("title", document.title)
                put("category", document.category.label)
                put("folder", folderName)
                put("tags", JSONArray(document.tags))
                put("createdAtMillis", document.createdAtMillis)
            })
            put("analysis", JSONObject().apply {
                put("documentSummary", document.documentSummary)
                put("keyDatesAndDeadlines", JSONArray(document.keyDatesAndDeadlines))
                put("financialAmounts", JSONArray(document.financialAmounts))
                put("redFlagsAndRisks", JSONArray(document.redFlagsAndRisks))
            })
            put("redaction", JSONObject().apply {
                put("piiRedactedCount", document.piiRedactedCount)
                put("sanitizedText", document.sanitizedText)
                put("redactionLog", JSONArray().apply {
                    document.redactionLog.forEach { entry ->
                        put(JSONObject().apply {
                            put("entityType", entry.entityType)
                            put("placeholder", entry.placeholder)
                            put("engine", entry.engine)
                        })
                    }
                })
            })
        }
        return root.toString(2)
    }

    val createJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(buildSecureJsonString().toByteArray(Charsets.UTF_8))
                }
            }.onSuccess {
                jsonExportStatus = "Saved secure JSON summary."
            }.onFailure {
                jsonExportStatus = "Could not write JSON export file."
            }
        }
    }

    fun shareSafeSummary() {
        val report = buildString {
            appendLine("VaultLens Safe Redacted Report: ${document.title}")
            appendLine("Category: ${document.category.label}")
            if (document.tags.isNotEmpty()) {
                appendLine("Tags: ${document.tags.joinToString(", ")}")
            }
            appendLine("PII Redacted Locally: ${document.piiRedactedCount}")
            appendLine()
            appendLine("Summary:")
            appendLine(document.documentSummary)
            appendLine()
            appendLine("Sanitized Text:")
            appendLine(document.sanitizedText)
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, report)
        }
        context.startActivity(Intent.createChooser(intent, "Share Safe Redacted Summary"))
    }

    fun exportJson() {
        val slug = document.title
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { "vaultlens-document" }
        createJsonLauncher.launch("$slug-secure-summary.json")
    }

    val scheme = MaterialTheme.colorScheme
    val routeLabel = when (document.inferenceRoute) {
        InferenceRoute.CLOUD_FALLBACK_FLASH -> "Hybrid · device + cloud"
        InferenceRoute.ON_DEVICE_NANO -> "On-device · Nano"
        else -> "On-device · rules"
    }
    val statusLabel = if (document.analysisStatus == AnalysisStatus.QUEUED_OFFLINE) "Queued offline" else "Analyzed"
    val isAnalyzed = document.analysisStatus == AnalysisStatus.ANALYZED
    val riskCount = document.redFlagsAndRisks.size

    Scaffold(
        containerColor = scheme.background,
        topBar = {
            VaultTopBar(
                title = "Document",
                onBack = onBack,
                actionIcon = Icons.Default.Share,
                actionDescription = "Share redacted summary",
                onAction = ::shareSafeSummary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VaultCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "$folderName · ${document.category.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = scheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    HairlineDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = document.piiRedactedCount.toString(),
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp),
                                color = scheme.onSurface
                            )
                            Text(
                                text = "PII redacted locally",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAnalyzed) riskCount.toString() else "–",
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp),
                                color = when {
                                    !isAnalyzed -> scheme.onSurfaceVariant
                                    riskCount > 0 -> scheme.error
                                    else -> scheme.onSurface
                                }
                            )
                            Text(
                                text = "Risks flagged",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonoTag(text = routeLabel, inverted = true)
                        MonoTag(text = statusLabel)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircleAction(icon = Icons.Default.Download, label = "Export JSON", onClick = ::exportJson)
                CircleAction(icon = Icons.Default.Share, label = "Share summary", onClick = ::shareSafeSummary)
            }

            jsonExportStatus?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            InferenceTrailCard(document = document)

            if (folders.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionCaption(text = "Folder", modifier = Modifier.padding(start = 4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            PillChip(
                                selected = document.folderId.isEmpty(),
                                onClick = { onChangeFolder(document.id, "") },
                                label = { Text("Unfiled") }
                            )
                        }
                        items(folders, key = { it.id }) { folder ->
                            PillChip(
                                selected = document.folderId == folder.id,
                                onClick = { onChangeFolder(document.id, folder.id) },
                                label = { Text(folder.name) }
                            )
                        }
                    }
                }
            }

            if (document.tags.isNotEmpty()) {
                VaultCard {
                    Column(modifier = Modifier.padding(18.dp)) {
                        SectionCaption(text = "Tags")
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            document.tags.forEach { tag -> MonoTag(text = tag) }
                        }
                    }
                }
            }

            DetailBlock("Summary", listOf(document.documentSummary))
            DetailBlock("Key dates & deadlines", document.keyDatesAndDeadlines)
            DetailBlock("Financial amounts", document.financialAmounts)
            DetailBlock("Red flags & risks", document.redFlagsAndRisks, danger = true)
            DetailBlock(
                "Local redaction log",
                document.redactionLog.map { "${it.entityType}  →  ${it.placeholder}\n${it.engine}" },
                monospace = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Original text never leaves this device",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailBlock(
    title: String,
    lines: List<String>,
    monospace: Boolean = false,
    danger: Boolean = false
) {
    val visible = lines.filter { it.isNotBlank() }
    val scheme = MaterialTheme.colorScheme
    val flagged = danger && visible.isNotEmpty()
    VaultCard {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionCaption(
                text = title,
                color = if (flagged) scheme.error else scheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (visible.isEmpty()) {
                Text(
                    "None detected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            } else {
                visible.forEachIndexed { index, line ->
                    if (index > 0) {
                        HairlineDivider(modifier = Modifier.padding(vertical = 10.dp))
                    }
                    if (flagged) {
                        FindingLine(text = line, danger = true)
                    } else {
                        Text(
                            text = line,
                            style = if (monospace) {
                                MaterialTheme.typography.labelMedium
                            } else {
                                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal)
                            },
                            color = scheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
