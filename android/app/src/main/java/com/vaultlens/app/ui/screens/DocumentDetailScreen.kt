package com.vaultlens.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.vaultlens.app.ui.components.PillChip
import com.vaultlens.app.ui.components.VaultCard
import com.vaultlens.app.ui.components.VaultTopBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vaultlens.app.data.model.VaultDocument
import com.vaultlens.app.data.model.VaultFolder
import com.vaultlens.app.ui.components.HybridPrivacyBadge
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { VaultTopBar(title = "Document", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$folderName · ${document.category.label} · ${document.piiRedactedCount} PII redacted",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HybridPrivacyBadge(
                route = document.inferenceRoute,
                piiSanitizedCount = document.piiRedactedCount,
                isOnline = isOnline
            )

            if (folders.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PillChip(
                        selected = document.folderId.isEmpty(),
                        onClick = { onChangeFolder(document.id, "") },
                        label = { Text("Unfiled") }
                    )
                    folders.forEach { folder ->
                        PillChip(
                            selected = document.folderId == folder.id,
                            onClick = { onChangeFolder(document.id, folder.id) },
                            label = { Text(folder.name) }
                        )
                    }
                }
            }

            if (document.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    document.tags.forEach { tag ->
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            DetailBlock("Document Summary", listOf(document.documentSummary))
            DetailBlock("Key Dates & Deadlines", document.keyDatesAndDeadlines)
            DetailBlock("Financial Amounts", document.financialAmounts)
            DetailBlock("Potential Red Flags / Risks", document.redFlagsAndRisks)
            DetailBlock(
                "Local Redaction Log",
                document.redactionLog.map { "${it.entityType} -> ${it.placeholder} (${it.engine})" }
            )

            Button(
                onClick = {
                    val slug = document.title
                        .lowercase()
                        .replace(Regex("[^a-z0-9]+"), "-")
                        .trim('-')
                        .ifEmpty { "vaultlens-document" }
                    createJsonLauncher.launch("$slug-secure-summary.json")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Export Secure JSON")
            }

            jsonExportStatus?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            OutlinedButton(
                onClick = ::shareSafeSummary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Share Safe Redacted Summary")
            }
        }
    }
}

@Composable
private fun DetailBlock(title: String, lines: List<String>) {
    VaultCard {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            if (lines.isEmpty() || lines.all { it.isBlank() }) {
                Text(
                    "None detected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                lines.filter { it.isNotBlank() }.forEach { line ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        )
                        Text(line, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
