package com.vaultlens.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultlens.app.data.model.AnalysisStatus
import com.vaultlens.app.data.model.DocumentCategory
import com.vaultlens.app.data.model.InferenceRoute
import com.vaultlens.app.data.model.VaultDocument
import com.vaultlens.app.data.model.VaultFolder
import com.vaultlens.app.ui.components.HairlineDivider
import com.vaultlens.app.ui.components.HybridEngineCard
import com.vaultlens.app.ui.components.NanoState
import com.vaultlens.app.ui.components.IconBubble
import com.vaultlens.app.ui.components.NewFolderCircleButton
import com.vaultlens.app.ui.components.PillChip
import com.vaultlens.app.ui.components.SectionCaption
import com.vaultlens.app.ui.components.SectionHeader
import com.vaultlens.app.ui.components.VaultCard
import com.vaultlens.app.ui.components.VaultMark
import com.vaultlens.app.ui.components.vaultFieldColors
import org.json.JSONArray
import org.json.JSONObject

private enum class VaultSortOption(val label: String) {
    DATE_CREATED("Date Created"),
    ALPHABETICAL("Alphabetical"),
    RISK_LEVEL("Risk Level")
}

private val highRiskKeywords = Regex(
    """\b(unlimited|indemnif|collections|penalty|forfeit|breach|uncapped|lock-in|conflict|default)\b""",
    RegexOption.IGNORE_CASE
)

private fun riskScore(doc: VaultDocument): Int {
    val severe = doc.redFlagsAndRisks.count { highRiskKeywords.containsMatchIn(it) }
    return doc.redFlagsAndRisks.size * 10 + severe * 3
}

