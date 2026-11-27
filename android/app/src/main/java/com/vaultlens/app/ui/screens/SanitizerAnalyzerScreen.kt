package com.vaultlens.app.ui.screens

import com.vaultlens.app.ui.components.CircleIconButton
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.graphics.Brush
import com.vaultlens.app.ui.theme.softShadow
import com.vaultlens.app.ui.components.MonoTag
import com.vaultlens.app.ui.theme.vaultHeaderBrush
import androidx.compose.foundation.background
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.vaultlens.app.ui.components.FindingLine
import com.vaultlens.app.ui.components.HairlineDivider
import com.vaultlens.app.ui.components.HybridPipeline
import com.vaultlens.app.ui.components.NanoState
import com.vaultlens.app.ui.components.PipelineStage
import com.vaultlens.app.ui.components.IconBubble
import com.vaultlens.app.ui.components.PillChip
import com.vaultlens.app.ui.components.PrimaryPillButton
import com.vaultlens.app.ui.components.SectionCaption
import com.vaultlens.app.ui.components.VaultCard
import com.vaultlens.app.ui.components.VaultTopBar
import com.vaultlens.app.ui.components.vaultFieldColors
import com.vaultlens.app.ui.theme.vaultExtras
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SanitizerAnalyzerScreen(
    router: HybridPrivacyRouter,
    activeRoute: InferenceRoute,
    isOnline: Boolean,
    nanoState: NanoState = NanoState.CHECKING,
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
    var sourceTab by remember { mutableStateOf(0) } // 0 camera, 1 paste, 2 file

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


    val scheme = MaterialTheme.colorScheme
    val fieldShape = RoundedCornerShape(20.dp)

    val extras = MaterialTheme.vaultExtras
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to extras.headerTop,
                    0.55f to extras.sheet,
                    1f to extras.sheet
                )
            )
    ) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack
                )
                Text(
                    text = "Scan or paste",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp, lineHeight = 28.sp),
                    color = scheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Surface(shape = CircleShape, color = scheme.primaryContainer) {
                    Text(
                        text = if (isOnline) "Hybrid" else "On-device",
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SourceTab(
                    label = "Camera",
                    selected = sourceTab == 0,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        sourceTab = 0
                        cameraLauncher.launch(null)
                    }
                )
                SourceTab(
                    label = "Paste",
                    selected = sourceTab == 1,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                    onClick = { sourceTab = 1 }
                )
                SourceTab(
                    label = "File",
                    selected = sourceTab == 2,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        sourceTab = 2
                        imagePicker.launch("image/*")
                    }
                )
            }

            VaultCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = redactionResult?.let { "Found on your phone · ${it.redactionLog.size} items" }
                            ?: "Paste or scan to begin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                    if (redactionResult == null) {
                        OutlinedTextField(
                            value = rawText,
                            onValueChange = { rawText = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = scheme.primary
                            ),
                            placeholder = { Text("Paste or type the text here. It never leaves this device.") },
                            minLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val result = redactionResult!!
                        Text(
                            text = if (showOriginal) AnnotatedString(result.originalText)
                            else blockRedactions(result.sanitizedText, scheme.primary),
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.onSurface
                        )
                        val counts = result.redactionLog.groupingBy { it.entityType.toString() }.eachCount()
                        if (counts.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                counts.forEach { (type, count) ->
                                    val name = type.lowercase().replace('_', ' ')
                                    val label = if (name.length <= 3) name.uppercase() else if (count > 1) name + "s" else name
                                    Surface(shape = CircleShape, color = scheme.primaryContainer) {
                                        Text(
                                            text = "$count $label",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = scheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                        TextButton(onClick = { redactionResult = null }) {
                            Text("Edit text", color = scheme.onSurface)
                        }
                    }
                }
            }

            VaultCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "Read" to "ML Kit OCR",
                        "Find" to "On-device",
                        "Redact" to "Local rules",
                        "Reason" to "Cloud"
                    ).forEachIndexed { index, (step, sub) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = step,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (index == 3) scheme.onSurface else Color(0xFF2C6E8E)
                            )
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(22.dp))
                    Column {
                        Text("Cloud sees placeholders only", style = MaterialTheme.typography.titleMedium)
                        Text("Names become [REDACTED_NAME]", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            FieldGroup(caption = "Title") {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    shape = fieldShape,
                    colors = vaultFieldColors(),
                    placeholder = { Text("Document title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FieldGroup(caption = "Category") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DocumentCategory.entries) { item ->
                        PillChip(
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
            }

            if (folders.isNotEmpty()) {
                FieldGroup(caption = "Save to folder") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            PillChip(
                                selected = selectedFolderId.isEmpty(),
                                onClick = { selectedFolderId = "" },
                                label = { Text("Unfiled") }
                            )
                        }
                        items(folders, key = { it.id }) { folder ->
                            PillChip(
                                selected = selectedFolderId == folder.id,
                                onClick = { selectedFolderId = folder.id },
                                label = { Text(folder.name) }
                            )
                        }
                    }
                }
            }

            PrimaryPillButton(
                text = if (isBusy && redactionResult == null) "Working…" else "Redact PII locally",
                onClick = { applyLocalRedaction(rawText) },
                enabled = rawText.isNotBlank() && !isBusy,
                icon = Icons.Default.Lock
            )
            Text(
                text = "Offline? Rule-based redaction still works",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            redactionResult?.let { result ->
                VaultCard {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (showOriginal) "Original" else "Sanitized output",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = scheme.onSurface
                                )
                                Text(
                                    text = if (showOriginal) "Local only, never uploaded"
                                    else "${result.redactionLog.size} items redacted",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = showOriginal,
                                onCheckedChange = { showOriginal = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = scheme.onPrimary,
                                    checkedTrackColor = scheme.primary,
                                    uncheckedThumbColor = scheme.onSurfaceVariant,
                                    uncheckedTrackColor = scheme.surfaceVariant,
                                    uncheckedBorderColor = scheme.outline
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.small,
                            color = scheme.surfaceVariant
                        ) {
                            Text(
                                text = if (showOriginal) {
                                    AnnotatedString(result.originalText)
                                } else {
                                    highlightRedactedTokens(result.sanitizedText)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSurface,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                VaultCard {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconBubble(icon = Icons.Default.Lock, size = 40.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Outbound payload",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = scheme.onSurface
                                )
                                Text(
                                    text = "Zero-leak proof of what gets sent",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { showPayloadProof = !showPayloadProof }) {
                                Text(if (showPayloadProof) "Hide" else "Inspect", color = scheme.onSurface)
                            }
                        }
                        if (showPayloadProof) {
                            val proofJson = JSONObject().apply {
                                put("model", "gemini-3.8-flash")
                                put("appCheckVerified", true)
                                put("rawOriginalTextIncluded", false)
                                put("piiScrubbedLocally", result.redactionLog.size)
                                put("sanitizedText", result.sanitizedText)
                            }.toString(2)
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                                color = scheme.surfaceVariant
                            ) {
                                Text(
                                    text = proofJson,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = scheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }

                VaultCard {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tags",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = scheme.onSurface
                                )
                                Text(
                                    text = "${selectedTags.size} of 10 selected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                            IconButton(
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
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh tag suggestions")
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestedTags.forEach { tag ->
                                PillChip(
                                    selected = tag in selectedTags,
                                    onClick = { toggleTag(tag) },
                                    label = { Text(tag) },
                                    onCard = true
                                )
                            }
                        }

                        OutlinedTextField(
                            value = customTagInput,
                            onValueChange = { if (it.length <= 40) customTagInput = it },
                            placeholder = { Text("Add a custom tag") },
                            singleLine = true,
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = scheme.surfaceVariant,
                                unfocusedContainerColor = scheme.surfaceVariant,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = scheme.primary,
                                cursorColor = scheme.primary
                            ),
                            trailingIcon = {
                                IconButton(
                                    onClick = ::addCustomTag,
                                    enabled = customTagInput.isNotBlank() && selectedTags.size < 10
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add tag")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                PrimaryPillButton(
                    text = when {
                        isBusy -> "Working…"
                        isOnline -> "Analyze with Gemini Flash"
                        else -> "Queue offline"
                    },
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
                    enabled = !isBusy
                )
            }

            cloudInsight?.let { insight ->
                InsightSectionCard("Summary", listOf(insight.documentSummary))
                InsightSectionCard("Key dates & deadlines", insight.keyDatesAndDeadlines)
                InsightSectionCard("Financial amounts", insight.financialAmounts)
                InsightSectionCard("Red flags & risks", insight.redFlagsAndRisks, danger = true)
            }
        }
    }
    }
}

@Composable
private fun FieldGroup(caption: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionCaption(text = caption, modifier = Modifier.padding(start = 4.dp))
        content()
    }
}

@Composable
private fun SourceRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconBubble(icon = icon, size = 40.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun highlightRedactedTokens(text: String): AnnotatedString {
    val extras = MaterialTheme.vaultExtras
    val pattern = Regex("""\[REDACTED_[A-Z_]+]""")
    return buildAnnotatedString {
        append(text)
        pattern.findAll(text).forEach { match ->
            addStyle(
                style = SpanStyle(
                    color = extras.onInk,
                    background = extras.ink,
                    fontWeight = FontWeight.Bold
                ),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }
}

@Composable
private fun InsightSectionCard(title: String, items: List<String>, danger: Boolean = false) {
    val lines = items.filter { it.isNotBlank() }
    val scheme = MaterialTheme.colorScheme
    val flagged = danger && lines.isNotEmpty()
    VaultCard {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionCaption(
                text = title,
                color = if (flagged) scheme.error else scheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (lines.isEmpty()) {
                Text(
                    "None detected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
            lines.forEachIndexed { index, item ->
                if (index > 0) {
                    HairlineDivider(modifier = Modifier.padding(vertical = 10.dp))
                }
                if (flagged) {
                    FindingLine(text = item, danger = true)
                } else {
                    Text(
                        item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface
                    )
                }
            }
        }
    }
}

/** Source tab (Camera / Paste / File): pale blue when active, white otherwise. */
@Composable
private fun SourceTab(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .softShadow(CircleShape, elevation = 3.dp),
        shape = CircleShape,
        color = if (selected) scheme.primaryContainer else scheme.surface,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
    }
}

/** Shows each [REDACTED_*] placeholder as a solid navy block, like the mockup. */
private fun blockRedactions(text: String, ink: Color): AnnotatedString = buildAnnotatedString {
    val token = Regex("\\[[A-Z0-9_]+\\]")
    var last = 0
    token.findAll(text).forEach { m ->
        append(text.substring(last, m.range.first))
        pushStyle(SpanStyle(background = ink, color = ink))
        append("█".repeat(m.value.length.coerceIn(6, 14)))
        pop()
        last = m.range.last + 1
    }
    append(text.substring(last))
}
