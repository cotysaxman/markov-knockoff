// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    var data by remember { mutableStateOf(initialData) }
    var currentRules by remember { mutableStateOf(initialRules) }
    var isPlaying by remember { mutableStateOf(true) }
    var transformer by remember {
        mutableStateOf(
            Engine(currentRules)
        )
    }

    Window(onCloseRequest = ::exitApplication) {
        App(
            data,
            currentRules,
            isPlaying,
            playPause = {
                isPlaying = !isPlaying
                if (isPlaying) {
                    data = transformer.nextFrame(data)
                }
            },
            submitNewRules = { newRules ->
                currentRules = newRules
                data = initialData
                transformer = Engine(newRules)
            },
            callFrame = {
                data = transformer.nextFrame(data)
            }
        )
    }
}

val initialRules = ColorScripts.RIVER_VALLEY.copy()

val initialData = NDimensionalCollection(
    Black,
    listOf(15, 15)
)
