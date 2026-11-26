package com.vaultlens.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import com.vaultlens.app.data.model.InferenceRoute
import com.vaultlens.app.ui.theme.Emerald600
import com.vaultlens.app.ui.theme.Indigo600

@Composable
fun HybridPrivacyBadge(
    route: InferenceRoute,
    piiSanitizedCount: Int = 0,
    isOnline: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isLocal = route != InferenceRoute.CLOUD_FALLBACK_FLASH
    val tint by animateColorAsState(
        targetValue = if (isLocal) Emerald600 else Indigo600,
        label = "privacyRouteTint"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (isLocal) Icons.Default.Shield else Icons.Default.CloudQueue,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (route == InferenceRoute.ON_DEVICE_NANO) {
                    "🟢 On-Device Privacy Shield (Gemini Nano)"
                } else if (isLocal) {
                    "🟢 On-Device Privacy Shield (rules only · Gemini Nano unavailable)"
                } else {
                    "☁️ Cloud Fallback Mode (Gemini Flash) · PII Redacted ($piiSanitizedCount)"
                },
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (!isOnline) {
                Text(
                    text = "Offline Queue",
                    style = MaterialTheme.typography.labelMedium,
                    color = tint
                )
            }
        }
    }
}
