package com.vaultlens.app.data.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.vaultlens.app.data.hybrid.HybridPrivacyRouter
import com.vaultlens.app.data.model.AnalysisStatus
import com.vaultlens.app.data.model.DocumentCategory
import com.vaultlens.app.data.model.VaultDocument
import com.vaultlens.app.data.model.VaultFolder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Offline-first Firestore repository storing ONLY sanitized documents and summaries.
 * Queued documents marked QUEUED_OFFLINE automatically trigger cloud analysis upon reconnection.
 */
class VaultRepository(
    private val router: HybridPrivacyRouter = HybridPrivacyRouter()
) {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    private fun documentsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("documents")

    private fun foldersCollection(userId: String) =
        firestore.collection("users").document(userId).collection("folders")

    fun observeDocuments(): Flow<List<VaultDocument>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = documentsCollection(userId)
            .orderBy("createdAtMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val docs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(VaultDocument::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(docs)
            }

        awaitClose { registration.remove() }
    }

    suspend fun saveSanitizedDocument(doc: VaultDocument) {
        val userId = auth.currentUser?.uid ?: return
        documentsCollection(userId).document(doc.id).set(doc.copy(ownerId = userId)).await()
    }

    suspend fun processOfflineQueue() {
        val userId = auth.currentUser?.uid ?: return
        val queuedSnapshot = documentsCollection(userId)
            .whereEqualTo("analysisStatus", AnalysisStatus.QUEUED_OFFLINE.name)
            .get()
            .await()

        for (snapshot in queuedSnapshot.documents) {
            val doc = snapshot.toObject(VaultDocument::class.java) ?: continue
            val insight = router.analyzeSanitizedDocument(
                sanitizedText = doc.sanitizedText,
                category = doc.category
            )
            val mergedTags = (doc.tags + insight.suggestedTags)
                .map { it.trim().take(40) }
                .filter { it.isNotEmpty() }
                .distinct()
                .take(10)
            snapshot.reference.update(
                mapOf(
                    "analysisStatus" to AnalysisStatus.ANALYZED.name,
                    "documentSummary" to insight.documentSummary,
                    "keyDatesAndDeadlines" to insight.keyDatesAndDeadlines,
                    "financialAmounts" to insight.financialAmounts,
                    "redFlagsAndRisks" to insight.redFlagsAndRisks,
                    "tags" to mergedTags
                )
            ).await()
        }
    }

    suspend fun deleteDocuments(docIds: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        if (docIds.isEmpty()) return
        val batch = firestore.batch()
        docIds.forEach { id ->
            batch.delete(documentsCollection(userId).document(id))
        }
        batch.commit().await()
    }

    fun observeFolders(): Flow<List<VaultFolder>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = foldersCollection(userId)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(VaultFolder::class.java)?.copy(id = doc.id)
                }.orEmpty()
                trySend(list)
            }

        awaitClose { registration.remove() }
    }

    suspend fun createFolder(folder: VaultFolder) {
        val userId = auth.currentUser?.uid ?: return
        foldersCollection(userId).document(folder.id).set(folder.copy(ownerId = userId)).await()
    }

    suspend fun moveDocumentsToFolder(docIds: List<String>, folderId: String) {
        val userId = auth.currentUser?.uid ?: return
        if (docIds.isEmpty()) return
        val cleanFolderId = folderId.trim().take(128)
        val batch = firestore.batch()
        docIds.forEach { id ->
            batch.update(documentsCollection(userId).document(id), "folderId", cleanFolderId)
        }
        batch.commit().await()
    }

    suspend fun deleteFolder(folderId: String, docIdsInFolder: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        val batch = firestore.batch()
        docIdsInFolder.forEach { id ->
            batch.update(documentsCollection(userId).document(id), "folderId", "")
        }
        batch.delete(foldersCollection(userId).document(folderId))
        batch.commit().await()
    }
}
