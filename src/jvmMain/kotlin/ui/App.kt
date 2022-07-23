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
    rules: Rules.Colors,
    isPlaying: Boolean,
    playPause: () -> Unit,
    submitNewRules: (Rules.Colors) -> Unit,
    callFrame: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        var inspectedTileString by remember { mutableStateOf("") }
        Controls(
            rules,
            submitNewRules,
            playPause,
            isPlaying,
            inspectedTileString
        )
        Display(
            data,
            reportTileInspection =  { inspectedTileString = it }
        )
    }

    if (isPlaying) {
        LaunchedEffect(data) {
            delay(1)
            callFrame()
        }
    }
}

