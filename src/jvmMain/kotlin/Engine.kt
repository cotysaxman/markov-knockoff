class Engine<T> (
    private val rules: Rules<T>
) {
    fun nextFrame(
        currentFrame: Tiles<T>
    ): Tiles<T> {
        val mask = rules.createTransform(currentFrame) ?: return currentFrame

        return currentFrame.transform(mask)
    }
}
