import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ui.Controls
import ui.Display
import ui.rememberAppState

@Composable
@Preview
fun App() {
    val appState = rememberAppState()

    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Controls(
            appState::rules,
            appState::updateRules,
            appState::toggleIsPlaying,
            appState::playing,
            appState::debugString
        )
        Display(
            appState::tileData,
            appState::updateDebugString
        )
    }

    if (appState.playing) {
        LaunchedEffect(appState) {
            delay(1)
            appState.advanceFrame()
        }
    }
}
