package ui

import ColorScripts
import NDimensionalCollection
import RuleSet
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Composable
fun rememberAppState(): AppState {
    lateinit var updateAppState: (AppState) -> Unit
    var appState by remember { mutableStateOf(
        AppState.instance { updateAppState }
    )}
    updateAppState = { nextState -> appState = nextState }

    return appState
}

class AppState private constructor(
    val key: Int = 0,
    private val state: StateData,
    private val onTransitionProvider: () -> (AppState) -> Unit
) {
    private fun transitionTo(nextData: StateData) {
        onTransitionProvider()(
            AppState(
                key = key + 1,
                state = nextData,
                onTransitionProvider = onTransitionProvider
            )
        )
    }

    val playing: Boolean
        get() = state.isPlaying
    fun toggleIsPlaying() {
        val nextState = state.copy(
            isPlaying = !state.isPlaying
        )
        transitionTo(nextState)
    }

    fun tileAt2dIndex(y: Int, x: Int): Color =
        state.data[y, x]
    val dataShape: List<Int>
        get() = state.data.edges

    fun advanceFrame() {
        val nextState = state.ruleSet.nextFrame(state.data)
            .takeIf { it != state.data }
            ?.let{ newFrame ->
                state.copy(
                    data = newFrame
                )
            } ?: state.copy(
                isPlaying = false
            )

        transitionTo(nextState)
    }

    val rules: RuleSet<Color>
        get() = state.ruleSet
    fun updateRules(newRules: RuleSet<Color>) {
        val nextState = StateData.DEFAULT_INSTANCE.copy(
            ruleSet = newRules
        )
        transitionTo(nextState)
    }

    val debugString: String
        get() = state.debugString
    fun updateDebugString(newString: String) {
        val nextState = state.copy(
            debugString = newString
        )
        transitionTo(nextState)
    }

    companion object {
        fun instance(onTransitionProvider: () -> (AppState) -> Unit) =
            AppState(0, StateData.DEFAULT_INSTANCE, onTransitionProvider)
    }
}

@Immutable
private data class StateData(
    val data: NDimensionalCollection<Color>,
    val ruleSet: RuleSet<Color>,
    val isPlaying: Boolean,
    val debugString: String
) {
    companion object {
        private val DEFAULT_TILE = Color.Black
        private val DEFAULT_EDGES = listOf(32, 60)
        private val DEFAULT_RULES = ColorScripts.MAZE_BACK_TRACKER.copy()
        private const val DEFAULT_IS_PLAYING = false
        private const val DEFAULT_DEBUG_STRING = ""

        val DEFAULT_INSTANCE = StateData(
            data = NDimensionalCollection(DEFAULT_TILE, DEFAULT_EDGES),
            ruleSet = DEFAULT_RULES,
            isPlaying = DEFAULT_IS_PLAYING,
            debugString = DEFAULT_DEBUG_STRING
        )
    }
}
