// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay

@Composable
@Preview
fun App(
    data: NDimensionalCollection<Color>,
    rules: Rules.Colors,
    isPlaying: Boolean,
    playPause: () -> Unit,
    submitNewRules: (Rules.Colors) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val ruleString = rules.toString()
                var pendingRules by remember { mutableStateOf(ruleString) }

                TextField(
                    value = pendingRules,
                    onValueChange = { newValue -> pendingRules = newValue },
                    enabled = true
                )
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            submitNewRules(Rules.Colors.fromString(pendingRules))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = Icons.Default.Refresh.name,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Button(
                        onClick = {
                            playPause()
                        }
                    ) {
                        val (vector, description) = when(isPlaying) {
                            true -> Icons.Default.Pause
                            false -> Icons.Default.PlayArrow
                        }.let { it to it.name }

                        Icon(
                            imageVector = vector,
                            contentDescription = description,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
                Text(ruleString)
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                data.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { item ->
                            Spacer(modifier = Modifier
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
}

fun main() = application {
    var data by remember { mutableStateOf(initialData) }
    var currentRules by remember { mutableStateOf(initialRules) }
    var isPlaying by remember { mutableStateOf(true) }
    var transformer by remember {
        mutableStateOf(
            Engine(currentRules)
        )
    }

    @Suppress("UNUSED_VARIABLE")
    val animationLoop = LaunchedEffect(Unit) {
        while(true) {
            delay(1)
            if (!isPlaying) {
                delay(250)
                continue
            }
            data = transformer.nextFrame(data)
        }
    }

    Window(onCloseRequest = ::exitApplication) {
        App(
            data,
            currentRules,
            isPlaying,
            playPause = { isPlaying = !isPlaying }
        ) { newRules ->
            currentRules = newRules
            data = initialData
            transformer = Engine(currentRules)
        }
    }
}

val initialRules = Rules.Colors.fromString("""
    !
    B > W * 1
    B > R * 1
    RB > RR | WB > WW
    RW > UU
    W > B | R > B
    UB > UU * 1
    UB > UG
    B > E * 13
    EB > EE | GB > GG
""".trimIndent()
)

val initialData = NDimensionalCollection(
    Black,
    listOf(20, 16)
)
