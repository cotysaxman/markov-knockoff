import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ui.Controls

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
        Controls(rules, submitNewRules, playPause, isPlaying, inspectedTileString)
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                data.forEachIndexed { rowIndex, row ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEachIndexed { columnIndex, item ->
                            val decodedIndex = NDimensionalCollection.indexFromCoordinates(
                                listOf(rowIndex, columnIndex),
                                data.edges
                            ) ?: 0
                            val reEncodedCoordinates =
                                NDimensionalCollection.coordinatesForIndex(decodedIndex, data.edges)
                            Spacer(modifier = Modifier
                                .clickable {
                                    inspectedTileString =
                                        "$rowIndex, $columnIndex: $item\nShould be at: $decodedIndex -> $reEncodedCoordinates"
                                }
                                .padding(1.dp)
                                .size(width = 26.dp, height = 14.dp)
                                .background(color = item.value)
                            )
                        }
                    }
                }
            }
        }
    }

    if (isPlaying) {
        LaunchedEffect(data) {
            delay(1)
            callFrame()
        }
    }
}

