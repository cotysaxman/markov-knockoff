import androidx.compose.ui.graphics.Color

sealed class Rule<T> {
    abstract fun findMatches(
        searchIn: NDimensionalCollection<T>
    ): List<Pair<List<Int>, NDimensionalCollection<T>>>?

    abstract fun toString(charMap: Map<Char, T>): String

    data class Single<T>(private val rule: Pair<NDimensionalCollection<T>, NDimensionalCollection<T>>): Rule<T>() {
        override fun findMatches(
            searchIn: NDimensionalCollection<T>
        ): List<Pair<List<Int>, NDimensionalCollection<T>>>? =
            rule.let { (searchFor, replaceWith) ->
                val matchingIndexLists = searchIn.find(searchFor)
                if (matchingIndexLists.isEmpty()) {
                    return null
                } else {
                    return matchingIndexLists.map { it to replaceWith }
                }
            }

        override fun toString(charMap: Map<Char, T>): String =
            rule.toList().joinToString(" > ") { nDimensionalCollection ->
                val data = nDimensionalCollection.flattenedSequence().toList()
                val edges = nDimensionalCollection.edges
                val data2d = when (nDimensionalCollection.edges.size) {
                    1 -> listOf(data)
                    else -> {
                        val thisEdge = edges[0]
                        val otherEdges = edges.drop(1)
                        val remainingDimension = otherEdges.reduce(Int::times)
                        (0 until thisEdge).map { index ->
                            data.subList(
                                fromIndex = index * remainingDimension,
                                toIndex = (index + 1) * remainingDimension
                            ).toList()
                        }
                    }
                }
                data2d.joinToString(" ^ ") { chunk ->
                    chunk.map { element ->
                        charMap.keys.firstOrNull { keyChar ->
                            charMap[keyChar] == element
                        } ?: throw InvalidRuleDefinition()
                    }.joinToString("")
                }
            }
    }

    data class Composite<T>(private val rules: List<Rule<T>>): Rule<T>() {
        override fun findMatches(
            searchIn: NDimensionalCollection<T>
        ): List<Pair<List<Int>, NDimensionalCollection<T>>>? =
            rules.flatMap { rule ->
                rule.findMatches(searchIn) ?: emptyList()
            }.takeIf { it.isNotEmpty() }


        override fun toString(charMap: Map<Char, T>): String =
            rules.joinToString(" | ") { rule ->
                rule.toString(charMap)
            }
    }

    data class Limited<T>(
        private val times: Int,
        private val rule: Rule<T>
    ): Rule<T>() {
        private var timesSelected = 0
        override fun findMatches(
            searchIn: NDimensionalCollection<T>
        ): List<Pair<List<Int>, NDimensionalCollection<T>>>? {
            if (timesSelected >= times) {
                return null
            }
            val results = rule.findMatches(searchIn)
                ?.takeIf { it.isNotEmpty() } ?: return null

            timesSelected += 1
            return results
        }

        override fun toString(charMap: Map<Char, T>): String = "${rule.toString(charMap)} * $times"
    }

    data class Sequential<T>(private val rules: List<Rule<T>>) : Rule<T>() {
        private var passedIndices = 0

        override fun findMatches(
            searchIn: NDimensionalCollection<T>
        ): List<Pair<List<Int>, NDimensionalCollection<T>>>? {
            val (results, index) = rules.drop(passedIndices).asSequence().mapIndexedNotNull { index, rule ->
                val matches = rule.findMatches(searchIn)
                    ?.takeIf { it.isNotEmpty() } ?: return@mapIndexedNotNull null

                matches to index
            }.firstOrNull() ?: run {
                passedIndices = rules.size
                return null
            }
            passedIndices += index

            return results
        }

        override fun toString(charMap: Map<Char, T>): String {
            val rulesString = rules.joinToString(separator = "\n") { it.toString(charMap) }
            return "!\n$rulesString"
        }
    }


    data class Markov<T>(private val rules: List<Rule<T>>) : Rule<T>() {
        override fun findMatches(
            searchIn: NDimensionalCollection<T>
        ): List<Pair<List<Int>, NDimensionalCollection<T>>>? = rules.asSequence().mapNotNull { rule ->
            rule.findMatches(searchIn)
                ?.takeIf { it.isNotEmpty() }
        }.firstOrNull()

