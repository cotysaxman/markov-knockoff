data class NDimensionalCollection<T>(
    private val data: List<T>,
    private val edges: List<Int> = listOf(data.size)
): Sequence<NDimensionalCollection<T>> {
    constructor(
        default: T,
        edges: List<Int>
    ) : this(
        List(edges.reduce(Int::times)) { default },
        edges
    )

    private val dimensions = edges.size

    val value = data[0]

    override fun iterator(): Iterator<NDimensionalCollection<T>> {
        return when (edges.size) {
            0 -> sequenceOf(this).iterator()
            1 -> data.asSequence().map {
                NDimensionalCollection(listOf(it), emptyList())
            }.iterator()

            else -> {
                val thisEdge = edges[0]
                val remainingDimension = edges.drop(1).reduce(Int::times)
                (0 until thisEdge).map { index ->
                    val dataSegment = data.subList(
                        fromIndex = index * remainingDimension,
                        toIndex = (index + 1) * remainingDimension
                    ).toList()
                    NDimensionalCollection(dataSegment, edges.drop(1))
                }.iterator()
            }
        }
    }

    private fun indexedSequence(): Sequence<Pair<List<Int>, T>> = sequence {
        data.asSequence().forEachIndexed { index, entry ->
            yield(coordinatesForIndex(index, edges) to entry)
        }
    }

    private fun sizeInDimension(dimension: Int): Int =
        if (dimension in 1 .. dimensions) {
            edges[dimension - 1]
        } else throw IndexOutOfBoundsException()

    fun transform(mask: Map<Int, T>): NDimensionalCollection<T> =
        copy(
            data = List(data.size) { index ->
                mask.getOrDefault(index, data[index])
            }
        )

    fun find(
        subList: List<T>
    ): List<List<Int>> =
        find(NDimensionalCollection(subList))

    private fun remapDimensions(
        dimensionMapping: List<Vector>
    ): List<Pair<NDimensionalCollection<T>, Map<Int, Int>>> {
        val newEdges = dimensionMapping.map {
            sizeInDimension(it.dimension)
        }

        val unclaimedEdges = edges.filterIndexed { index, _ ->
            index + 1 !in dimensionMapping.map(Vector::dimension)
        }
        val numberOfResults = unclaimedEdges.fold(1, Int::times)
        val unclaimedCoordinateLists = unclaimedEdges.map { edgeLength ->
            (0 until edgeLength).toList()
        }.cartesianFold()

        val unusedDimensionIterator = unclaimedEdges.indices.iterator()
        val completeDimensionMap = (1 .. dimensions).associate { localDimension ->
            val mappedIndex = dimensionMapping.indexOfFirst { it.dimension == localDimension }
            if (mappedIndex >= 0) {
                return@associate localDimension to mappedIndex
            }
            val fallbackIndex = unusedDimensionIterator.nextInt()
            return@associate localDimension to fallbackIndex + dimensionMapping.size
        }

        return List(numberOfResults) { resultIndex ->
            val keyMap = mutableMapOf<Int, Int>()
            NDimensionalCollection(
                data = newEdges.reversed().map { edgeLength ->
                    (0 until edgeLength).toList()
                }.cartesianFold().mapIndexed { index, partialCoordinateList ->
                    val compositeCombinedCoordinates = partialCoordinateList + unclaimedCoordinateLists[resultIndex]
                    val localizedDirectionlessCoordinates = (1 .. dimensions).map { localDimension ->
                        compositeCombinedCoordinates[completeDimensionMap[localDimension]!!]
                    }
                    val directionalScalars = (1 .. dimensions).map { localDimension ->
                        dimensionMapping.firstOrNull {
                            it.dimension == localDimension
                        }?.direction?.scalar ?: 1
                    }
                    val localizedCoordinates = applyScalars(
                        localizedDirectionlessCoordinates,
                        edges,
                        directionalScalars
                    ).reversed()
                    val localIndex = unCheckedIndexFromCoordinates(localizedCoordinates, edges)
                    keyMap[index] = localIndex

                    data[localIndex]
                },
                edges = newEdges
            ) to keyMap.toMap()
        }
    }

    private fun find(
        other: NDimensionalCollection<T>
    ): List<List<Int>> {
        val dimensionMappings = (1 .. dimensions)
        val vectors = Direction.values().flatMap { direction ->
            dimensionMappings.map { dimension ->
                Vector(dimension, direction)
            }
        }.choose(other.dimensions).filter { vectorList ->
            vectorList.map(Vector::dimension).toSet().size == vectorList.size
        }

        return vectors.flatMap { mapping ->
            remapDimensions(mapping).flatMap { (collectionToSearch, keyMap) ->
                collectionToSearch.match(other).map { it.mapNotNull(keyMap::get) }
            }
        }
    }

    private fun match(
        other: NDimensionalCollection<T>
    ): List<List<Int>> =
        indexedSequence().map { it.first }.mapNotNull { coordinates ->
            other.indexedSequence().toList().map { (otherCoordinates, otherEntry) ->
                val offsetCoordinates = otherCoordinates.zip(coordinates, Int::plus)
                val localIndex = indexFromCoordinates(offsetCoordinates, edges) ?: return@mapNotNull null

                if (data[localIndex] != otherEntry) {
                    return@mapNotNull null
                }

                localIndex
            }
        }.toList()

    private data class Vector(
        val dimension: Int,
        val direction: Direction
    )

    enum class Direction(val scalar: Int) {
        FORWARD(1), BACKWARD(-1)
    }

    companion object {
        private fun coordinatesForIndex(index: Int, edges: List<Int>): List<Int> =
            edges.reversed()
                .fold(emptyList<Int>() to 1) { accumulated, nextEdge ->
                    (accumulated.first + ((index / accumulated.second) % nextEdge)) to
                            accumulated.second * nextEdge
                }.first

        private fun indexFromCoordinates(coordinates: List<Int>, edges: List<Int>): Int? {
            edges.reversed().zip(coordinates) { edgeLength, coordinate ->
                if (coordinate >= edgeLength) {
                    return null
                }
            }
            return unCheckedIndexFromCoordinates(coordinates, edges)
        }

        private fun unCheckedIndexFromCoordinates(coordinates: List<Int>, edges: List<Int>) =
            coordinates.zip(
                edges.reversed().runningFold(1, Int::times),
                Int::times
            ).reduce(Int::plus)

        private fun applyScalars(coordinates: List<Int>, edges: List<Int>, scalars: List<Int>) =
            coordinates.zip(edges).zip(scalars).map { (tuple, scalar) ->
                val (coordinateComponent, edgeLength) = tuple

                when (scalar) {
                    1 -> coordinateComponent
                    -1 -> edgeLength - coordinateComponent - 1
                    else -> throw IllegalArgumentException()
                }
            }
    }
}

private fun <T> Iterable<T>.choose(number: Int): List<List<T>> =
    (1 until number).fold(map(::listOf)) { accumulated, _ ->
        accumulated.flatMap { partialResult ->
            filter { it !in partialResult }.map { entry ->
                partialResult + entry
            }
        }
    }

private fun <T> List<List<T>>.cartesianFold() =
    fold<List<T>, List<List<T>>?>(null) { accumulated, next ->
        next.map { newCoordinateComponent ->
            accumulated?.flatMap { existingCoordinateComponents ->
                existingCoordinateComponents + newCoordinateComponent
            } ?: listOf(newCoordinateComponent)
        }
    }.orEmpty()
