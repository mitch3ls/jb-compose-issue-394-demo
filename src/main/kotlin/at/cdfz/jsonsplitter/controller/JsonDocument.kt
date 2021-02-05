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
}

interface WithAvailableKeys {
    fun getAvailableFields(): List<String>
}

abstract class DataKeyState
class DataKeyInit : DataKeyState()
data class DataKeyProcessing(val progress: Float) : DataKeyState()
class DataKeyNoneFound : DataKeyState()
class DataKeyInvalidDocument : DataKeyState()
data class DataKeyIsArray(val fields: List<String>) : DataKeyState(), WithAvailableKeys {
    override fun getAvailableFields(): List<String> {
        return fields
    }
}
data class DataKeyValue(val key: String, val allOptions: Map<String, List<String>>) : DataKeyState(),
    WithAvailableKeys {
    override fun getAvailableFields(): List<String> {
        return allOptions[key] ?: emptyList()
    }
}

abstract class IdGenerationState
class IdGenerationInit : IdGenerationState()
class IdGenerationProcessing : IdGenerationState()
class IdGenerationDisabled : IdGenerationState()
data class IdGenerationEnabled(
    val idField: String,
    val hashFields: List<String>
) : IdGenerationState()

abstract class ProcessingState
class ProcessingInit : ProcessingState()
class ProcessingError : ProcessingState()
class ProcessingProgress(val progress: Float) : ProcessingState()
class ProcessingDone : ProcessingState()