package at.cdfz.jsonsplitter.controller

import java.io.File

data class JsonDocument(
    val file: File,
    val dataKeyState: DataKeyState,
    val idGenerationState: IdGenerationState,
    val processingState: ProcessingState
) {
    override fun equals(other: Any?) = (other is JsonDocument)
            && file == other.file

    override fun hashCode(): Int {
        return file.hashCode()
    }

    fun isReady(): Boolean {
        val splittingPossible = dataKeyState is DataKeyState.ObjectLike || dataKeyState is DataKeyState.Array

        if (idGenerationState is IdGenerationState.Enabled) {
            return splittingPossible && idGenerationState.idField.isNotEmpty() && idGenerationState.hashFields.isNotEmpty()
        }

        return splittingPossible
    }
}