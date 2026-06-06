package it.uliveto.browser.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.domain.SearchEngine
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.InstrumentSerif
import it.uliveto.browser.ui.tokens.WarmCream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineLine(
    engine: SearchEngine,
    onEngineSelected: (SearchEngine) -> Unit,
    modifier: Modifier = Modifier,
    customSearchEngineUrl: String = "",
    onCustomUrlChange: (String) -> Unit = {},
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val displayName = if (engine == SearchEngine.Custom && customSearchEngineUrl.isNotBlank()) {
        customSearchEngineUrl.substringAfter("://").substringBefore("/").substringBefore("?")
    } else {
        engine.displayName
    }

    Row(
        modifier = modifier
            .semantics { contentDescription = "Search engine: $displayName, tap to change" }
            .clickable { showSheet = true }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontFamily = HankenGrotesk,
                        fontWeight = FontWeight.Normal,
                        color = WarmCream.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                    )
                ) {
                    append("Searching with ")
                }
                withStyle(
                    SpanStyle(
                        fontFamily = InstrumentSerif,
                        fontStyle = FontStyle.Italic,
                        color = WarmCream,
                        fontSize = 14.sp,
                    )
                ) {
                    append(displayName)
                }
            },
        )
    }

    if (showSheet) {
        val keyboard = LocalSoftwareKeyboardController.current
        var showCustomInput by remember { mutableStateOf(engine == SearchEngine.Custom) }
        var customInput by remember { mutableStateOf(customSearchEngineUrl) }

        fun closeSheet() {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) showSheet = false
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            Text(
                text = "Search engine",
                style = TextStyle(
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color.Unspecified,
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            HorizontalDivider()

            // Built-in engines
            SearchEngine.entries.filter { it != SearchEngine.Custom }.forEach { entry ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = entry.displayName,
                            style = TextStyle(
                                fontFamily = HankenGrotesk,
                                fontWeight = if (entry == engine) FontWeight.Medium else FontWeight.Normal,
                                fontSize = 15.sp,
                            ),
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        onEngineSelected(entry)
                        showCustomInput = false
                        closeSheet()
                    },
                )
            }

            HorizontalDivider()

            // Custom engine row
            ListItem(
                headlineContent = {
                    Text(
                        text = "Custom engine",
                        style = TextStyle(
                            fontFamily = HankenGrotesk,
                            fontWeight = if (engine == SearchEngine.Custom) FontWeight.Medium else FontWeight.Normal,
                            fontSize = 15.sp,
                        ),
                    )
                },
                supportingContent = {
                    Text(
                        text = "Use any search engine with a URL template",
                        fontFamily = HankenGrotesk,
                        fontSize = 12.sp,
                        color = Color.Unspecified.copy(alpha = 0.6f),
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { showCustomInput = !showCustomInput },
            )

            if (showCustomInput) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it },
                        label = { Text("URL template", fontFamily = HankenGrotesk) },
                        placeholder = { Text("https://example.com/search?q=%s", fontFamily = HankenGrotesk) },
                        supportingText = { Text("Use %s where the search query goes", fontFamily = HankenGrotesk) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { showCustomInput = false }) {
                            Text("Cancel", fontFamily = HankenGrotesk)
                        }
                        TextButton(
                            onClick = {
                                val url = customInput.trim()
                                onCustomUrlChange(url)
                                onEngineSelected(SearchEngine.Custom)
                                keyboard?.hide()
                                closeSheet()
                            },
                            enabled = customInput.isNotBlank() && customInput.contains("%s"),
                        ) {
                            Text("Use this engine", fontFamily = HankenGrotesk)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
