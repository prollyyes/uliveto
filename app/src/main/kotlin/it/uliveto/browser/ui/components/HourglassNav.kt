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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.ui.tokens.CharcoalDark
import it.uliveto.browser.ui.tokens.GlassMaterial
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.HourglassShape
import it.uliveto.browser.ui.tokens.ulivetoGlass

/**
 * Loaded-page navigation bar with the custom hourglass shape.
 *
 * Contains back/forward navigation buttons and a centered domain display
 * that opens the address sheet when tapped.
 */
@Composable
fun HourglassNav(
    currentUrl: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onAddressTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .ulivetoGlass(GlassMaterial.Functional, HourglassShape)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Back button — 48dp hit target, extends into waist area
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(enabled = canGoBack, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (canGoBack) CharcoalDark else CharcoalDark.copy(alpha = 0.26f),
                modifier = Modifier.size(20.dp),
            )
        }

        // Center address section
        val domain = remember(currentUrl) { extractDomain(currentUrl) }
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onAddressTap),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
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

        // Forward button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(enabled = canGoForward, onClick = onForward),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Forward",
                tint = if (canGoForward) CharcoalDark else CharcoalDark.copy(alpha = 0.26f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Three-dot overflow menu button styled as a circular glass bubble.
 * Companion to [HourglassNav] in the bottom chrome row.
 */
@Composable
fun OverflowDot(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .ulivetoGlass(GlassMaterial.Functional, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "More options",
            tint = CharcoalDark,
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
