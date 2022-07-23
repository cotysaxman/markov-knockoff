package ui

import NDimensionalCollection
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Display(
    data: NDimensionalCollection<Color>,
    reportTileInspection: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Grid(data, reportTileInspection)
    }
}

@Composable
private fun Grid(
    data: NDimensionalCollection<Color>,
    reportTileInspection: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center
    ) {
        data.forEachIndexed { rowIndex, row ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEachIndexed { columnIndex, item ->
                    Tile(
                        rowIndex,
                        columnIndex,
                        data,
                        reportTileInspection,
                        item
                    )
                }
            }
        }
    }
}

@Composable
private fun Tile(
    rowIndex: Int,
    columnIndex: Int,
    data: NDimensionalCollection<Color>,
    reportTileInspection: (String) -> Unit,
    item: NDimensionalCollection<Color>
) {
    val decodedIndex = NDimensionalCollection.indexFromCoordinates(
        listOf(rowIndex, columnIndex),
        data.edges
    ) ?: 0
    val reEncodedCoordinates =
        NDimensionalCollection.coordinatesForIndex(decodedIndex, data.edges)
    Spacer(modifier = Modifier
        .clickable {
            reportTileInspection(
                "$rowIndex, $columnIndex: $item\nShould be at: $decodedIndex -> $reEncodedCoordinates"
            )
        }
        .padding(1.dp)
        .size(width = 26.dp, height = 14.dp)
        .background(color = item.value)
    )
}