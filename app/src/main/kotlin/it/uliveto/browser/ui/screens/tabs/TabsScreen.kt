package it.uliveto.browser.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.ui.tokens.CharcoalDark
import it.uliveto.browser.ui.tokens.GlassMaterial
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.ulivetoGlass

private val CardShape = RoundedCornerShape(16.dp)
private val FrostedCard = Color(0xFFF7F7F9).copy(alpha = 0.95f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsScreen(
    currentUrl: String,
    onSelectTab: () -> Unit,
    onNewTab: () -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Tabs",
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            },
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Current tab card
            item {
                Box(
                    modifier = Modifier
                        .aspectRatio(0.75f)
                        .ulivetoGlass(GlassMaterial.Functional, CardShape)
                        .clickable(onClick = onSelectTab)
                        .padding(12.dp),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = currentUrl,
                            fontFamily = HankenGrotesk,
                            fontSize = 11.sp,
                            color = CharcoalDark.copy(alpha = 0.65f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Current tab",
                            fontFamily = HankenGrotesk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = CharcoalDark,
                        )
                    }
                }
            }

            // New tab tile
            item {
                Box(
                    modifier = Modifier
                        .aspectRatio(0.75f)
                        .background(
                            color = Color(0xFFF7F7F9).copy(alpha = 0.5f),
                            shape = CardShape,
                        )
                        .clickable(onClick = onNewTab),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "New tab",
                            tint = CharcoalDark.copy(alpha = 0.7f),
                        )
                        Text(
                            text = "New tab",
                            fontFamily = HankenGrotesk,
                            fontSize = 14.sp,
                            color = CharcoalDark.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
