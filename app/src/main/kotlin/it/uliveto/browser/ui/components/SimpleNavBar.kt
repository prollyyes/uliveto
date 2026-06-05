package it.uliveto.browser.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.ui.tokens.CharcoalDark
import it.uliveto.browser.ui.tokens.GlassMaterial
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.PillShape
import it.uliveto.browser.ui.tokens.ulivetoGlass

/**
 * Four-element bottom navigation bar: back circle | url pill | forward circle | menu circle.
 *
 * Each element carries the same glass surface so the bar reads as a cohesive unit
 * while staying visually light over page content.
 */
@Composable
fun SimpleNavBar(
    currentUrl: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onAddressTap: () -> Unit,
    onMenuTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NavCircle(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Go back",
            enabled = canGoBack,
            onClick = onBack,
        )

        val domain = remember(currentUrl) { extractDomain(currentUrl) }
        Row(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .ulivetoGlass(GlassMaterial.Functional, PillShape)
                .clickable(onClick = onAddressTap)
                .padding(horizontal = 14.dp)
                .semantics { contentDescription = "Address bar: $currentUrl" },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = CharcoalDark.copy(alpha = 0.6f),
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = domain,
                color = CharcoalDark,
                fontFamily = HankenGrotesk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        NavCircle(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Go forward",
            enabled = canGoForward,
            onClick = onForward,
        )

        NavCircle(
            icon = Icons.Filled.MoreVert,
            contentDescription = "More options",
            enabled = true,
            onClick = onMenuTap,
        )
    }
}

@Composable
private fun NavCircle(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .ulivetoGlass(GlassMaterial.Functional, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) CharcoalDark else CharcoalDark.copy(alpha = 0.26f),
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun extractDomain(url: String): String {
    return try {
        android.net.Uri.parse(url).host?.removePrefix("www.") ?: url
    } catch (_: Exception) {
        url
    }
}