@Composable
fun VaultDashboardScreen(
    documents: List<VaultDocument>,
    folders: List<VaultFolder> = emptyList(),
    activeRoute: InferenceRoute,
    isOnline: Boolean,
    nanoState: NanoState = NanoState.CHECKING,
    onOpenScanner: () -> Unit,
    onSelectDocument: (VaultDocument) -> Unit,
    onDeleteDocuments: (List<String>) -> Unit = {},
    onCreateFolder: (name: String, color: String) -> Unit = { _, _ -> },
    onDeleteFolder: (folderId: String) -> Unit = {},
    onMoveDocumentsToFolder: (docIds: List<String>, folderId: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<DocumentCategory?>(null) }
    var selectedFolderFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(VaultSortOption.DATE_CREATED) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var moveMenuExpanded by remember { mutableStateOf(false) }
    var isCreatingFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    val folderById = remember(folders) { folders.associateBy { it.id } }

    LaunchedEffect(folders, selectedFolderFilter) {
        if (selectedFolderFilter != "all" && selectedFolderFilter != "unfiled" && selectedFolderFilter !in folderById) {
            selectedFolderFilter = "all"
        }
    }

    val selectedDocuments = remember(documents, selectedIds) {
        documents.filter { it.id in selectedIds }
    }

    val bulkExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && selectedDocuments.isNotEmpty()) {
            val payload = JSONObject().apply {
                put("schemaVersion", "1.0")
                put("exportedAtMillis", System.currentTimeMillis())
                put("privacyGuarantee", JSONObject().apply {
                    put("rawPiiExcluded", true)
                    put("localOriginalTextIncluded", false)
                    put("totalDocuments", selectedDocuments.size)
                })
                put("documents", JSONArray().apply {
                    selectedDocuments.forEach { doc ->
                        put(JSONObject().apply {
                            put("id", doc.id)
                            put("title", doc.title)
                            put("category", doc.category.label)
                            put("folder", folderById[doc.folderId]?.name ?: "Unfiled")
                            put("tags", JSONArray(doc.tags))
                            put("documentSummary", doc.documentSummary)
                            put("keyDatesAndDeadlines", JSONArray(doc.keyDatesAndDeadlines))
                            put("financialAmounts", JSONArray(doc.financialAmounts))
                            put("redFlagsAndRisks", JSONArray(doc.redFlagsAndRisks))
                            put("piiRedactedCount", doc.piiRedactedCount)
                            put("sanitizedText", doc.sanitizedText)
                        })
                    }
                })
            }
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(payload.toString(2).toByteArray(Charsets.UTF_8))
            }
        }
    }

    val filteredDocs = remember(documents, folderById, selectedCategory, selectedFolderFilter, searchQuery, sortOption) {
        val tokens = searchQuery.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val filtered = documents.filter { doc ->
            val matchesCategory = selectedCategory == null || doc.category == selectedCategory
            if (!matchesCategory) return@filter false

            val docFolder = folderById[doc.folderId]
            when (selectedFolderFilter) {
                "all" -> Unit
                "unfiled" -> if (docFolder != null) return@filter false
                else -> if (doc.folderId != selectedFolderFilter) return@filter false
            }

            if (tokens.isEmpty()) return@filter true

            val haystack = buildString {
                append(doc.title)
                append(' ')
                append(doc.category.label)
                append(' ')
                append(docFolder?.name.orEmpty())
                append(' ')
                append(doc.tags.joinToString(" "))
                append(' ')
                append(doc.documentSummary)
                append(' ')
                append(doc.sanitizedText)
                append(' ')
                append(doc.keyDatesAndDeadlines.joinToString(" "))
                append(' ')
                append(doc.financialAmounts.joinToString(" "))
                append(' ')
                append(doc.redFlagsAndRisks.joinToString(" "))
            }.lowercase()

            tokens.all { token -> haystack.contains(token) }
        }

        when (sortOption) {
            VaultSortOption.DATE_CREATED -> filtered.sortedByDescending { it.createdAtMillis }
            VaultSortOption.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
            VaultSortOption.RISK_LEVEL -> filtered.sortedWith(
                compareByDescending<VaultDocument> { riskScore(it) }
                    .thenByDescending { it.piiRedactedCount }
                    .thenByDescending { it.createdAtMillis }
            )
        }
    }
    val totalPiiRedacted = remember(documents) { documents.sumOf { it.piiRedactedCount } }
    val flaggedCount = remember(documents) { documents.count { it.redFlagsAndRisks.isNotEmpty() } }
    val analyzedCount = documents.count { it.analysisStatus == AnalysisStatus.ANALYZED }
    val queuedCount = documents.count { it.analysisStatus == AnalysisStatus.QUEUED_OFFLINE }
    val scheme = MaterialTheme.colorScheme

    Scaffold(containerColor = scheme.background) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Large bottom padding so the floating button never covers the last card.
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 128.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        VaultMark(size = 44.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Your private vault",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant
                            )
                            Text(
                                text = "VaultLens",
                                style = MaterialTheme.typography.titleLarge,
                                color = scheme.onBackground
                            )
                        }
                    }
                }

                item { VaultHeroCard(documentCount = documents.size) }

                item {
                    OverviewCard(
                        piiRedacted = totalPiiRedacted,
                        flagged = flaggedCount,
                        analyzed = analyzedCount,
                        queued = queuedCount,
                        total = documents.size
                    )
                }

                item {
                    HybridEngineCard(
                        nanoState = nanoState,
                        isOnline = isOnline,
                        activeRoute = activeRoute
                    )
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = CircleShape,
                        colors = vaultFieldColors(),
                        placeholder = {
                            Text(
                                "Search documents",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        }
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "Folders") {
                            NewFolderCircleButton(onClick = { isCreatingFolder = !isCreatingFolder })
                        }
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                PillChip(
                                    selected = selectedFolderFilter == "all",
                                    onClick = { selectedFolderFilter = "all" },
                                    label = { Text("All  ${documents.size}") }
                                )
                            }
                            item {
                                val unfiledCount = documents.count { it.folderId !in folderById }
                                PillChip(
                                    selected = selectedFolderFilter == "unfiled",
                                    onClick = { selectedFolderFilter = "unfiled" },
                                    label = { Text("Unfiled  $unfiledCount") }
                                )
                            }
                            items(folders, key = { it.id }) { folder ->
                                val count = documents.count { it.folderId == folder.id }
                                val isActive = selectedFolderFilter == folder.id
                                PillChip(
                                    selected = isActive,
                                    onClick = { selectedFolderFilter = folder.id },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    trailingIcon = if (isActive) {
                                        {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Delete ${folder.name}",
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .clickable { onDeleteFolder(folder.id) }
                                            )
                                        }
                                    } else null,
                                    label = { Text("${folder.name}  $count") }
                                )
                            }
                        }

                        if (isCreatingFolder) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newFolderName,
                                    onValueChange = { if (it.length <= 60) newFolderName = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = CircleShape,
                                    colors = vaultFieldColors(),
                                    placeholder = { Text("New folder name") }
                                )
                                Button(
                                    enabled = newFolderName.trim().isNotEmpty(),
                                    onClick = {
                                        onCreateFolder(newFolderName.trim(), "neutral")
                                        newFolderName = ""
                                        isCreatingFolder = false
                                    },
                                    modifier = Modifier.height(54.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = scheme.primary,
                                        contentColor = scheme.onPrimary,
                                        disabledContainerColor = scheme.outline,
                                        disabledContentColor = scheme.onSurfaceVariant
                                    )
                                ) {
                                    Text("Create")
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "Documents") {
                            Box {
                                Surface(
                                    onClick = { sortMenuExpanded = true },
                                    shape = CircleShape,
                                    color = scheme.surface,
                                    contentColor = scheme.onSurface
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .padding(start = 14.dp, end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = sortOption.label,
                                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp)
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Sort options",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false }
                                ) {
                                    VaultSortOption.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            trailingIcon = if (option == sortOption) {
                                                { Icon(Icons.Default.Check, contentDescription = null) }
                                            } else null,
                                            onClick = {
                                                sortOption = option
                                                sortMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                PillChip(
                                    selected = selectedCategory == null,
                                    onClick = { selectedCategory = null },
                                    label = { Text("All") }
                                )
                            }
                            items(DocumentCategory.entries) { category ->
                                PillChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.label) }
                                )
                            }
                        }
                    }
                }

                item {
                    VaultCard {
                        if (filteredDocs.isEmpty()) {
                            Column(modifier = Modifier.padding(22.dp)) {
                                Text(
                                    text = "No matching documents",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = scheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try clearing your search or filters.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        } else {
                            SectionCaption(
                                text = "${filteredDocs.size} ${if (filteredDocs.size == 1) "document" else "documents"}",
                                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 4.dp)
                            )
                            filteredDocs.forEachIndexed { index, doc ->
                                if (index > 0) {
                                    HairlineDivider(modifier = Modifier.padding(horizontal = 18.dp))
                                }
                                DocumentRow(
                                    doc = doc,
                                    folderName = folderById[doc.folderId]?.name,
                                    selected = doc.id in selectedIds,
                                    onOpen = { onSelectDocument(doc) },
                                    onToggleSelect = {
                                        selectedIds = if (doc.id in selectedIds) selectedIds - doc.id else selectedIds + doc.id
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // Soft fade so cards slide out from under the floating action area.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, scheme.background))
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
            ) {
                if (selectedDocuments.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = CircleShape,
                        color = scheme.primary,
                        contentColor = scheme.onPrimary,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 22.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedDocuments.size} selected",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Box {
                                IconButton(onClick = { moveMenuExpanded = true }) {
                                    Icon(Icons.Default.DriveFileMove, contentDescription = "Move to folder")
                                }
                                DropdownMenu(
                                    expanded = moveMenuExpanded,
                                    onDismissRequest = { moveMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Unfiled") },
                                        onClick = {
                                            onMoveDocumentsToFolder(selectedDocuments.map { it.id }, "")
                                            moveMenuExpanded = false
                                        }
                                    )
                                    folders.forEach { folder ->
                                        DropdownMenuItem(
                                            text = { Text(folder.name) },
                                            onClick = {
                                                onMoveDocumentsToFolder(selectedDocuments.map { it.id }, folder.id)
                                                moveMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { bulkExportLauncher.launch("vaultlens-bulk-export.json") }) {
                                Icon(Icons.Default.Download, contentDescription = "Export")
                            }
                            IconButton(
                                onClick = {
                                    onDeleteDocuments(selectedDocuments.map { it.id })
                                    selectedIds = emptySet()
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                            IconButton(onClick = { selectedIds = emptySet() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear selection")
                            }
                        }
                    }
                } else {
                    Surface(
                        onClick = onOpenScanner,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = CircleShape,
                        color = scheme.primary,
                        contentColor = scheme.onPrimary,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DocumentScanner,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Scan or paste document",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Black "card" hero, in the spirit of the credit-card reference, without colour. */
@Composable
private fun VaultHeroCard(documentCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2B2B2E),
                        Color(0xFF161618),
                        Color(0xFF0B0B0C),
                        Color(0xFF1E1E21)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .drawBehind {
                val ring = Color.White.copy(alpha = 0.10f)
                listOf(0.52f, 0.62f, 0.72f).forEachIndexed { i, r ->
                    drawCircle(
                        color = ring,
                        radius = size.height * r,
                        center = Offset(size.width * (0.70f + i * 0.04f), size.height * 0.42f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "vaultlens",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.6).sp
                )
                Text(
                    text = "PRIVATE",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.4.sp
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = documentCount.toString(),
                        color = Color.White,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-1.2).sp
                    )
                    Text(
                        text = "documents protected",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "vault",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** White summary card, like "Current balance" in the reference. */
@Composable
private fun OverviewCard(
    piiRedacted: Int,
    flagged: Int,
    analyzed: Int,
    queued: Int,
    total: Int
) {
    val scheme = MaterialTheme.colorScheme
    val progress = if (total == 0) 0f else analyzed.toFloat() / total
    VaultCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Protection overview",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OverviewStat(
                    value = piiRedacted.toString(),
                    label = "PII redacted locally",
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(scheme.outline)
                )
                OverviewStat(
                    value = flagged.toString(),
                    label = "Documents with risks",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(scheme.onSurface)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$analyzed of $total analyzed",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = if (queued > 0) "$queued queued offline" else "All synced",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = scheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun OverviewStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun categoryIcon(category: DocumentCategory): ImageVector = when (category) {
    DocumentCategory.CONTRACT -> Icons.Default.Description
    DocumentCategory.ID_PASSPORT -> Icons.Default.Badge
    DocumentCategory.INVOICE -> Icons.Default.Receipt
    DocumentCategory.LEGAL -> Icons.Default.Gavel
}

/** One row inside the documents card. Tap the round icon to select it. */
@Composable
private fun DocumentRow(
    doc: VaultDocument,
    folderName: String?,
    selected: Boolean,
    onOpen: () -> Unit,
    onToggleSelect: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val queued = doc.analysisStatus == AnalysisStatus.QUEUED_OFFLINE
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) scheme.surfaceVariant else Color.Transparent)
            .clickable(onClick = onOpen)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onToggleSelect)
        ) {
            IconBubble(
                icon = if (selected) Icons.Default.Check else categoryIcon(doc.category),
                filled = selected,
                contentDescription = if (selected) "Selected, tap to unselect" else "Select document"
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = doc.title,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = listOfNotNull(folderName, doc.category.label).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (doc.tags.isNotEmpty()) {
                Text(
                    text = doc.tags.take(3).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = doc.piiRedactedCount.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = scheme.onSurface
            )
            Text(
                text = "PII hidden",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
            Text(
                text = if (queued) "Queued" else "Analyzed",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (queued) scheme.onSurfaceVariant else scheme.onSurface
            )
        }
    }
}
