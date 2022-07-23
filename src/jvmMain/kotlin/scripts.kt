import androidx.compose.ui.graphics.Color

enum class ColorScripts(
    private val ruleSet: RuleSet<Color>
) {
    STRAIGHT_WHITE_LINE("""
        !
        B > W * 1
        WB > WW * 1
        WWB > WWW
    """.trimIndent()),
    RIVER_VALLEY("""
        !
        B > W * 1
        B > R * 1
        RB > RR | WB > WW
        RW > UU
        W > B | R > B
        UB > UU * 1
        BU^UB > UU^UB
        UB > UG
        B > E * 13
        EB > EE | GB > GG
    """.trimIndent()),
    CHECKERBOARD("""
        !
        B > R * 1
        RB^BB > RB^BR
    """.trimIndent()),
    MAZE_BACK_TRACKER("""
        ?
        B > R * 1
        RBB > GGR
        RGG > WWR
    """.trimIndent()),
    ;

    constructor(
        ruleString: String
    ) : this(
        RuleSetInterpreter.Colors.decodeRuleSetSafely(ruleString)
    )

    fun copy(): RuleSet<Color> = ruleSet.copy()

    val ruleString: String = RuleSetInterpreter.Colors.encodeToString(ruleSet)

    companion object {
        private fun byRules(ruleSet: RuleSet<Color>) = values().firstOrNull { script ->
            ruleSet == script.ruleSet
        }
        fun byString(raw: String) = byRules(
            RuleSetInterpreter.Colors.decodeRuleSetSafely(raw)
        )
    }
}