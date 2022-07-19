import kotlin.math.pow

data class Tiles<T>(
    private val data: List<T>,
    private val edges: List<Int> = listOf(data.size)
): Sequence<Tiles<T>> {
    constructor(
        default: T,
        edges: List<Int>
    ) : this(
        List(edges.reduce(Int::times)) { default },
        edges
    )

    private val dimensions = edges.size

    val value = data[0]

    override fun iterator(): Iterator<Tiles<T>> {
        return when (edges.size) {
            0 -> sequenceOf(this).iterator()
            1 -> data.asSequence().map {
                Tiles(listOf(it), emptyList())
            }.iterator()

            else -> edges[0].let { edgeLength ->
                (0 until data.size / edgeLength).map { index ->
                    val dataSegment = data.subList(
                        fromIndex = index * edgeLength,
                        toIndex = (index + 1) * edgeLength
                    )
                    Tiles(dataSegment, edges.drop(1))
                }.iterator()
            }
        }
    }

    fun sizeInDimension(dimension: Int): Int =
        if (dimension in 1 .. dimensions) {
            edges[dimension - 1]
        } else throw IndexOutOfBoundsException()

    fun transform(mask: Map<Int, T>): Tiles<T> =
        copy(
            data = List(data.size) { index ->
                mask.getOrDefault(index, data[index])
            }
        )

    fun find(
        subList: List<T>
    ): List<List<Int>> =
        (1 .. dimensions).flatMap { dimension ->
            Direction.values().map {
                dimension to it
            }
        }.flatMap { (dimension, direction) ->
            findMatches(subList, dimension, direction)
        }

    private fun findMatches(
        subList: List<T>,
        dimension: Int,
        direction: Direction
    ): List<List<Int>> {
        val edgeLength = edges.getOrNull(dimension - 1) ?: return emptyList()

        val searchOffsets = subList.scan(0) { previous, _ ->
            previous.nextIndex(edgeLength, dimension, direction)
        }.dropLast(1)

        return data.indices.mapNotNull { index ->
            searchOffsets.mapIndexed { position, indexOffset ->
                val dimensionSize = edgeLength.toDouble().pow(dimension).toInt()
                val dimensionFloor = (index / dimensionSize) * dimensionSize
                val dimensionCeiling = (index / dimensionSize + 1) * dimensionSize
                val searchIndex = index + indexOffset
                if (searchIndex !in dimensionFloor until dimensionCeiling) {
                    return@mapNotNull null
                }
                val other = data.getOrNull(searchIndex) ?: return@mapNotNull null
                if (subList[position] != other) {
                    return@mapNotNull null
                }

                searchIndex
            }
        }
    }

    enum class Direction { FORWARD, BACKWARD }

    private fun Int.nextIndex(
        edgeLength: Int,
        dimension: Int = 1,
        direction: Direction = Direction.FORWARD
    ): Int {
        val offset = edgeLength.toDouble()
            .pow(dimension - 1).toInt()

        return when(direction) {
            Direction.FORWARD -> this + offset
            Direction.BACKWARD -> this - offset
        }
    }
}