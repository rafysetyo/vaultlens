package com.vaultlens.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vaultlens.app.data.model.AnalysisStatus
import com.vaultlens.app.data.model.DocumentCategory
import com.vaultlens.app.data.model.InferenceRoute
import com.vaultlens.app.data.model.VaultDocument
import com.vaultlens.app.data.model.VaultFolder
import com.vaultlens.app.ui.components.HybridPrivacyBadge
import com.vaultlens.app.ui.components.VaultLogo
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

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenScanner,
                icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null) },
                text = { Text("Scan or Paste Document") },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VaultLogo(size = 32.dp)
                    Text(
                        text = "VaultLens",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
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
                    StatCard(
                        label = "Documents Protected",
                        value = documents.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "PII Items Redacted Locally",
                        value = totalPiiRedacted.toString(),
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
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    placeholder = { Text("Search by title, folder, tags, or content...") },
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    shape = RoundedCornerShape(50),
                                    selected = selectedFolderFilter == "all",
                                    onClick = { selectedFolderFilter = "all" },
                                    label = { Text("All Folders (${documents.size})") }
                                )
                            }
                            item {
                                val unfiledCount = documents.count { it.folderId !in folderById }
                                FilterChip(
                                    shape = RoundedCornerShape(50),
                                    selected = selectedFolderFilter == "unfiled",
                                    onClick = { selectedFolderFilter = "unfiled" },
                                    label = { Text("Unfiled ($unfiledCount)") }
                                )
                            }
                            items(folders, key = { it.id }) { folder ->
                                val count = documents.count { it.folderId == folder.id }
                                FilterChip(
                                    shape = RoundedCornerShape(50),
                                    selected = selectedFolderFilter == folder.id,
                                    onClick = { selectedFolderFilter = folder.id },
                                    leadingIcon = {
                                        Icon(Icons.Default.Folder, contentDescription = null)
                                    },
                                    trailingIcon = if (selectedFolderFilter == folder.id) {
                                        {
                                            IconButton(onClick = { onDeleteFolder(folder.id) }) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Delete ${folder.name}"
                                                )
                                            }
                                        }
                                    } else null,
                                    label = { Text("${folder.name} ($count)") }
                                )
                            }
                        }

                        IconButton(onClick = { isCreatingFolder = !isCreatingFolder }) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder")
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
                                label = { Text("New folder name") }
                            )
                            OutlinedButton(
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
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                shape = RoundedCornerShape(50),
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All") }
                            )
                        }
                        items(DocumentCategory.entries) { category ->
                            FilterChip(
                                shape = RoundedCornerShape(50),
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category.label) }
                            )
                        }
                    }

                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        OutlinedButton(onClick = { sortMenuExpanded = true }) {
                            Text("Sort: ${sortOption.label}")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Sort options")
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

            if (selectedDocuments.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedDocuments.size} selected",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                TextButton(onClick = { selectedIds = emptySet() }) {
                                    Text("Clear")
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box {
                                    OutlinedButton(onClick = { moveMenuExpanded = true }) {
                                        Icon(Icons.Default.DriveFileMove, contentDescription = null)
                                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                                        Text("Move")
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
                                OutlinedButton(
                                    onClick = { bulkExportLauncher.launch("vaultlens-bulk-export.json") }
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                                    Text("Export")
                                }
                                OutlinedButton(
                                    onClick = {
                                        onDeleteDocuments(selectedDocuments.map { it.id })
                                        selectedIds = emptySet()
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }

            if (filteredDocs.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "No matching documents",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try clearing your search query or category filter.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(filteredDocs, key = { it.id }) { doc ->
                val isSelected = doc.id in selectedIds
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectDocument(doc) },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                selectedIds = if (checked) selectedIds + doc.id else selectedIds - doc.id
                            }
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                            Text(
                                text = doc.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val folderLabel = folderById[doc.folderId]?.name
                            Text(
                                text = buildString {
                                    if (folderLabel != null) {
                                        append(folderLabel)
                                        append(" · ")
                                    }
                                    append(doc.category.label)
                                    append(" · ")
                                    append("${doc.piiRedactedCount} PII redacted · ")
                                    append(
                                        if (doc.analysisStatus == AnalysisStatus.QUEUED_OFFLINE) "Queued Offline"
                                        else "Cloud Insight Ready"
                                    )
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (doc.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = doc.tags.joinToString(" · "),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
