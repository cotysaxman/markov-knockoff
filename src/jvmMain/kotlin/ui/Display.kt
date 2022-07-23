package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
//        Grid(dataProvider, dataEdgesProvider, reportTileInspection)
        CanvasGrid(dataProvider, dataEdgesProvider, reportTileInspection)
    }
}
