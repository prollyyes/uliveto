package it.uliveto.browser.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
) {
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .semantics { contentDescription = "Search engine: ${engine.displayName}, tap to change" }
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
                    append(engine.displayName)
                }
            },
        )
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) showSheet = false
                }
            },
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
            SearchEngine.entries.forEach { entry ->
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
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                    modifier = Modifier.clickable {
                        onEngineSelected(entry)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) showSheet = false
                        }
                    },
                )
            }
        }
    }
}
