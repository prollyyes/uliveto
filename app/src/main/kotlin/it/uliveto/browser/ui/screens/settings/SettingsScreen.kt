package it.uliveto.browser.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.BuildConfig
import it.uliveto.browser.data.prefs.AppTheme
import it.uliveto.browser.data.prefs.NavStyle
import it.uliveto.browser.ui.components.EngineLine
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.InstrumentSerif

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

    var showCookieDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    if (showCookieDialog) {
        AlertDialog(
            onDismissRequest = { showCookieDialog = false },
            title = { Text("Cookie Policy", fontFamily = InstrumentSerif, fontSize = 20.sp) },
            text = {
                Text(
                    "Uliveto does not set any first-party cookies. Third-party cookies are subject " +
                        "to the policies of the websites you visit. Uliveto does not share cookie " +
                        "data with any analytics or advertising services.",
                    fontFamily = HankenGrotesk, fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showCookieDialog = false }) { Text("OK", fontFamily = HankenGrotesk) }
            },
        )
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontFamily = InstrumentSerif, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {

            // ── Personal ──────────────────────────────────────────────────
            item { SectionHeader("Personal") }
            item {
                var nameInput by remember(prefs.userName) { mutableStateOf(prefs.userName) }
                var editing by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Your name", fontFamily = HankenGrotesk, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(6.dp))
                    if (editing) {
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
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = {
                                viewModel.setUserName(nameInput.trim())
                                keyboard?.hide()
                                editing = false
                            }) { Text("Save", fontFamily = HankenGrotesk) }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (prefs.userName.isBlank()) "Not set" else prefs.userName,
                                fontFamily = HankenGrotesk, fontSize = 15.sp,
                                color = if (prefs.userName.isBlank())
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.onBackground,
                            )
                            TextButton(onClick = { editing = true }) {
                                Text("Edit", fontFamily = HankenGrotesk)
                            }
                        }
                    }
                }
            }
            item { SectionDivider() }

            // ── Search ────────────────────────────────────────────────────
            item { SectionHeader("Search") }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Search engine", fontFamily = HankenGrotesk, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                    EngineLine(engine = prefs.searchEngine, onEngineSelected = { viewModel.setSearchEngine(it) })
                }
            }
            item { SectionDivider() }

            // ── Navigation ────────────────────────────────────────────────
            item { SectionHeader("Navigation") }
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Navigation style", fontFamily = HankenGrotesk, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(8.dp))
                    val navOptions = listOf(NavStyle.Hourglass, NavStyle.Classic)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        navOptions.forEachIndexed { index, style ->
                            SegmentedButton(
                                selected = prefs.navStyle == style,
                                onClick = { viewModel.setNavStyle(style) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = navOptions.size),
                            ) {
                                Text(
                                    text = if (style == NavStyle.Hourglass) "Bubbles" else "Classic",
                                    fontFamily = HankenGrotesk, fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }
            }
            item { SectionDivider() }

            // ── Appearance ────────────────────────────────────────────────
            item { SectionHeader("Appearance") }
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Theme", fontFamily = HankenGrotesk, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(themeSwatches) { (theme, label, swatchColor) ->
                            ThemeSwatch(
                                label = label,
                                color = swatchColor,
                                selected = prefs.theme == theme,
                                onClick = { viewModel.setTheme(theme) },
                            )
                        }
                    }
                }
            }
            item { SectionDivider() }

            // ── Privacy ───────────────────────────────────────────────────
            item { SectionHeader("Privacy") }
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Uliveto collects no telemetry. The CI job that proves this publishes a " +
                            "network capture for every release.",
                        fontFamily = HankenGrotesk, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                        lineHeight = 20.sp,
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Safe Browsing", fontFamily = HankenGrotesk, fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground)
                        Text("Safe Browsing sends URLs to Google's servers.", fontFamily = HankenGrotesk,
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            lineHeight = 17.sp)
                    }
                    Switch(checked = prefs.safeBrowsingEnabled, onCheckedChange = { viewModel.setSafeBrowsing(it) })
                }
            }
            item { ClickableRow("Cookie policy") { showCookieDialog = true } }
            item { ClickableRow("Clear browsing data") { showClearDataDialog = true } }
            item { ClickableRow("Privacy Receipts", onClick = onNavigateToPrivacyReceipts) }
            item { SectionDivider() }

            // ── About ─────────────────────────────────────────────────────
            item { SectionHeader("About") }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Version", fontFamily = HankenGrotesk, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text(BuildConfig.VERSION_NAME, fontFamily = HankenGrotesk, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            }
            item { ClickableRow("Open source licenses") { showLicensesDialog = true } }
            item { ClickableRow("Source code") { uriHandler.openUri("https://github.com/prollyyes/uliveto") } }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ThemeSwatch(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontFamily = HankenGrotesk,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (selected) 1f else 0.65f),
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = InstrumentSerif,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun ClickableRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = HankenGrotesk, fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
