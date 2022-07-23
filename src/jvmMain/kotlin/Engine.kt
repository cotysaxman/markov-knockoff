class Engine<T> (
    private val ruleSet: RuleSet<T>
) {
    fun nextFrame(
        currentFrame: NDimensionalCollection<T>
    ): NDimensionalCollection<T> {
        val mask = ruleSet.createTransform(currentFrame) ?: return currentFrame

        return currentFrame.transform(mask)
    }
}
