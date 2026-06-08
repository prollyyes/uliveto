package it.uliveto.browser.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.BuildConfig
import it.uliveto.browser.data.prefs.AppTheme
import it.uliveto.browser.data.prefs.NavStyle
import it.uliveto.browser.ui.LocalUlivetoColors
import it.uliveto.browser.ui.components.EngineLine
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.InstrumentSerif
import it.uliveto.browser.ui.tokens.WarmCream
import kotlinx.coroutines.delay

// (theme, display label, swatch color)
private val themeSwatches = listOf(
    Triple(AppTheme.Terracotta, "Terra", Color(0xFFB25737)),
    Triple(AppTheme.Aegean,     "Aegean", Color(0xFF1A5E8F)),
    Triple(AppTheme.OliveGrove, "Olive",  Color(0xFF4A5C25)),
    Triple(AppTheme.Amalfi,     "Amalfi", Color(0xFFC17F18)),
    Triple(AppTheme.Santorini,  "Santorini", Color(0xFF1A3A8F)),
    Triple(AppTheme.Night,      "Night",  Color(0xFF2A1A0E)),
    Triple(AppTheme.System,     "System", Color(0xFF888888)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToPrivacyReceipts: () -> Unit,
) {
    val prefs by viewModel.preferences.collectAsState()
    val uriHandler = LocalUriHandler.current
    val keyboard = LocalSoftwareKeyboardController.current

    val ulivetoColors = LocalUlivetoColors.current
    val themeGradient = remember(ulivetoColors) { Brush.radialGradient(ulivetoColors.gradientColors) }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Browsing Data", fontFamily = InstrumentSerif, fontSize = 20.sp) },
            text = {
                Text(
                    "This will clear your browsing history, cookies, and cached files.",
                    fontFamily = HankenGrotesk, fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Clear", fontFamily = HankenGrotesk) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel", fontFamily = HankenGrotesk) }
            },
        )
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open Source Licenses", fontFamily = InstrumentSerif, fontSize = 20.sp) },
            text = {
                Text(
                    "Instrument Serif and Hanken Grotesk are licensed under the SIL Open Font " +
                        "License, Version 1.1.\n\nCopyright 2022 The Instrument Serif Project Authors.\n" +
                        "Copyright 2021 The Hanken Grotesk Project Authors.",
                    fontFamily = HankenGrotesk, fontSize = 13.sp, lineHeight = 19.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) { Text("Close", fontFamily = HankenGrotesk) }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = themeGradient),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            fontFamily = InstrumentSerif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 20.sp,
                            color = WarmCream.copy(alpha = 0.92f),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = WarmCream.copy(alpha = 0.50f),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {

                // ── Personal ──────────────────────────────────────────────────
                item {
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(0L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(200)) +
                                slideInVertically(tween(200)) { -8 },
                    ) {
                        Column {
                            SectionLabel("Personal")
                            GlassCard {
                                var nameInput by remember(prefs.userName) { mutableStateOf(prefs.userName) }
                                var editing by remember { mutableStateOf(false) }

                                if (editing) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            "Name",
                                            fontFamily = HankenGrotesk,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = WarmCream.copy(alpha = 0.90f),
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = nameInput,
                                                onValueChange = { nameInput = it },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                keyboardActions = KeyboardActions(onDone = {
                                                    viewModel.setUserName(nameInput.trim())
                                                    keyboard?.hide()
                                                    editing = false
                                                }),
                                                placeholder = { Text("Your name", fontFamily = HankenGrotesk) },
                                            )
                                            TextButton(onClick = {
                                                viewModel.setUserName(nameInput.trim())
                                                keyboard?.hide()
                                                editing = false
                                            }) { Text("Save", fontFamily = HankenGrotesk, color = WarmCream) }
                                        }
                                    }
                                } else {
                                    SettingRow(
                                        label = "Name",
                                        value = if (prefs.userName.isBlank()) "Not set" else prefs.userName,
                                        showArrow = true,
                                        onClick = { editing = true },
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Search ────────────────────────────────────────────────────
                item {
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(20L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(200)) +
                                slideInVertically(tween(200)) { -8 },
                    ) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            SectionLabel("Search")
                            GlassCard {
                                // EngineLine owns its tap target and bottom-sheet state.
                                // Wrap it with SettingRow-equivalent padding so it sits flush in the card.
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    EngineLine(
                                        engine = prefs.searchEngine,
                                        onEngineSelected = { viewModel.setSearchEngine(it) },
                                        customSearchEngineUrl = prefs.customSearchEngineUrl,
                                        onCustomUrlChange = { viewModel.setCustomSearchEngineUrl(it) },
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Appearance ────────────────────────────────────────────────
                item {
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(40L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(200)) +
                                slideInVertically(tween(200)) { -8 },
                    ) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            SectionLabel("Appearance")
                            GlassCard {
                                Text(
                                    text = "Theme",
                                    fontFamily = HankenGrotesk,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = WarmCream.copy(alpha = 0.90f),
                                    modifier = Modifier.padding(start = 14.dp, top = 11.dp, bottom = 8.dp),
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp),
                                ) {
                                    items(themeSwatches) { (theme, _, swatchColor) ->
                                        ThemeSwatch(
                                            label = null,
                                            color = swatchColor,
                                            selected = prefs.theme == theme,
                                            onClick = { viewModel.setTheme(theme) },
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                RowDivider()
                                val navLabel = if (prefs.navStyle == NavStyle.Hourglass) "Bubbles" else "Classic"
                                SettingRow(
                                    label = "Navigation",
                                    value = navLabel,
                                    showArrow = true,
                                    onClick = {
                                        val next = if (prefs.navStyle == NavStyle.Hourglass) NavStyle.Classic else NavStyle.Hourglass
                                        viewModel.setNavStyle(next)
                                    },
                                )
                            }
                        }
                    }
                }

                // ── Privacy ───────────────────────────────────────────────────
                item {
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(60L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(200)) +
                                slideInVertically(tween(200)) { -8 },
                    ) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            SectionLabel("Privacy")
                            GlassCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Safe Browsing",
                                            fontFamily = HankenGrotesk,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = WarmCream.copy(alpha = 0.90f),
                                        )
                                        Spacer(Modifier.height(1.dp))
                                        Text(
                                            "Sends URLs to Google",
                                            fontFamily = HankenGrotesk,
                                            fontSize = 9.sp,
                                            color = WarmCream.copy(alpha = 0.40f),
                                        )
                                    }
                                    Switch(
                                        checked = prefs.safeBrowsingEnabled,
                                        onCheckedChange = { viewModel.setSafeBrowsing(it) },
                                    )
                                }
                                RowDivider()
                                SettingRow(
                                    label = "Privacy Receipts",
                                    subtitle = "Verified network capture per release",
                                    showArrow = true,
                                    onClick = onNavigateToPrivacyReceipts,
                                )
                                RowDivider()
                                SettingRow(
                                    label = "Clear browsing data",
                                    showArrow = false,
                                    trailing = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Color(0xFFB25737).copy(alpha = 0.55f),
                                            modifier = Modifier
                                                .padding(start = 4.dp)
                                                .size(14.dp),
                                        )
                                    },
                                    onClick = { showClearDataDialog = true },
                                )
                            }
                        }
                    }
                }

                // ── About ─────────────────────────────────────────────────────
                item {
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(80L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(200)) +
                                slideInVertically(tween(200)) { -8 },
                    ) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            SectionLabel("About")
                            GlassCard {
                                SettingRow(
                                    label = "Version",
                                    value = BuildConfig.VERSION_NAME,
                                )
                                RowDivider()
                                SettingRow(
                                    label = "Open source",
                                    value = "GitHub",
                                    showArrow = true,
                                    onClick = { uriHandler.openUri("https://github.com/prollyyes/uliveto") },
                                )
                                RowDivider()
                                SettingRow(
                                    label = "Licenses",
                                    showArrow = true,
                                    onClick = { showLicensesDialog = true },
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSwatch(
    label: String?,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) WarmCream.copy(alpha = 0.85f)
                            else WarmCream.copy(alpha = 0.15f),
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
        )
        if (label != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontFamily = HankenGrotesk,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                color = WarmCream.copy(alpha = if (selected) 1f else 0.75f),
            )
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        fontFamily = HankenGrotesk,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.4.sp,
        color = WarmCream.copy(alpha = 0.28f),
        modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
    )
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmCream.copy(alpha = 0.065f))
            .border(
                width = 1.dp,
                color = WarmCream.copy(alpha = 0.13f),
                shape = RoundedCornerShape(16.dp),
            ),
        content = content,
    )
}

@Composable
private fun SettingRow(
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    showArrow: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 14.dp, vertical = 11.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = HankenGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = WarmCream.copy(alpha = 0.90f),
                lineHeight = 17.sp,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.sp,
                    color = WarmCream.copy(alpha = 0.40f),
                    lineHeight = 13.sp,
                )
            }
        }
        if (value != null) {
            Text(
                text = value,
                fontFamily = InstrumentSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                color = WarmCream.copy(alpha = 0.52f),
                modifier = Modifier.padding(end = if (showArrow) 4.dp else 0.dp),
            )
        }
        if (trailing != null) {
            trailing()
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = WarmCream.copy(alpha = 0.22f),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp),
            )
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = WarmCream.copy(alpha = 0.07f),
    )
}
