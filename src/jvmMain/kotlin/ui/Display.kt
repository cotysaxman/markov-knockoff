package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Display(
    dataProvider: (Int, Int) -> Color,
    dataEdgesProvider: () -> List<Int>,
    reportTileInspection: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Grid(dataProvider, dataEdgesProvider, reportTileInspection)
    }
}

@Composable
private fun Grid(
    dataProvider: (Int, Int) -> Color,
    dataEdgesProvider: () -> List<Int>,
    reportTileInspection: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center
    ) {
        repeat(times = dataEdgesProvider()[0]) { rowIndex ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(times = dataEdgesProvider()[1]) { columnIndex ->
                    Tile(
                        { dataProvider(rowIndex, columnIndex) },
                        { reportTileInspection("""
                            Row: $rowIndex
                            Column: $columnIndex
                            Content: ${dataProvider(rowIndex, columnIndex)}
                        """.trimIndent())
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Tile(
    contentProvider: () -> Color,
    reportTileInspection: () -> Unit
) {
    Spacer(modifier = Modifier
        .clickable { reportTileInspection() }
        .padding(1.dp)
        .size(width = 26.dp, height = 14.dp)
        .drawBehind {
            drawRect(contentProvider())
        }
    )
}