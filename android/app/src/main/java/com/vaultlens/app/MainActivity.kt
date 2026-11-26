package com.vaultlens.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.vaultlens.app.data.hybrid.HybridPrivacyRouter
import com.vaultlens.app.data.model.AnalysisStatus
import com.vaultlens.app.data.model.DocumentCategory
import com.vaultlens.app.data.model.InferenceRoute
import com.vaultlens.app.data.model.RedactionEntry
import com.vaultlens.app.data.model.VaultDocument
import com.vaultlens.app.data.model.VaultFolder
import com.vaultlens.app.data.network.NetworkMonitor
import com.vaultlens.app.data.repository.VaultRepository
import com.vaultlens.app.ui.screens.DocumentDetailScreen
import com.vaultlens.app.ui.screens.SanitizerAnalyzerScreen
import com.vaultlens.app.ui.screens.VaultDashboardScreen
import com.vaultlens.app.ui.theme.VaultLensTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val router = HybridPrivacyRouter()
        val repository = VaultRepository(router)
        val networkMonitor = NetworkMonitor(applicationContext)

        setContent {
            VaultLensTheme {
                val scope = rememberCoroutineScope()
                val activeRoute by router.activeRoute.collectAsState()
                val isOnline by networkMonitor.isOnline.collectAsState()
                var authReady by remember { mutableStateOf(Firebase.auth.currentUser != null) }

                DisposableEffect(Unit) {
                    onDispose { networkMonitor.unregister() }
                }

                // Make sure Gemini Nano is downloaded before the first scan.
                LaunchedEffect(Unit) { router.prepareOnDeviceModel() }

                val folders = remember {
                    mutableStateListOf(
                        VaultFolder(
                            id = "folder_client_contracts",
                            name = "Client Contracts",
                            color = "indigo"
                        ),
                        VaultFolder(
                            id = "folder_medical_billing",
                            name = "Medical & Billing",
                            color = "emerald"
                        )
                    )
                }
                val documents = remember {
                    mutableStateListOf(
                        VaultDocument(
                            id = "sample_contract_01",
                            title = "Freelance Software Architecture Agreement",
                            category = DocumentCategory.CONTRACT,
                            folderId = "folder_client_contracts",
                            sanitizedText = "Agreement between [REDACTED_NAME] and [REDACTED_NAME], SSN: [REDACTED_SSN]...",
                            piiRedactedCount = 7,
                            redactionLog = listOf(
                                RedactionEntry("NAME", "[REDACTED_NAME]", "Gemini Nano (On-Device)"),
                                RedactionEntry("SSN", "[REDACTED_SSN]", "Gemini Nano (On-Device)")
                            ),
                            inferenceRoute = InferenceRoute.ON_DEVICE_NANO,
                            analysisStatus = AnalysisStatus.ANALYZED,
                            documentSummary = "Fixed-milestone consulting contract with all personal identifiers scrubbed locally.",
                            keyDatesAndDeadlines = listOf("Deliverable Deadline: Dec 20, 2026", "Net-60 Payment"),
                            financialAmounts = listOf("$18,500.00 milestone fee", "$4,200/mo auto-renewal"),
                            redFlagsAndRisks = listOf("Unlimited personal indemnification clause"),
                            tags = listOf("Auto-Renewal", "Liability Risk", "Payment Terms", "IP Rights")
                        ),
                        VaultDocument(
                            id = "sample_invoice_02",
                            title = "Oakwood Medical Lab Invoice #INV-23-45812",
                            category = DocumentCategory.INVOICE,
                            folderId = "folder_medical_billing",
                            sanitizedText = "Patient: [REDACTED_NAME], Patient ID: [REDACTED_ID], Total Due: $155.00",
                            piiRedactedCount = 6,
                            redactionLog = listOf(
                                RedactionEntry("NAME", "[REDACTED_NAME]", "Gemini Nano (On-Device)"),
                                RedactionEntry("ID", "[REDACTED_ID]", "Gemini Nano (On-Device)")
                            ),
                            inferenceRoute = InferenceRoute.CLOUD_FALLBACK_FLASH,
                            analysisStatus = AnalysisStatus.ANALYZED,
                            documentSummary = "Diagnostic laboratory invoice with patient demographics redacted on-device.",
                            keyDatesAndDeadlines = listOf("Payment Due: Nov 26, 2026"),
                            financialAmounts = listOf("$335.00 subtotal", "$155.00 net due"),
                            redFlagsAndRisks = listOf("Pending -$180.00 insurer adjustment before collections window"),
                            tags = listOf("Healthcare Billing", "Payment Terms", "Late Penalty", "ID Scrubbed")
                        )
                    )
                }

                LaunchedEffect(Unit) {
                    if (Firebase.auth.currentUser == null) {
                        runCatching { Firebase.auth.signInAnonymously().await() }
                    }
                    authReady = Firebase.auth.currentUser != null
                }

                LaunchedEffect(authReady) {
                    if (!authReady) return@LaunchedEffect
                    launch {
                        repository.observeFolders().collectLatest { cloudFolders ->
                            if (cloudFolders.isNotEmpty()) {
                                folders.clear()
                                folders.addAll(cloudFolders)
                            }
                        }
                    }
                    launch {
                        repository.observeDocuments().collectLatest { cloudDocs ->
                            if (cloudDocs.isNotEmpty()) {
                                documents.clear()
                                documents.addAll(cloudDocs)
                            } else {
                                folders.forEach { folder -> repository.createFolder(folder) }
                                documents.forEach { doc -> repository.saveSanitizedDocument(doc) }
                            }
                        }
                    }
                }

                LaunchedEffect(isOnline, documents.size) {
                    if (isOnline && documents.any { it.analysisStatus == AnalysisStatus.QUEUED_OFFLINE }) {
                        runCatching { repository.processOfflineQueue() }
                    }
                }

                var currentScreen by remember { mutableStateOf("dashboard") }
                var selectedDocument by remember { mutableStateOf<VaultDocument?>(null) }

                when (currentScreen) {
                    "dashboard" -> VaultDashboardScreen(
                        documents = documents,
                        folders = folders,
                        activeRoute = activeRoute,
                        isOnline = isOnline,
                        onOpenScanner = { currentScreen = "scanner" },
                        onSelectDocument = { doc ->
                            selectedDocument = doc
                            currentScreen = "detail"
                        },
                        onDeleteDocuments = { ids ->
                            val idSet = ids.toSet()
                            documents.removeAll { it.id in idSet }
                            scope.launch { repository.deleteDocuments(ids) }
                        },
                        onCreateFolder = { name, color ->
                            val cleanName = name.trim().take(60)
                            if (cleanName.isNotEmpty()) {
                                val newFolder = VaultFolder(
                                    id = "folder_${UUID.randomUUID().toString().take(8)}",
                                    name = cleanName,
                                    color = color
                                )
                                folders.add(newFolder)
                                scope.launch { repository.createFolder(newFolder) }
                            }
                        },
                        onDeleteFolder = { folderId ->
                            val affectedIds = documents.filter { it.folderId == folderId }.map { it.id }
                            for (i in documents.indices) {
                                if (documents[i].folderId == folderId) {
                                    documents[i] = documents[i].copy(folderId = "")
                                }
                            }
                            folders.removeAll { it.id == folderId }
                            scope.launch { repository.deleteFolder(folderId, affectedIds) }
                        },
                        onMoveDocumentsToFolder = { docIds, folderId ->
                            val idSet = docIds.toSet()
                            for (i in documents.indices) {
                                if (documents[i].id in idSet) {
                                    documents[i] = documents[i].copy(folderId = folderId)
                                }
                            }
                            scope.launch { repository.moveDocumentsToFolder(docIds, folderId) }
                        }
                    )

                    "scanner" -> SanitizerAnalyzerScreen(
                        router = router,
                        activeRoute = activeRoute,
                        isOnline = isOnline,
                        folders = folders,
                        onBack = { currentScreen = "dashboard" },
                        onDocumentAnalyzed = { newDoc ->
                            documents.add(0, newDoc)
                            scope.launch { repository.saveSanitizedDocument(newDoc) }
                            selectedDocument = newDoc
                            currentScreen = "detail"
                        }
                    )

                    "detail" -> selectedDocument?.let { doc ->
                        val liveDoc = documents.find { it.id == doc.id } ?: doc
                        DocumentDetailScreen(
                            document = liveDoc,
                            folders = folders,
                            isOnline = isOnline,
                            onBack = { currentScreen = "dashboard" },
                            onChangeFolder = { docId, folderId ->
                                for (i in documents.indices) {
                                    if (documents[i].id == docId) {
                                        documents[i] = documents[i].copy(folderId = folderId)
                                    }
                                }
                                scope.launch { repository.moveDocumentsToFolder(listOf(docId), folderId) }
                            }
                        )
                    }
                }
            }
        }
    }
}
