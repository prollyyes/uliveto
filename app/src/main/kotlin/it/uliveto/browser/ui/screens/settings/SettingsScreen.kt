package it.uliveto.browser.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.BuildConfig
import it.uliveto.browser.data.prefs.AppTheme
import it.uliveto.browser.data.prefs.NavStyle
import it.uliveto.browser.ui.components.EngineLine
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.InstrumentSerif

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToPrivacyReceipts: () -> Unit,
) {
    val prefs by viewModel.preferences.collectAsState()
    val uriHandler = LocalUriHandler.current

    var showCookieDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    if (showCookieDialog) {
        AlertDialog(
            onDismissRequest = { showCookieDialog = false },
            title = {
                Text(
                    text = "Cookie Policy",
                    fontFamily = InstrumentSerif,
                    fontSize = 20.sp,
                )
            },
            text = {
                Text(
                    text = "Uliveto does not set any first-party cookies. Third-party cookies are " +
                        "subject to the policies of the websites you visit. Uliveto does not " +
                        "share cookie data with any analytics or advertising services.",
                    fontFamily = HankenGrotesk,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showCookieDialog = false }) {
                    Text("OK", fontFamily = HankenGrotesk)
                }
            },
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = "Clear Browsing Data",
                    fontFamily = InstrumentSerif,
                    fontSize = 20.sp,
                )
            },
            text = {
                Text(
                    text = "This will clear your browsing history, cookies, and cached files.",
                    fontFamily = HankenGrotesk,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Clear", fontFamily = HankenGrotesk)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel", fontFamily = HankenGrotesk)
                }
            },
        )
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = {
                Text(
                    text = "Open Source Licenses",
                    fontFamily = InstrumentSerif,
                    fontSize = 20.sp,
                )
            },
            text = {
                Text(
                    text = "Instrument Serif and Hanken Grotesk are licensed under the " +
                        "SIL Open Font License, Version 1.1.\n\n" +
                        "This Font Software is licensed under the SIL Open Font License, " +
                        "Version 1.1. This license is available with a FAQ at: " +
                        "https://openfontlicense.org\n\n" +
                        "Copyright 2022 The Instrument Serif Project Authors.\n" +
                        "Copyright 2021 The Hanken Grotesk Project Authors.",
                    fontFamily = HankenGrotesk,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close", fontFamily = HankenGrotesk)
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontFamily = InstrumentSerif,
                        fontSize = 22.sp,
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
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 0.dp),
        ) {
            // ── Search ────────────────────────────────────────────────────────
            item {
                SectionHeader(title = "Search")
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Search engine",
                        fontFamily = HankenGrotesk,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    EngineLine(
                        engine = prefs.searchEngine,
                        onEngineSelected = { viewModel.setSearchEngine(it) },
                    )
                }
            }
            item { SectionDivider() }

            // ── Navigation ────────────────────────────────────────────────────
            item {
                SectionHeader(title = "Navigation")
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Navigation style",
                        fontFamily = HankenGrotesk,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val navOptions = listOf(NavStyle.Hourglass, NavStyle.Classic)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        navOptions.forEachIndexed { index, navStyle ->
                            SegmentedButton(
                                selected = prefs.navStyle == navStyle,
                                onClick = { viewModel.setNavStyle(navStyle) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = navOptions.size),
                            ) {
                                Text(
                                    text = navStyle.name,
                                    fontFamily = HankenGrotesk,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }
            }
            item { SectionDivider() }

            // ── Appearance ────────────────────────────────────────────────────
            item {
                SectionHeader(title = "Appearance")
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Theme",
                        fontFamily = HankenGrotesk,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val themeOptions = listOf(AppTheme.Light, AppTheme.Dark, AppTheme.OledBlack, AppTheme.FollowSystem)
                    val themeLabels = listOf("Light", "Dark", "OLED", "System")
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        themeOptions.forEachIndexed { index, theme ->
                            SegmentedButton(
                                selected = prefs.theme == theme,
                                onClick = { viewModel.setTheme(theme) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                            ) {
                                Text(
                                    text = themeLabels[index],
                                    fontFamily = HankenGrotesk,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }
            item { SectionDivider() }

            // ── Privacy ───────────────────────────────────────────────────────
            item {
                SectionHeader(title = "Privacy")
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Uliveto collects no telemetry. The CI job that proves this " +
                            "publishes a network capture for every release.",
                        fontFamily = HankenGrotesk,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                        lineHeight = 20.sp,
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Safe Browsing",
                            fontFamily = HankenGrotesk,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Safe Browsing sends URLs to Google's servers.",
                            fontFamily = HankenGrotesk,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            lineHeight = 17.sp,
                        )
                    }
                    Switch(
                        checked = prefs.safeBrowsingEnabled,
                        onCheckedChange = { viewModel.setSafeBrowsing(it) },
                    )
                }
            }
            item {
                ClickableRow(
                    label = "Cookie policy",
                    onClick = { showCookieDialog = true },
                )
            }
            item {
                ClickableRow(
                    label = "Clear browsing data",
                    onClick = { showClearDataDialog = true },
                )
            }
            item {
                ClickableRow(
                    label = "Privacy Receipts",
                    onClick = onNavigateToPrivacyReceipts,
                )
            }
            item { SectionDivider() }

            // ── About ─────────────────────────────────────────────────────────
            item {
                SectionHeader(title = "About")
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Version",
                        fontFamily = HankenGrotesk,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        fontFamily = HankenGrotesk,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
            item {
                ClickableRow(
                    label = "Open source licenses",
                    onClick = { showLicensesDialog = true },
                )
            }
            item {
                ClickableRow(
                    label = "Source code",
                    onClick = { uriHandler.openUri("https://github.com/prollyyes/uliveto") },
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
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
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun ClickableRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = HankenGrotesk,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
