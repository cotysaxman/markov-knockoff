import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ui.Controls
import ui.Display

@Composable
@Preview
fun App(
    data: NDimensionalCollection<Color>,
    ruleSet: RuleSet<Color>,
    submitNewRules: (RuleSet<Color>) -> Unit,
    callFrame: () -> Unit
) {
    var appState by remember { mutableStateOf(AppState(false)) }

    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        var inspectedTileString by remember { mutableStateOf("") }
        Controls(
            { ruleSet },
            submitNewRules,
            { appState = AppState(!appState.isPlaying) },
            { appState.isPlaying },
            { inspectedTileString }
        )
        Display(
            { data },
            reportTileInspection =  { inspectedTileString = it }
        )
    }

    if (appState.isPlaying) {
        LaunchedEffect(data) {
            delay(1)
            callFrame()
        }
    }
}

data class AppState(
    val isPlaying: Boolean
)
