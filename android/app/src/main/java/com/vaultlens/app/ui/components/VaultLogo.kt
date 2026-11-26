package com.vaultlens.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vaultlens.app.ui.theme.BrandCyan
import com.vaultlens.app.ui.theme.BrandInk
import com.vaultlens.app.ui.theme.BrandTeal
import com.vaultlens.app.ui.theme.BrandTile

/**
 * VaultLens mark: a scan viewfinder around three document lines.
 * The bright middle bar is the redaction.
 *
 * The geometry uses the same 108 x 108 grid as res/drawable/ic_launcher_foreground.xml.
 * The tile shows the window x = 22..86, y = 22..86 of that grid (64 units), so the mark
 * has a little more breathing room than the adaptive launcher icon.
 * The colours are fixed brand colours and do not change with the light or dark theme.
 */
@Composable
fun VaultLogo(
    size: Dp = 30.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 64f
        fun p(v: Float) = (v - 22f) * u

        // Tile and hairline border.
        drawRoundRect(color = BrandTile, cornerRadius = CornerRadius(16.6f * u))
        drawRoundRect(
            color = Color.White.copy(alpha = 0.10f),
            topLeft = Offset(0.4f * u, 0.4f * u),
            size = Size(this.size.width - 0.8f * u, this.size.height - 0.8f * u),
            cornerRadius = CornerRadius(16.2f * u),
            style = Stroke(width = 0.8f * u)
        )

        // Viewfinder corners: square 31..77, arm length 13, corner radius 7.
        val l = 31f
        val t = 31f
        val rt = 77f
        val b = 77f
        val arm = 13f
        val rad = 7f
        val corners = Path().apply {
            moveTo(p(l), p(t + arm))
            lineTo(p(l), p(t + rad))
            arcTo(Rect(p(l), p(t), p(l + 2 * rad), p(t + 2 * rad)), 180f, 90f, false)
            lineTo(p(l + arm), p(t))

            moveTo(p(rt - arm), p(t))
            lineTo(p(rt - rad), p(t))
            arcTo(Rect(p(rt - 2 * rad), p(t), p(rt), p(t + 2 * rad)), 270f, 90f, false)
            lineTo(p(rt), p(t + arm))

            moveTo(p(rt), p(b - arm))
            lineTo(p(rt), p(b - rad))
            arcTo(Rect(p(rt - 2 * rad), p(b - 2 * rad), p(rt), p(b)), 0f, 90f, false)
            lineTo(p(rt - arm), p(b))

            moveTo(p(l + arm), p(b))
            lineTo(p(l + rad), p(b))
            arcTo(Rect(p(l), p(b - 2 * rad), p(l + 2 * rad), p(b)), 90f, 90f, false)
            lineTo(p(l), p(b - arm))
        }
        drawPath(
            path = corners,
            brush = Brush.linearGradient(
                colors = listOf(BrandTeal, BrandCyan),
                start = Offset(p(30f), p(30f)),
                end = Offset(p(78f), p(78f))
            ),
            style = Stroke(width = 5f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Three document lines; the middle one is the redaction bar.
        drawRoundRect(
            color = BrandInk.copy(alpha = 0.55f),
            topLeft = Offset(p(41f), p(41.5f)),
            size = Size(18f * u, 3.4f * u),
            cornerRadius = CornerRadius(1.7f * u)
        )
        drawRoundRect(
            color = BrandInk,
            topLeft = Offset(p(41f), p(51f)),
            size = Size(26f * u, 6f * u),
            cornerRadius = CornerRadius(2f * u)
        )
        drawRoundRect(
            color = BrandInk.copy(alpha = 0.55f),
            topLeft = Offset(p(41f), p(63.1f)),
            size = Size(12f * u, 3.4f * u),
            cornerRadius = CornerRadius(1.7f * u)
        )
    }
}