        override fun toString(charMap: Map<Char, T>): String {
            val rulesString = rules.joinToString(separator = "\n") { it.toString(charMap) }
            return "?\n$rulesString"
        }
    }
}

sealed class Rules<T> {
    fun createTransform(
        searchIn: NDimensionalCollection<T>
    ): Map<Int, T>? {
        val matches = ruleList.asSequence().mapNotNull { rule ->
            rule.findMatches(searchIn)
        }.firstOrNull()
            ?: return null

        return matches.random().let { (candidateIndexList, replacementItems) ->
            val replacements = replacementItems.flattenedSequence().toList()
            candidateIndexList.mapIndexed { index, dataIndex ->
                dataIndex to replacements[index]
            }.toMap()
        }
    }

    internal abstract val ruleList: List<Rule<T>>
    abstract override fun toString(): String

    sealed class RulesCompanion<T> {
        abstract val parseMap: Map<Char, T>
        abstract fun fromString(raw: String): Rules<T>

        internal fun parseRules(raw: String): List<Rule<T>> =
            raw.replace("!\n", ",!")
                .replace("?\n", ",?")
                .splitToSequence(',')
                .drop(1)
                .map { node ->
                    val nodeConstructor: (List<Rule<T>>) -> Rule<T> = when (node.first()) {
                        '!' -> { rules -> Rule.Sequential(rules) }
                        '?' -> { rules -> Rule.Markov(rules) }
                        else -> throw InvalidRuleDefinition()
                    }
                    val nodeContents = node.drop(1).split('\n')
                        .map { ruleLine ->
                            val timesSplit = ruleLine.split('*')
                            val singleRules = timesSplit[0].split('|')
                                .map { ruleComponents ->
                                    ruleComponents.split('>')
                                        .map { clauses ->
                                            val components = clauses.trim().split('^').map(String::trim)
                                            components.joinToString("") to
                                                    listOfNotNull(
                                                        components.first().length,
                                                        components.size.takeIf { it > 1 },
                                                    )
                                        }.map { (rawData, edges) ->
                                            val parsedData = rawData.map { rawCharacter ->
                                                parseMap[rawCharacter] ?: throw InvalidRuleDefinition()
                                            }
                                            NDimensionalCollection(parsedData, edges)
                                        }.let { (lhv, rhv) ->
                                            Rule.Single(lhv to rhv)
                                        }
                                }

                            val baseRule = when (singleRules.size) {
                                in 2..Int.MAX_VALUE -> Rule.Composite(singleRules)
                                1 -> singleRules[0]
                                else -> throw InvalidRuleDefinition()
                            }

                            when (timesSplit.size) {
                                2 -> Rule.Limited((timesSplit[1].trim().toInt()), baseRule)
                                1 -> baseRule
                                else -> throw InvalidRuleDefinition()
                            }
                        }
                    @Suppress("UNCHECKED_CAST")
                    return@map nodeConstructor(nodeContents as List<Rule<T>>)
                }.toList()
    }

    data class Colors(override val ruleList: List<Rule<Color>>) : Rules<Color>() {
        override fun toString(): String =
            ruleList.joinToString("\n") { rule ->
                rule.toString(parseMap)
            }

        companion object: RulesCompanion<Color>() {
            override fun fromString(raw: String) = Colors(parseRules(raw))

            override val parseMap: Map<Char, Color> = mapOf(
                'B' to Color(0xFF000000),
                'I' to Color(0xFF1D2B53),
                'P' to Color(0xFF7E2553),
                'E' to Color(0xFF008751),
                'N' to Color(0xFFAB5236),
                'D' to Color(0xFF5F574F),
                'A' to Color(0xFFC2C3C7),
                'W' to Color(0xFFFFF1E8),
                'R' to Color(0xFFFF004D),
                'O' to Color(0xFFFFA300),
                'Y' to Color(0xFFFFEC27),
                'G' to Color(0xFF00E436),
                'U' to Color(0xFF29ADFF),
                'S' to Color(0xFF83769C),
                'K' to Color(0xFFFF77A8),
                'F' to Color(0xFFFFCCAA),
            )
        }
    }
}

class InvalidRuleDefinition : Exception()
