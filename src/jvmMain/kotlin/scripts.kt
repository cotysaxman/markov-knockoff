enum class ColorScripts(
    private val rules: Rules.Colors
) {
    STRAIGHT_WHITE_LINE(
        Rules.Colors.fromString("""
            !
            B > W * 1
            WB > WW * 1
            WWB > WWW
        """.trimIndent()
        )
    ),
    RIVER_VALLEY(
        Rules.Colors.fromString("""
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
        """.trimIndent()
        )
    ),
    CHECKERBOARD(
        Rules.Colors.fromString("""
            !
            B > R * 1
            RB^BB > RB^BR
        """.trimIndent())
    ),
    MAZE_BACK_TRACKER(
        Rules.Colors.fromString("""
            ?
            B > R * 1
            RBB > GGR
            RGG > WWR
        """.trimIndent())
    )
    ;

    fun copy(): Rules.Colors = rules.copy()

    companion object {
        fun byRules(rules: Rules.Colors) = values().firstOrNull { script ->
            rules == script.rules
        }
    }
}