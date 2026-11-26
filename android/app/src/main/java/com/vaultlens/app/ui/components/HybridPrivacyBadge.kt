package com.vaultlens.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaultlens.app.data.model.InferenceRoute
import com.vaultlens.app.ui.theme.Emerald600
import com.vaultlens.app.ui.theme.Violet

@Composable
fun HybridPrivacyBadge(
    route: InferenceRoute,
    piiSanitizedCount: Int = 0,
    isOnline: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isLocal = route != InferenceRoute.CLOUD_FALLBACK_FLASH
    val tint by animateColorAsState(
        targetValue = if (isLocal) Emerald600 else Violet,
        label = "privacyRouteTint"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = tint.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface) {
                Icon(
                    imageVector = if (isLocal) Icons.Default.Shield else Icons.Default.CloudQueue,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(18.dp)
                )
            }
            Text(
                text = if (route == InferenceRoute.ON_DEVICE_NANO) {
                    "On-Device Privacy Shield (Gemini Nano)"
                } else if (isLocal) {
                    "On-Device Privacy Shield (rules only · Gemini Nano unavailable)"
                } else {
                    "Cloud Fallback Mode (Gemini Flash) · PII Redacted ($piiSanitizedCount)"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (!isOnline) {
                Text(
                    text = "Offline Queue",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = tint
                )
            }
        }
    }
}
