package com.vaultlens.app.data.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * Runs 100% on-device OCR via ML Kit Text Recognition before any text enters PII redaction.
 */
class LocalOcrProcessor(private val context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromUri(imageUri: Uri): String {
        val image = InputImage.fromFilePath(context, imageUri)
        val visionText = recognizer.process(image).await()
        return visionText.text.trim()
    }

    suspend fun extractTextFromBitmap(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        val visionText = recognizer.process(image).await()
        return visionText.text.trim()
    }
}
