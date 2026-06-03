package it.uliveto.browser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.PillShape
import it.uliveto.browser.ui.tokens.WarmCream

enum class AddressFieldState { Pill, HourglassCenter, Expanded }

@Composable
fun AddressField(
    state: AddressFieldState,
    onSubmit: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when (state) {
        AddressFieldState.Pill -> PillAddressField(onExpand = { /* handled inside */ }, onSubmit = onSubmit, modifier = modifier)
        AddressFieldState.Expanded -> ExpandedAddressField(onSubmit = onSubmit, modifier = modifier)
        AddressFieldState.HourglassCenter -> PillAddressField(onExpand = { }, onSubmit = onSubmit, modifier = modifier)
    }
}

@Composable
private fun PillAddressField(
    onExpand: () -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        // Pill state — always visible when not expanded
        AnimatedVisibility(
            visible = !expanded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(PillShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.30f),
                        shape = PillShape,
                    )
                    .clickable { expanded = true },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = WarmCream.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = "Search or enter URL",
                        style = TextStyle(
                            fontFamily = HankenGrotesk,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            color = WarmCream.copy(alpha = 0.6f),
                        ),
                    )
                }
            }
        }

        // Expanded state — fades in when tapped
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ExpandedAddressField(
                onSubmit = { url ->
                    expanded = false
                    onSubmit(url)
                },
                onDismiss = { expanded = false },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ExpandedAddressField(
    onSubmit: (String) -> Unit,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = text,
        onValueChange = { text = it },
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = HankenGrotesk,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = WarmCream,
        ),
        cursorBrush = SolidColor(WarmCream),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (text.isNotBlank()) onSubmit(text)
                else onDismiss?.invoke()
            },
        ),
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.20f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.40f),
                shape = RoundedCornerShape(50),
            )
            .focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = WarmCream.copy(alpha = 0.8f),
                    modifier = Modifier.padding(end = 8.dp),
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Search or enter URL",
                            style = TextStyle(
                                fontFamily = HankenGrotesk,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = WarmCream.copy(alpha = 0.5f),
                            ),
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}
