package com.vaultlens.app.data.hybrid

import com.google.firebase.Firebase
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.ai
import com.google.firebase.ai.ondevice.FirebaseAIOnDevice
import com.google.firebase.ai.ondevice.OnDeviceModelStatus
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.vaultlens.app.data.model.CloudInsight
import com.vaultlens.app.data.model.DocumentCategory
import com.vaultlens.app.data.model.InferenceRoute
import com.vaultlens.app.data.model.RedactionResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Hybrid AI routing through the Firebase AI Logic SDK, built around one rule:
 * raw document text never leaves the device.
 *
 * Step 1 (raw text): name detection runs with [InferenceMode.ONLY_ON_DEVICE]. If Gemini Nano is not
 * available the SDK throws instead of silently falling back to the cloud, and we fall back to the
 * deterministic on-device rules ([LocalPiiSanitizer]) only.
 *
 * Step 2 (sanitized text only): deep structured analysis goes to the cloud model. The text is
 * re-checked by the local sanitizer right before sending.
 */
@OptIn(PublicPreviewAPI::class)
class HybridPrivacyRouter {

    private val _activeRoute = MutableStateFlow(InferenceRoute.ON_DEVICE_NANO)
    val activeRoute: StateFlow<InferenceRoute> = _activeRoute.asStateFlow()

    private val _onDeviceStatus = MutableStateFlow(OnDeviceModelStatus.UNAVAILABLE)
    val onDeviceStatus: StateFlow<OnDeviceModelStatus> = _onDeviceStatus.asStateFlow()

    private val ai = Firebase.ai(backend = GenerativeBackend.googleAI())

    // ONLY_ON_DEVICE: throws when Nano is missing, so raw text can never reach the cloud model.
    // The cloud model name is only the SDK's required argument; this model never calls it.
    private val nanoNameModel = ai.generativeModel(
        modelName = CLOUD_MODEL,
        onDeviceConfig = OnDeviceConfig(
            mode = InferenceMode.ONLY_ON_DEVICE,
            maxOutputTokens = 256
        )
    )

    // Cloud only ever receives text that already went through LocalPiiSanitizer.
    private val cloudInsightModel = ai.generativeModel(
        modelName = CLOUD_MODEL,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = Schema.obj(
                mapOf(
                    "documentSummary" to Schema.string(),
                    "keyDatesAndDeadlines" to Schema.array(Schema.string()),
                    "financialAmounts" to Schema.array(Schema.string()),
                    "redFlagsAndRisks" to Schema.array(Schema.string()),
                    "suggestedTags" to Schema.array(Schema.string())
                )
            )
        }
    )

    /**
     * Checks whether Gemini Nano is ready and downloads it if needed. The SDK does not download the
     * model by itself, so call this early in the app lifecycle (MainActivity does).
     */
    @Suppress("DEPRECATION")
    suspend fun prepareOnDeviceModel(): OnDeviceModelStatus {
        val status = try {
            var current = FirebaseAIOnDevice.checkStatus()
            if (current == OnDeviceModelStatus.DOWNLOADABLE) {
                FirebaseAIOnDevice.download().collect { }
                current = FirebaseAIOnDevice.checkStatus()
            }
            current
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            OnDeviceModelStatus.UNAVAILABLE
        }
        _onDeviceStatus.value = status
        return status
    }

    suspend fun detectAndRedactLocally(
        rawText: String,
        preferOnDeviceNano: Boolean = true
    ): RedactionResult {
        val nanoNames: List<String>? = if (preferOnDeviceNano) detectNamesWithNano(rawText) else null

        return if (nanoNames != null) {
            _activeRoute.value = InferenceRoute.ON_DEVICE_NANO
            LocalPiiSanitizer.redactDeterministic(
                rawText = rawText,
                extraDetectedNames = nanoNames,
                engineLabel = "Gemini Nano (On-Device)"
            )
        } else {
            _activeRoute.value = InferenceRoute.ON_DEVICE_RULES
            LocalPiiSanitizer.redactDeterministic(
                rawText = rawText,
                engineLabel = "On-device rules (Nano unavailable)"
            )
        }
    }

    /** Returns detected names, or null when Gemini Nano could not run (nothing was sent anywhere). */
    private suspend fun detectNamesWithNano(rawText: String): List<String>? {
        val response = try {
            nanoNameModel.generateContent(
                "Extract only full person names present in this text. " +
                    "Reply with JSON only, shaped like {\"names\": [\"...\"]}. Text:\n$rawText"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
        return parseNames(response.text, rawText)
    }

    private fun parseNames(modelOutput: String?, sourceText: String): List<String> = try {
        val body = Regex("""\{[\s\S]*\}""").find(modelOutput.orEmpty())?.value
        val array = body?.let { JSONObject(it).optJSONArray("names") }
        buildList {
            if (array != null) {
                for (i in 0 until array.length()) add(array.optString(i).trim())
            }
        }.filter { it.length > 3 && sourceText.contains(it) }.distinct()
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun analyzeSanitizedDocument(
        sanitizedText: String,
        category: DocumentCategory
    ): CloudInsight {
        _activeRoute.value = InferenceRoute.CLOUD_FALLBACK_FLASH

        // Defense in depth: scrub once more so a stale or hand-edited string can never leak raw PII.
        val safeText = LocalPiiSanitizer.redactDeterministic(
            rawText = sanitizedText,
            engineLabel = "Pre-cloud recheck"
        ).sanitizedText

        val prompt = """
            Analyze this pre-sanitized ${category.label} document.
            Keep redacted tokens like [REDACTED_NAME] intact.
            Return documentSummary, keyDatesAndDeadlines, financialAmounts, redFlagsAndRisks,
            and suggestedTags (up to 5 concise 1-3 word topical/risk tags with zero PII).

            $safeText
        """.trimIndent()

        val response = cloudInsightModel.generateContent(prompt)
        val json = JSONObject(response.text ?: "{}")

        fun JSONObject.stringList(key: String): List<String> {
            val arr = optJSONArray(key) ?: return emptyList()
            return buildList {
                for (i in 0 until arr.length()) {
                    add(arr.optString(i))
                }
            }
        }

        val parsedTags = json.stringList("suggestedTags")
            .map { it.trim().take(40) }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(5)

        return CloudInsight(
            documentSummary = json.optString("documentSummary", "Analysis completed."),
            keyDatesAndDeadlines = json.stringList("keyDatesAndDeadlines"),
            financialAmounts = json.stringList("financialAmounts"),
            redFlagsAndRisks = json.stringList("redFlagsAndRisks"),
            suggestedTags = parsedTags
        )
    }

    private companion object {
        const val CLOUD_MODEL = "gemini-2.5-flash"
    }
}
