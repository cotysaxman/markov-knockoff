package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CanvasGrid(
    dataProvider: (Int, Int) -> Color,
    dataEdgesProvider: () -> List<Int>,
    reportTileInspection: (String) -> Unit
) {
    val (height, width) = dataEdgesProvider().let { edges ->
        edges[0] to edges[1]
    }
    val tileWidth = 10
    val tileHeight = 8
    val padding = 1

    Canvas(
        Modifier.size(
            width = (width * tileWidth).dp,
            height = (height * tileHeight).dp
        ).clickable {
            reportTileInspection("Clicked the canvas")
        }
    ) {
        repeat(height) { rowIndex ->
            repeat(width) { columnIndex ->
                drawRect(
                    color = dataProvider(rowIndex, columnIndex),
                    size = Size(
                        width = (size.width / width) - padding,
                        height = (size.height / height) - padding
                    ),
                    topLeft = Offset(
                        x = (size.width / width) * columnIndex,
                        y = (size.height / height) * rowIndex
                    )
                )
            }
        }
    }
}

@Suppress("unused")
@Composable
fun Grid(
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
                        { reportTileInspection(
                                """
                            Row: $rowIndex
                            Column: $columnIndex
                            Content: ${dataProvider(rowIndex, columnIndex)}
                        """.trimIndent()
                        )}
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
