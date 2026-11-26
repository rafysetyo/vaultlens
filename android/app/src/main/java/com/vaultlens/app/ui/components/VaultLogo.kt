package com.vaultlens.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val LogoInk = Color(0xFF17171C)
private val LogoIndigo = Color(0xFF4F46E5)

/**
 * VaultLens mark (matches the web logo on a 40-unit grid):
 * a lens ring over three document lines; the middle line is the indigo redaction.
 */
@Composable
fun VaultLogo(
    size: Dp = 30.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 40f

        drawRoundRect(color = LogoInk, cornerRadius = CornerRadius(11f * u, 11f * u))

        drawCircle(
            color = Color.White,
            radius = 11f * u,
            center = Offset(20f * u, 20f * u),
            style = Stroke(width = 2.4f * u)
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.55f),
            topLeft = Offset(13.5f * u, 14.6f * u),
            size = Size(9f * u, 2.2f * u),
            cornerRadius = CornerRadius(1.1f * u, 1.1f * u)
        )
        drawRoundRect(
            color = LogoIndigo,
            topLeft = Offset(13.5f * u, 18.9f * u),
            size = Size(13f * u, 3.6f * u),
            cornerRadius = CornerRadius(1.2f * u, 1.2f * u)
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.55f),
            topLeft = Offset(13.5f * u, 24.6f * u),
            size = Size(6.5f * u, 2.2f * u),
            cornerRadius = CornerRadius(1.1f * u, 1.1f * u)
        )

        drawLine(
            color = Color.White,
            start = Offset(28.2f * u, 28.2f * u),
            end = Offset(32.4f * u, 32.4f * u),
            strokeWidth = 2.6f * u,
            cap = StrokeCap.Round
        )
    }
}
