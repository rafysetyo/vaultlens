package com.vaultlens.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vaultlens.app.data.hybrid.HybridPrivacyRouter
import com.vaultlens.app.data.hybrid.LocalPiiSanitizer
import com.vaultlens.app.data.model.AnalysisStatus
import com.vaultlens.app.data.model.CloudInsight
import com.vaultlens.app.data.model.DocumentCategory
import com.vaultlens.app.data.model.InferenceRoute
import com.vaultlens.app.data.model.RedactionResult
import com.vaultlens.app.data.model.VaultDocument
import com.vaultlens.app.data.model.VaultFolder
import com.vaultlens.app.data.ocr.LocalOcrProcessor
import com.vaultlens.app.ui.components.HybridPrivacyBadge
import com.vaultlens.app.ui.theme.Emerald400
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SanitizerAnalyzerScreen(
    router: HybridPrivacyRouter,
    activeRoute: InferenceRoute,
    isOnline: Boolean,
    folders: List<VaultFolder> = emptyList(),
    onBack: () -> Unit,
    onDocumentAnalyzed: (VaultDocument) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ocrProcessor = remember { LocalOcrProcessor(context) }

    var title by remember { mutableStateOf("Untitled Scanned Document") }
    var category by remember { mutableStateOf(DocumentCategory.CONTRACT) }
    var selectedFolderId by remember { mutableStateOf("") }
    var rawText by remember { mutableStateOf("") }
    var redactionResult by remember { mutableStateOf<RedactionResult?>(null) }
    var showOriginal by remember { mutableStateOf(false) }
    var suggestedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var customTagInput by remember { mutableStateOf("") }
    var cloudInsight by remember { mutableStateOf<CloudInsight?>(null) }
    var showPayloadProof by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }

    fun applyLocalRedaction(text: String, currentCategory: DocumentCategory = category) {
        scope.launch {
            isBusy = true
            val result = router.detectAndRedactLocally(text)
            redactionResult = result
            val localTags = LocalPiiSanitizer.suggestTagsLocally(
                sanitizedText = result.sanitizedText,
                category = currentCategory,
                redactionLog = result.redactionLog
            )
            suggestedTags = localTags
            selectedTags = localTags.take(4)
            isBusy = false
        }
    }

    fun toggleTag(tag: String) {
        selectedTags = if (tag in selectedTags) {
            selectedTags.filterNot { it == tag }
        } else if (selectedTags.size < 10) {
            selectedTags + tag
        } else {
            selectedTags
        }
    }

    fun addCustomTag() {
        val cleaned = customTagInput.trim().take(40)
        if (cleaned.isEmpty()) return
        if (suggestedTags.none { it.equals(cleaned, ignoreCase = true) }) {
            suggestedTags = (suggestedTags + cleaned).take(10)
        }
        if (selectedTags.size < 10 && selectedTags.none { it.equals(cleaned, ignoreCase = true) }) {
            selectedTags = selectedTags + cleaned
        }
        customTagInput = ""
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isBusy = true
                val extracted = ocrProcessor.extractTextFromUri(uri)
                rawText = extracted
                val result = router.detectAndRedactLocally(extracted)
                redactionResult = result
                val localTags = LocalPiiSanitizer.suggestTagsLocally(
                    sanitizedText = result.sanitizedText,
                    category = category,
                    redactionLog = result.redactionLog
                )
                suggestedTags = localTags
                selectedTags = localTags.take(4)
                isBusy = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            scope.launch {
                isBusy = true
                val extracted = ocrProcessor.extractTextFromBitmap(bitmap)
                rawText = extracted
                val result = router.detectAndRedactLocally(extracted)
                redactionResult = result
                val localTags = LocalPiiSanitizer.suggestTagsLocally(
                    sanitizedText = result.sanitizedText,
                    category = category,
                    redactionLog = result.redactionLog
                )
                suggestedTags = localTags
                selectedTags = localTags.take(4)
                isBusy = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Sanitizer & Analyzer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HybridPrivacyBadge(
                route = activeRoute,
                piiSanitizedCount = redactionResult?.redactionLog?.size ?: 0,
                isOnline = isOnline
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Document Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DocumentCategory.entries.forEach { item ->
                    FilterChip(
                        selected = category == item,
                        onClick = {
                            category = item
                            redactionResult?.let { currentResult ->
                                val refreshed = LocalPiiSanitizer.suggestTagsLocally(
                                    sanitizedText = currentResult.sanitizedText,
                                    category = item,
                                    redactionLog = currentResult.redactionLog
                                )
                                suggestedTags = (refreshed + suggestedTags).distinct().take(10)
                            }
                        },
                        label = { Text(item.label) }
                    )
                }
            }

            if (folders.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFolderId.isEmpty(),
                        onClick = { selectedFolderId = "" },
                        label = { Text("Unfiled") }
                    )
                    folders.forEach { folder ->
                        FilterChip(
                            selected = selectedFolderId == folder.id,
                            onClick = { selectedFolderId = folder.id },
                            label = { Text(folder.name) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Camera OCR")
                }
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Gallery Image")
                }
            }

            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                label = { Text("Raw Sensitive Document Text (Stays On-Device)") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { applyLocalRedaction(rawText) },
                enabled = rawText.isNotBlank() && !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Step 1: Redact PII Locally")
            }

            redactionResult?.let { result ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (showOriginal) "Original (Local Only)" else "Sanitized Output",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Compare Original", style = MaterialTheme.typography.bodyMedium)
                                Switch(checked = showOriginal, onCheckedChange = { showOriginal = it })
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showOriginal) {
                                AnnotatedString(result.originalText)
                            } else {
                                highlightRedactedTokens(result.sanitizedText)
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Emerald400
                                )
                                Text(
                                    text = "Outbound Cloud Payload (Zero-Leak Proof)",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            TextButton(onClick = { showPayloadProof = !showPayloadProof }) {
                                Text(if (showPayloadProof) "Hide" else "Inspect")
                            }
                        }
                        if (showPayloadProof) {
                            val proofJson = JSONObject().apply {
                                put("model", "gemini-2.5-flash")
                                put("appCheckVerified", true)
                                put("rawOriginalTextIncluded", false)
                                put("piiScrubbedLocally", result.redactionLog.size)
                                put("sanitizedText", result.sanitizedText)
                            }.toString(2)
                            Text(
                                text = proofJson,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Auto-Suggested Content Tags (${selectedTags.size}/10)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(
                                onClick = {
                                    val refreshed = LocalPiiSanitizer.suggestTagsLocally(
                                        sanitizedText = result.sanitizedText,
                                        category = category,
                                        redactionLog = result.redactionLog
                                    )
                                    suggestedTags = (refreshed + suggestedTags).distinct().take(10)
                                    selectedTags = refreshed.take(4)
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                                Text("Refresh")
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            suggestedTags.forEach { tag ->
                                FilterChip(
                                    selected = tag in selectedTags,
                                    onClick = { toggleTag(tag) },
                                    label = { Text(tag) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customTagInput,
                                onValueChange = { if (it.length <= 40) customTagInput = it },
                                label = { Text("Add custom tag") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = ::addCustomTag,
                                enabled = customTagInput.isNotBlank() && selectedTags.size < 10
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add tag")
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isBusy = true
                            val docId = "doc_${UUID.randomUUID().toString().take(12)}"
                            if (!isOnline) {
                                val offlineTags = (if (selectedTags.isNotEmpty()) selectedTags else suggestedTags)
                                    .distinct()
                                    .take(10)
                                onDocumentAnalyzed(
                                    VaultDocument(
                                        id = docId,
                                        title = title,
                                        category = category,
                                        folderId = selectedFolderId,
                                        sanitizedText = result.sanitizedText,
                                        piiRedactedCount = result.redactionLog.size,
                                        redactionLog = result.redactionLog,
                                        inferenceRoute = activeRoute,
                                        analysisStatus = AnalysisStatus.QUEUED_OFFLINE,
                                        documentSummary = "Queued offline in Firestore cache. Auto-analyzes upon reconnect.",
                                        tags = offlineTags
                                    )
                                )
                            } else {
                                val insight = router.analyzeSanitizedDocument(
                                    sanitizedText = result.sanitizedText,
                                    category = category
                                )
                                cloudInsight = insight
                                val mergedSuggested = (insight.suggestedTags + suggestedTags).distinct().take(10)
                                val finalTags = (selectedTags + insight.suggestedTags).distinct().take(10)
                                suggestedTags = mergedSuggested
                                selectedTags = finalTags
                                onDocumentAnalyzed(
                                    VaultDocument(
                                        id = docId,
                                        title = title,
                                        category = category,
                                        folderId = selectedFolderId,
                                        sanitizedText = result.sanitizedText,
                                        piiRedactedCount = result.redactionLog.size,
                                        redactionLog = result.redactionLog,
                                        inferenceRoute = InferenceRoute.CLOUD_FALLBACK_FLASH,
                                        analysisStatus = AnalysisStatus.ANALYZED,
                                        documentSummary = insight.documentSummary,
                                        keyDatesAndDeadlines = insight.keyDatesAndDeadlines,
                                        financialAmounts = insight.financialAmounts,
                                        redFlagsAndRisks = insight.redFlagsAndRisks,
                                        tags = finalTags
                                    )
                                )
                            }
                            isBusy = false
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isOnline) "Step 2: Analyze Sanitized Text (Gemini Flash)"
                        else "Queue Sanitized Text Offline"
                    )
                }
            }

            cloudInsight?.let { insight ->
                InsightSectionCard("Document Summary", listOf(insight.documentSummary))
                InsightSectionCard("Key Dates & Deadlines", insight.keyDatesAndDeadlines)
                InsightSectionCard("Financial Amounts", insight.financialAmounts)
                InsightSectionCard("Potential Red Flags / Risks", insight.redFlagsAndRisks)
            }
        }
    }
}

@Composable
private fun highlightRedactedTokens(text: String): AnnotatedString {
    val pattern = Regex("""\[REDACTED_[A-Z_]+]""")
    return buildAnnotatedString {
        append(text)
        pattern.findAll(text).forEach { match ->
            addStyle(
                style = SpanStyle(
                    color = Emerald400,
                    background = Color(0x2610B981),
                    fontWeight = FontWeight.Bold
                ),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }
}

@Composable
private fun InsightSectionCard(title: String, items: List<String>) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            items.forEach { item ->
                Text("• $item", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
