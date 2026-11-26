package com.vaultlens.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultlens.app.data.model.AnalysisStatus
import com.vaultlens.app.data.model.InferenceRoute
import com.vaultlens.app.data.model.VaultDocument

/** UI-side view of the Gemini Nano model status reported by Firebase AI Logic. */
enum class NanoState(val label: String, val detail: String) {
    CHECKING("Checking", "Asking Firebase AI Logic for model status"),
    READY("Ready", "Runs on this phone, works offline"),
    DOWNLOADING("Downloading", "Model download in progress, rules cover it meanwhile"),
    DOWNLOADABLE("Not downloaded", "Download pending, rules cover it meanwhile"),
    UNAVAILABLE("Not supported", "This device has no Nano, rules run locally instead")
}

private const val CLOUD_MODEL_LABEL = "Gemini 3.8 Flash"

/**
 * Dashboard card that explains the hybrid setup at a glance: which model runs on the phone,
 * which runs in the cloud, what each one is allowed to see, and the Firebase stack underneath.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HybridEngineCard(
    nanoState: NanoState,
    isOnline: Boolean,
    activeRoute: InferenceRoute,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    VaultCard(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionCaption(text = "Hybrid inference")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Raw text stays on-device",
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurface
                    )
                }
                MonoTag(
                    text = if (activeRoute == InferenceRoute.CLOUD_FALLBACK_FLASH) "Last: cloud" else "Last: on-device",
                    inverted = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            EngineLane(
                icon = Icons.Default.PhoneAndroid,
                title = "On-device · Gemini Nano",
                status = nanoState.label,
                statusActive = nanoState == NanoState.READY,
                detail = "Finds names in raw text. ${nanoState.detail}."
            )
            HairlineDivider(modifier = Modifier.padding(vertical = 12.dp))
            EngineLane(
                icon = if (isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                title = "Cloud · $CLOUD_MODEL_LABEL",
                status = if (isOnline) "Online" else "Offline",
                statusActive = isOnline,
                detail = if (isOnline) {
                    "Deep analysis on sanitized text only."
                } else {
                    "Scans queue in Firestore and analyze on reconnect."
                }
            )

            Spacer(modifier = Modifier.height(14.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Firebase AI Logic", "App Check", "Auth", "Firestore").forEach { name ->
                    MonoTag(text = name)
                }
            }
        }
    }
}

@Composable
private fun EngineLane(
    icon: ImageVector,
    title: String,
    status: String,
    statusActive: Boolean,
    detail: String
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconBubble(icon = icon, size = 40.dp, filled = statusActive)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                StatusDot(text = status, active = statusActive)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusDot(text: String, active: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val tone = if (active) scheme.onSurface else scheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (active) scheme.primary else scheme.outline)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = tone,
            maxLines = 1
        )
    }
}

/** Where the scanner currently is in the capture, redact, analyze flow. */
enum class PipelineStage { CAPTURE, REDACT, ANALYZE, DONE }

/**
 * Three-step stepper for the scanner. Each step names the engine that runs it and where it runs,
 * so the on-device / cloud split is visible while the user works.
 */
@Composable
fun HybridPipeline(
    stage: PipelineStage,
    nanoState: NanoState,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val redactEngine = if (nanoState == NanoState.READY) "Gemini Nano" else "Local rules"
    val steps = listOf(
        PipelineStepInfo(Icons.Default.DocumentScanner, "Capture", "ML Kit OCR", "On-device"),
        PipelineStepInfo(Icons.Default.Shield, "Redact", redactEngine, "On-device"),
        PipelineStepInfo(
            Icons.Default.Cloud,
            "Analyze",
            "Gemini Flash",
            if (isOnline) "Cloud · sanitized" else "Queued offline"
        )
    )
    val activeIndex = stage.ordinal

    VaultCard(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionCaption(text = "Hybrid pipeline")
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                steps.forEachIndexed { index, step ->
                    PipelineStep(
                        info = step,
                        number = index + 1,
                        done = index < activeIndex,
                        current = index == activeIndex,
                        modifier = Modifier.weight(1f)
                    )
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(top = 17.dp)
                                .width(12.dp)
                                .height(1.dp)
                                .background(
                                    if (index < activeIndex) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "The cloud model only ever receives text after step 2.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class PipelineStepInfo(
    val icon: ImageVector,
    val title: String,
    val engine: String,
    val location: String
)

@Composable
private fun PipelineStep(
    info: PipelineStepInfo,
    number: Int,
    done: Boolean,
    current: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Done: solid primary with a check. Current: accent fill with a primary ring. Next: muted.
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = when {
                done -> scheme.primary
                current -> scheme.primaryContainer
                else -> scheme.surfaceVariant
            },
            contentColor = when {
                done -> scheme.onPrimary
                current -> scheme.onPrimaryContainer
                else -> scheme.onSurfaceVariant
            },
            border = if (current) BorderStroke(1.5.dp, scheme.primary) else null
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (done) {
                    Icon(Icons.Default.Check, contentDescription = "Done", modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
        Text(
            text = info.title,
            style = MaterialTheme.typography.titleSmall,
            color = if (done || current) scheme.onSurface else scheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = info.engine,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = info.location,
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Per-document audit trail: which engine handled each step and whether anything reached the cloud.
 */
@Composable
fun InferenceTrailCard(document: VaultDocument, modifier: Modifier = Modifier) {
    val redactEngine = document.redactionLog.firstOrNull()?.engine?.takeIf { it.isNotBlank() }
        ?: if (document.inferenceRoute == InferenceRoute.ON_DEVICE_RULES) "On-device rules" else "Gemini Nano (On-Device)"

    val (cloudTitle, cloudDetail, cloudDone) = when (document.analysisStatus) {
        AnalysisStatus.ANALYZED -> Triple(
            "Analyzed by $CLOUD_MODEL_LABEL",
            "Received sanitized text only, App Check verified",
            true
        )
        AnalysisStatus.QUEUED_OFFLINE -> Triple(
            "Waiting for network",
            "Stored in Firestore offline cache, analyzes on reconnect",
            false
        )
        AnalysisStatus.LOCAL_ONLY -> Triple(
            "Not sent to the cloud",
            "Kept fully on this device",
            false
        )
    }

    VaultCard(modifier = modifier) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionCaption(text = "Inference trail")
            Spacer(modifier = Modifier.height(12.dp))
            TrailRow(
                icon = Icons.Default.DocumentScanner,
                title = "Text captured",
                detail = "ML Kit OCR · on-device",
                done = true
            )
            HairlineDivider(modifier = Modifier.padding(vertical = 10.dp))
            TrailRow(
                icon = Icons.Default.Shield,
                title = "${document.piiRedactedCount} PII redacted",
                detail = "$redactEngine · on-device",
                done = true
            )
            HairlineDivider(modifier = Modifier.padding(vertical = 10.dp))
            TrailRow(
                icon = if (cloudDone) Icons.Default.Cloud else Icons.Default.CloudOff,
                title = cloudTitle,
                detail = cloudDetail,
                done = cloudDone
            )
        }
    }
}

@Composable
private fun TrailRow(icon: ImageVector, title: String, detail: String, done: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconBubble(icon = icon, size = 36.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (done) scheme.primary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Done",
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text("–", color = scheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
