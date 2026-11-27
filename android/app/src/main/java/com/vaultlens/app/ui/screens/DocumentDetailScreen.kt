package com.vaultlens.app.ui.screens

import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import androidx.compose.material3.Surface
import com.vaultlens.app.ui.theme.softShadow
import com.vaultlens.app.ui.components.PrimaryPillButton
import com.vaultlens.app.ui.components.CircleIconButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import com.vaultlens.app.ui.theme.vaultExtras
import com.vaultlens.app.ui.components.vaultHeader
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
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

    val dateLabel = remember(document.createdAtMillis) {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(document.createdAtMillis))
    }
    val amountLabel = document.financialAmounts.firstOrNull { it.isNotBlank() } ?: "–"

    Scaffold(
        containerColor = MaterialTheme.vaultExtras.sheet,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: gradient, big title, details chip and the stats pill.
            val glass = scheme.surface.copy(alpha = 0.4f)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .vaultHeader()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = 44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CircleIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                        containerColor = glass,
                        elevated = false
                    )
                    CircleIconButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Share redacted summary",
                        onClick = ::shareSafeSummary,
                        containerColor = glass,
                        elevated = false
                    )
                }
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MonoTag(text = "${document.category.label} · PII hidden")
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp, lineHeight = 46.sp),
                        color = scheme.onBackground
                    )
                    Text(
                        text = "$dateLabel · $statusLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = scheme.primaryContainer) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp), tint = scheme.onPrimaryContainer)
                                Text(routeLabel, style = MaterialTheme.typography.titleMedium, color = scheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircleIconButton(
                                icon = Icons.Default.Download,
                                contentDescription = "Export JSON",
                                onClick = ::exportJson,
                                containerColor = scheme.surface,
                                elevated = false
                            )
                            CircleIconButton(
                                icon = Icons.Default.Share,
                                contentDescription = "Share summary",
                                onClick = ::shareSafeSummary,
                                containerColor = scheme.surface,
                                elevated = false
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .softShadow(MaterialTheme.shapes.extraLarge),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = scheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HeaderStat(
                                value = document.piiRedactedCount.toString(),
                                label = "Redacted",
                                modifier = Modifier.weight(1f)
                            )
                            HeaderStat(
                                value = if (isAnalyzed) riskCount.toString() else "–",
                                label = "Red flags",
                                modifier = Modifier.weight(1f)
                            )
                            HeaderStat(
                                value = amountLabel,
                                label = "Amount",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                jsonExportStatus?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleLarge,
                        color = scheme.onBackground
                    )
                    Text(
                        text = document.documentSummary.ifBlank { "No summary yet." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant
                    )
                }

                DetailBlock("Red flags", document.redFlagsAndRisks, danger = true)
                DetailBlock("Key dates & deadlines", document.keyDatesAndDeadlines)
                DetailBlock("Financial amounts", document.financialAmounts)

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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        document.tags.forEach { tag -> MonoTag(text = tag) }
                    }
                }

                DetailBlock(
                    "Local redaction log",
                    document.redactionLog.map { "${it.entityType}  →  ${it.placeholder}\n${it.engine}" },
                    monospace = true
                )

                PrimaryPillButton(
                    text = "Share redacted summary",
                    onClick = ::shareSafeSummary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun HeaderStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color(0xFF2C6E8E)
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/** Titled list. Each line is a soft white row; risk rows get an amber dot like the mockup. */
@Composable
private fun DetailBlock(
    title: String,
    lines: List<String>,
    monospace: Boolean = false,
    danger: Boolean = false
) {
    val visible = lines.filter { it.isNotBlank() }
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = scheme.onBackground
        )
        if (visible.isEmpty()) {
            Text(
                text = "None detected.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        } else {
            visible.forEach { line ->
                val parts = line.split(Regex(":\\s+|\\s+[—–-]\\s+"), limit = 2)
                val rowTitle = parts[0].trim()
                val rowSub = parts.getOrNull(1)?.trim().orEmpty()
                VaultCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (danger) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE9A23B))
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (monospace || rowSub.isEmpty()) line else rowTitle,
                                style = if (monospace) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium,
                                color = scheme.onSurface
                            )
                            if (!monospace && rowSub.isNotEmpty()) {
                                Text(
                                    text = rowSub,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
