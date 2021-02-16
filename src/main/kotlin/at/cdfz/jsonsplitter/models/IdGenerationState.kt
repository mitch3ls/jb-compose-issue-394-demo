package at.cdfz.jsonsplitter.models

sealed class IdGenerationState {
    object Init : IdGenerationState()
    object Processing : IdGenerationState()
    object Unavailable: IdGenerationState()
    object Disabled : IdGenerationState()
    data class Enabled(
        val idField: String,
        val hashFields: List<String>
    ) : IdGenerationState()
}