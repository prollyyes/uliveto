package it.uliveto.browser.ui.screens.browser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

/**
 * Full-screen browser screen.
 *
 * Contains a debug URL bar (removed in M5) above a full-screen [GeckoView].
 */
@Composable
fun BrowserScreen(
    runtime: GeckoRuntime,
    vmFactory: ViewModelProvider.Factory,
    initialUrl: String = "about:blank",
) {
    val viewModel: BrowserViewModel = viewModel(factory = vmFactory)
    val urlText by viewModel.urlText.collectAsState()

    // Create one GeckoSession per composition; re-use the same instance on recompose.
    val session = remember { GeckoSession() }

    // Open the session when composed; close it when this composable leaves the tree.
    DisposableEffect(session, runtime) {
        session.open(runtime)
        session.loadUri(initialUrl)
        onDispose {
            session.close()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Debug URL bar (removed in M5) ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = urlText,
                onValueChange = { viewModel.onUrlTextChanged(it) },
                label = { Text("URL") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    val url = urlText.trim().let { raw ->
                        if (raw.startsWith("http://") || raw.startsWith("https://")) raw
                        else "https://$raw"
                    }
                    session.loadUri(url)
                },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("Go")
            }
        }

        // ── GeckoView ─────────────────────────────────────────────────────────
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                GeckoView(ctx).apply {
                    setSession(session)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}
