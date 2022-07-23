package ui

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
    private val state: StateData,
    private val onTransitionProvider: () -> (AppState) -> Unit
) {
    val playing: Boolean
        get() = state.isPlaying
    fun toggleIsPlaying() {
        val nextState = state.copy(
            isPlaying = !state.isPlaying
        )
        onTransitionProvider()(AppState(nextState, onTransitionProvider))
    }

    fun tileAt2dIndex(y: Int, x: Int): Color =
        state.data[y, x]
    val dataShape: List<Int>
        get() = state.data.edges
    fun advanceFrame() {
        val nextState = state.copy(
            data = state.ruleSet.nextFrame(state.data)
        )
        onTransitionProvider()(AppState(nextState, onTransitionProvider))
    }

    val rules: RuleSet<Color>
        get() = state.ruleSet
    fun updateRules(newRules: RuleSet<Color>) {
        val nextState = StateData.DEFAULT_INSTANCE.copy(
            ruleSet = newRules
        )
        onTransitionProvider()(AppState(nextState, onTransitionProvider))
    }

    val debugString: String
        get() = state.debugString
    fun updateDebugString(newString: String) {
        val nextState = state.copy(
            debugString = newString
        )
        onTransitionProvider()(AppState(nextState, onTransitionProvider))
    }

    companion object {
        fun instance(onTransitionProvider: () -> (AppState) -> Unit) =
            AppState(StateData.DEFAULT_INSTANCE, onTransitionProvider)
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
        private val DEFAULT_EDGES = listOf(15, 15)
        private val DEFAULT_RULES = ColorScripts.RIVER_VALLEY.copy()
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
