package com.vaultlens.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
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
import com.vaultlens.app.ui.components.CircleIconButton
import com.vaultlens.app.ui.components.HybridPrivacyBadge
import com.vaultlens.app.ui.components.PillChip
import com.vaultlens.app.ui.components.VaultLogo
import com.vaultlens.app.ui.components.vaultFieldColors
import com.vaultlens.app.ui.theme.Emerald600
import com.vaultlens.app.ui.theme.Ink
import com.vaultlens.app.ui.theme.Violet
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 128.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        VaultLogo(size = 42.dp)
                        Column {
                            Text(
                                text = "Your private vault",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant
                            )
                            Text(
                                text = "VaultLens",
                                style = MaterialTheme.typography.headlineMedium,
                                color = scheme.onBackground
                            )
                        }
                    }
                }

                item { VaultHeroCard(documentCount = documents.size) }

                item {
                    HybridPrivacyBadge(
                        route = activeRoute,
                        piiSanitizedCount = totalPiiRedacted,
                        isOnline = isOnline
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatTile(
                            label = "PII redacted",
                            value = totalPiiRedacted.toString(),
                            caption = "removed locally",
                            icon = Icons.Default.Lock,
                            gradient = listOf(Color(0xFFD3E3FB), Color(0xFF9EC4F3)),
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            label = "Risks flagged",
                            value = flaggedCount.toString(),
                            caption = "documents to review",
                            icon = Icons.Default.Warning,
                            gradient = listOf(Color(0xFFFBD5DA), Color(0xFFF59CAA)),
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                            Icon(Icons.Default.Search, contentDescription = "Search documents")
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
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionLabel("Folders")
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                PillChip(
                                    selected = selectedFolderFilter == "all",
                                    onClick = { selectedFolderFilter = "all" },
                                    label = { Text("All (${documents.size})") }
                                )
                            }
                            item {
                                val unfiledCount = documents.count { it.folderId !in folderById }
                                PillChip(
                                    selected = selectedFolderFilter == "unfiled",
                                    onClick = { selectedFolderFilter = "unfiled" },
                                    label = { Text("Unfiled ($unfiledCount)") }
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
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = if (isActive) {
                                        {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Delete ${folder.name}",
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .clickable { onDeleteFolder(folder.id) }
                                            )
                                        }
                                    } else null,
                                    label = { Text("${folder.name} ($count)") }
                                )
                            }
                            item {
                                com.vaultlens.app.ui.components.NewFolderCircleButton(
                                    onClick = { isCreatingFolder = !isCreatingFolder }
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
                                        onCreateFolder(newFolderName.trim(), "indigo")
                                        newFolderName = ""
                                        isCreatingFolder = false
                                    }
                                ) {
                                    Text("Create")
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Documents (${filteredDocs.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = scheme.onBackground
                        )
                        Box {
                            Surface(
                                onClick = { sortMenuExpanded = true },
                                shape = CircleShape,
                                color = scheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sortOption.label,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = scheme.onSurface
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

                item {
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

                if (filteredDocs.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = scheme.surface
                        ) {
                            Column(modifier = Modifier.padding(22.dp)) {
                                Text(
                                    text = "No matching documents",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = scheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try clearing your search query or category filter.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(filteredDocs, key = { it.id }) { doc ->
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
            }

            // Soft fade so cards slide out from under the floating action area.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, scheme.background))
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            ) {
                if (selectedDocuments.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = scheme.primary,
                        contentColor = scheme.onPrimary,
                        shadowElevation = 10.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedDocuments.size} selected",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Box {
                                IconButton(onClick = { moveMenuExpanded = true }) {
                                    Icon(Icons.Default.DriveFileMove, contentDescription = "Move")
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
                        shadowElevation = 10.dp
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Scan or Paste Document",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
    )
}

/** Purple gradient "card" at the top, in the spirit of the credit-card hero. */
@Composable
private fun VaultHeroCard(documentCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(188.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFC9B8E6),
                        Color(0xFF9E80C9),
                        Color(0xFF6B4F91),
                        Color(0xFF45325F)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .drawBehind {
                val ring = Color.White.copy(alpha = 0.22f)
                drawCircle(
                    color = ring,
                    radius = size.height * 0.62f,
                    center = Offset(size.width * 0.80f, size.height * 0.05f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = ring,
                    radius = size.height * 0.85f,
                    center = Offset(size.width * 0.86f, size.height * 0.10f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height * 0.1f),
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.2f, size.height * 0.1f)
                )
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
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "PRIVATE",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
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
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "documents protected",
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "vault",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    caption: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(gradient, start = Offset.Zero, end = Offset.Infinite))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(14.dp))
                }
                Text(text = label, fontSize = 13.sp, color = Ink.copy(alpha = 0.75f), maxLines = 1)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = value,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(text = caption, fontSize = 12.sp, color = Ink.copy(alpha = 0.6f), maxLines = 1)
        }
    }
}

private fun categoryIcon(category: DocumentCategory): ImageVector = when (category) {
    DocumentCategory.CONTRACT -> Icons.Default.Description
    DocumentCategory.ID_PASSPORT -> Icons.Default.Badge
    DocumentCategory.INVOICE -> Icons.Default.Receipt
    DocumentCategory.LEGAL -> Icons.Default.Gavel
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DocumentRow(
    doc: VaultDocument,
    folderName: String?,
    selected: Boolean,
    onOpen: () -> Unit,
    onToggleSelect: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = scheme.surface,
        border = if (selected) BorderStroke(1.5.dp, scheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tap the round icon to select (turns into a check mark).
                Surface(
                    onClick = onToggleSelect,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = if (selected) scheme.primary else scheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (selected) Icons.Default.Check else categoryIcon(doc.category),
                            contentDescription = if (selected) "Selected" else "Select document",
                            tint = if (selected) scheme.onPrimary else scheme.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusPill(
                    text = "${doc.piiRedactedCount} PII redacted",
                    background = scheme.primaryContainer,
                    foreground = scheme.tertiary
                )
                val queued = doc.analysisStatus == AnalysisStatus.QUEUED_OFFLINE
                StatusPill(
                    text = if (queued) "Queued Offline" else "Cloud Insight Ready",
                    background = (if (queued) Color(0xFFF59E0B) else Emerald600).copy(alpha = 0.14f),
                    foreground = if (queued) Color(0xFFB45309) else Emerald600
                )
            }

            if (doc.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = doc.tags.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StatusPill(text: String, background: Color, foreground: Color) {
    Surface(shape = CircleShape, color = background) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = foreground,
            maxLines = 1
        )
    }
}
