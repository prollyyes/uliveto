package it.uliveto.browser.ui.screens.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.domain.UrlClassifier
import it.uliveto.browser.ui.components.AddressField
import it.uliveto.browser.ui.components.AddressFieldState
import it.uliveto.browser.ui.components.EngineLine
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.InstrumentSerif
import it.uliveto.browser.ui.tokens.PillShape
import it.uliveto.browser.ui.tokens.WarmCream
import kotlin.random.Random

@Composable
fun StartScreen(
    viewModel: StartViewModel,
    onNavigateToBrowser: (String) -> Unit,
    onNavigateToTabs: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefs by viewModel.preferences.collectAsState()
    var showNameDialog by remember { mutableStateOf(false) }

    // Show name prompt on first open if name is blank
    if (prefs.userName.isBlank() && !showNameDialog) {
        showNameDialog = true
    }

    if (showNameDialog) {
        NamePromptDialog(
            onConfirm = { name ->
                viewModel.setUserName(name)
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false },
        )
    }

    val terracottaGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFFB25737),
            Color(0xFF9D4626),
            Color(0xFF7E3415),
        ),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = terracottaGradient)
            // 4%-opacity grain overlay drawn with Canvas — no PNG texture
            .drawWithCache {
                val rng = Random(seed = 42)
                val paint = Paint().apply {
                    color = Color.White.copy(alpha = 0.04f)
                }
                val count = 4000
                val xs = FloatArray(count) { rng.nextFloat() * size.width }
                val ys = FloatArray(count) { rng.nextFloat() * size.height }
                val rs = FloatArray(count) { rng.nextFloat() * 1.5f + 0.5f }
                onDrawWithContent {
                    drawContent()
                    drawIntoCanvas { canvas ->
                        for (i in 0 until count) {
                            canvas.drawCircle(Offset(xs[i], ys[i]), rs[i], paint)
                        }
                    }
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Engine line
            EngineLine(
                engine = prefs.searchEngine,
                onEngineSelected = { viewModel.setSearchEngine(it) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Welcome greeting
            val greeting = if (prefs.userName.isBlank()) "Welcome" else "Welcome, ${prefs.userName}"
            Text(
                text = greeting,
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 32.sp,
                    color = WarmCream,
                ),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Search pill (AddressField in Pill state)
            AddressField(
                state = AddressFieldState.Pill,
                onSubmit = { input ->
                    val url = UrlClassifier.buildNavigationUrl(input, prefs.searchEngine)
                    onNavigateToBrowser(url)
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom frosted pill bar
            BottomNavBar(
                onTabsClick = onNavigateToTabs,
                onBookmarksClick = onNavigateToBookmarks,
                onSettingsClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun BottomNavBar(
    onTabsClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(64.dp)
            .clip(PillShape)
            .background(Color.White.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.20f),
                shape = PillShape,
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavBarItem(
            icon = { Icon(imageVector = Icons.Filled.Menu, contentDescription = "Tabs", tint = WarmCream) },
            label = "Tabs",
            onClick = onTabsClick,
            modifier = Modifier.weight(1f),
        )
        NavBarItem(
            icon = { Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Bookmarks", tint = WarmCream) },
            label = "Bookmarks",
            onClick = onBookmarksClick,
            modifier = Modifier.weight(1f),
        )
        NavBarItem(
            icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings", tint = WarmCream) },
            label = "Settings",
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NavBarItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = TextStyle(
                fontFamily = HankenGrotesk,
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                fontSize = 11.sp,
                color = if (active) WarmCream else WarmCream.copy(alpha = 0.80f),
            ),
        )
    }
}

@Composable
private fun NamePromptDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("What's your name?") },
        text = {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Your name") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nameInput.trim()) }) {
                Text("Let's go")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        },
    )
}
