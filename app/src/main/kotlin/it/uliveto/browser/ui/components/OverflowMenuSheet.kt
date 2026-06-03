package it.uliveto.browser.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.ui.tokens.CharcoalDark
import it.uliveto.browser.ui.tokens.HankenGrotesk

private val SheetBackground = Color(0xFFF7F7F9).copy(alpha = 0.95f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverflowMenuSheet(
    sheetState: SheetState,
    isReaderAvailable: Boolean,
    onNewTab: () -> Unit,
    onBookmarks: () -> Unit,
    onShare: () -> Unit,
    onReader: () -> Unit,
    onFind: () -> Unit,
    onDesktopSite: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // Row 1: New tab, Bookmarks, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                OverflowItem(
                    icon = Icons.Filled.Add,
                    label = "New tab",
                    onClick = onNewTab,
                )
                OverflowItem(
                    icon = Icons.Filled.Bookmarks,
                    label = "Bookmarks",
                    onClick = onBookmarks,
                )
                OverflowItem(
                    icon = Icons.Filled.Share,
                    label = "Share",
                    onClick = onShare,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row 2: Reader, Find, Desktop site
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                OverflowItem(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = "Reader",
                    onClick = onReader,
                    enabled = isReaderAvailable,
                )
                OverflowItem(
                    icon = Icons.Filled.Search,
                    label = "Find",
                    onClick = onFind,
                )
                OverflowItem(
                    icon = Icons.Filled.DesktopWindows,
                    label = "Desktop site",
                    onClick = onDesktopSite,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OverflowItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val contentAlpha = if (enabled) 1f else 0.38f
    Column(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = CharcoalDark.copy(alpha = contentAlpha),
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = CharcoalDark.copy(alpha = contentAlpha),
            fontFamily = HankenGrotesk,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
