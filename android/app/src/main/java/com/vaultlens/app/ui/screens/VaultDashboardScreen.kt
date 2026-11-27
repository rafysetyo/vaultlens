package com.vaultlens.app.ui.screens

import androidx.compose.material.icons.filled.Notifications
import com.vaultlens.app.ui.theme.softShadow
import com.vaultlens.app.ui.components.MonoTag
import com.vaultlens.app.ui.components.CircleIconButton
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NorthEast
import com.vaultlens.app.ui.theme.vaultExtras
import com.vaultlens.app.ui.theme.BrandNavy
import com.vaultlens.app.ui.components.vaultHeader
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material.icons.filled.Lock
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
import com.vaultlens.app.ui.theme.BrandCyan
import com.vaultlens.app.ui.theme.BrandInk
import com.vaultlens.app.ui.theme.BrandTeal
import com.vaultlens.app.ui.theme.BrandTile
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
    var searchOpen by remember { mutableStateOf(false) }
    var tileFilter by remember { mutableStateOf<String?>(null) }

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

    val filteredDocs = remember(documents, folderById, tileFilter, selectedFolderFilter, searchQuery, sortOption) {
        val tokens = searchQuery.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val filtered = documents.filter { doc ->
            val matchesCategory = when (tileFilter) {
                "contracts" -> doc.category == DocumentCategory.CONTRACT || doc.category == DocumentCategory.LEGAL
                "invoices" -> doc.category == DocumentCategory.INVOICE
                "ids" -> doc.category == DocumentCategory.ID_PASSPORT
                "unfiled" -> doc.folderId !in folderById
                else -> true
            }
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
    val sheetColor = MaterialTheme.vaultExtras.sheet
    val recentDocs = remember(documents) { documents.sortedByDescending { it.createdAtMillis }.take(6) }

    Scaffold(
        containerColor = sheetColor,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Large bottom padding so the floating button never covers the last card.
                contentPadding = PaddingValues(bottom = 128.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .vaultHeader()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(top = 12.dp, bottom = 44.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            VaultMark(size = 44.dp)
                            Text(
                                text = "VaultLens",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp, lineHeight = 32.sp),
                                color = scheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Box {
                                CircleIconButton(
                                    icon = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    onClick = {},
                                    elevated = false
                                )
                                if (queuedCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(top = 8.dp, end = 8.dp)
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(scheme.error)
                                    )
                                }
                            }
                        }
                        MonoTag(
                            text = "${documents.size} protected · ${if (isOnline) "Hybrid" else "On-device"} redaction",
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (recentDocs.isEmpty()) {
                                item {
                                    RecentDocCard(
                                        title = "No documents yet",
                                        subtitle = "Scan or paste your first one",
                                        highlighted = true,
                                        onClick = onOpenScanner
                                    )
                                }
                            } else {
                                itemsIndexed(recentDocs, key = { _, doc -> doc.id }) { index, doc ->
                                    RecentDocCard(
                                        title = doc.title,
                                        subtitle = "${doc.category.label} · ${doc.piiRedactedCount} PII hidden",
                                        highlighted = index == 0,
                                        onClick = { onSelectDocument(doc) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(scheme.primary)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Let's find\nyour document",
                                style = MaterialTheme.typography.headlineMedium,
                                color = scheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                            CircleIconButton(
                                icon = Icons.Default.Search,
                                contentDescription = "Search",
                                onClick = { searchOpen = !searchOpen },
                                containerColor = scheme.surfaceVariant,
                                elevated = false
                            )
                            Box {
                                CircleIconButton(
                                    icon = Icons.Default.FilterList,
                                    contentDescription = "Sort options",
                                    onClick = { sortMenuExpanded = true },
                                    containerColor = scheme.surfaceVariant,
                                    elevated = false
                                )
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
                    }
                }

                if (searchOpen || searchQuery.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
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
                }

                item {
                    val tiles = listOf(
                        Triple("contracts", "Contracts", documents.count { it.category == DocumentCategory.CONTRACT || it.category == DocumentCategory.LEGAL }),
                        Triple("invoices", "Invoices", documents.count { it.category == DocumentCategory.INVOICE }),
                        Triple("ids", "IDs and forms", documents.count { it.category == DocumentCategory.ID_PASSPORT }),
                        Triple("unfiled", "Unfiled", documents.count { it.folderId !in folderById })
                    )
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        tiles.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { (key, label, count) ->
                                    CategoryTile(
                                        label = label,
                                        count = count,
                                        selected = tileFilter == key,
                                        onClick = { tileFilter = if (tileFilter == key) null else key },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                    SectionHeader(
                        title = "Documents",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        if (tileFilter != null) {
                            PillChip(
                                selected = true,
                                onClick = { tileFilter = null },
                                label = { Text("Clear filter  ✕") }
                            )
                        }
                    }
                }

                item {
                    VaultCard(modifier = Modifier.padding(horizontal = 20.dp)) {
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

                item {
                    OverviewCard(
                        piiRedacted = totalPiiRedacted,
                        flagged = flaggedCount,
                        analyzed = analyzedCount,
                        queued = queuedCount,
                        total = documents.size,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                item {
                    HybridEngineCard(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        nanoState = nanoState,
                        isOnline = isOnline,
                        activeRoute = activeRoute
                    )
                }

            }

            // Soft fade so cards slide out from under the floating action area.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, sheetColor))
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
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

/** Small pill in the header: primary dot when online, muted dot when offline. */
@Composable
private fun ConnectionPill(isOnline: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = scheme.surface,
        contentColor = scheme.onSurface,
        border = BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .height(32.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) scheme.primary else scheme.onSurfaceVariant)
            )
            Text(
                text = if (isOnline) "Online" else "Offline",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

/**
 * Brand hero: violet-black card with the same teal and cyan glow as the web app's dark background.
 * It stays dark in both themes. The three bars bottom right echo the logo (middle line redacted).
 */
@Composable
private fun VaultHeroCard(documentCount: Int) {
    val shape = MaterialTheme.shapes.extraLarge
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(shape)
            .background(BrandNavy)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(BrandCyan.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(size.width * 0.10f, 0f),
                        radius = size.width * 0.80f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(BrandCyan.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width, size.height * 0.10f),
                        radius = size.width * 0.60f
                    )
                )
                val ring = BrandCyan.copy(alpha = 0.16f)
                listOf(0.52f, 0.62f, 0.72f).forEachIndexed { i, r ->
                    drawCircle(
                        color = ring,
                        radius = size.height * r,
                        center = Offset(size.width * (0.70f + i * 0.04f), size.height * 0.42f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            .border(1.dp, Color.White.copy(alpha = 0.10f), shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = BrandCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "PRIVATE VAULT",
                    color = BrandInk.copy(alpha = 0.75f),
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
                Column(
                    modifier = Modifier.padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(BrandInk.copy(alpha = 0.45f))
                    )
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandInk)
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(BrandInk.copy(alpha = 0.45f))
                    )
                }
            }
        }
    }
}

/** Summary card: redaction totals, risk count and analysis progress. */
@Composable
private fun OverviewCard(
    piiRedacted: Int,
    flagged: Int,
    analyzed: Int,
    queued: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val progress = if (total == 0) 0f else analyzed.toFloat() / total
    VaultCard(modifier = modifier) {
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
                        .background(scheme.outlineVariant)
                )
                OverviewStat(
                    value = flagged.toString(),
                    label = "Documents with risks",
                    modifier = Modifier.weight(1f),
                    valueColor = if (flagged > 0) scheme.error else scheme.onSurface
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
                        .background(scheme.primary)
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
private fun OverviewStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp),
            color = valueColor
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
            val riskCount = doc.redFlagsAndRisks.size
            Text(
                text = when {
                    queued -> "Queued"
                    riskCount == 1 -> "1 risk"
                    riskCount > 1 -> "$riskCount risks"
                    else -> "Analyzed"
                },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = when {
                    queued -> scheme.onSurfaceVariant
                    riskCount > 0 -> scheme.error
                    else -> scheme.onSurface
                }
            )
        }
    }
}

/** Recent document card in the header carousel. The first card is darker and carries the white pill. */
@Composable
private fun RecentDocCard(
    title: String,
    subtitle: String,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(240.dp)
            .height(176.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = if (highlighted) Color(0xFF9DBBD0) else scheme.surface.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (highlighted) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = Color(0xFFE3EAEF)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        PreviewBar(width = 96.dp, color = Color(0xFFB6C5D0))
                        PreviewBar(width = 96.dp, color = scheme.primary)
                        PreviewBar(width = 52.dp, color = Color(0xFFB6C5D0))
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE3EAEF)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = scheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(scheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.NorthEast,
                                contentDescription = null,
                                tint = scheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(1.dp))
                Column(modifier = Modifier.padding(start = 8.dp, bottom = 10.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewBar(width: androidx.compose.ui.unit.Dp, color: Color) {
    Box(
        modifier = Modifier
            .width(width)
            .height(6.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/** Category tile in the 2x2 grid: label, document count and an arrow. Navy when selected. */
@Composable
private fun CategoryTile(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(148.dp)
            .softShadow(shape),
        shape = shape,
        color = if (selected) scheme.primary else scheme.surface,
        contentColor = if (selected) scheme.onPrimary else scheme.onSurface,
        border = if (selected) null else BorderStroke(1.dp, scheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp)
                    )
                    Text(
                        text = if (count == 1) "document" else "documents",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) scheme.onPrimary.copy(alpha = 0.75f) else scheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (selected) scheme.primaryContainer else scheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.NorthEast,
                        contentDescription = null,
                        tint = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
