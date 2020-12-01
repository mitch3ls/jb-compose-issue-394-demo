package at.cdfz.jsonsplitter.controller

import javafx.beans.property.Property
import javafx.collections.ObservableList
import tornadofx.*
import java.io.File

abstract class DataKeyState
class DataKeyInit : DataKeyState()
class DataKeyProcessing(val progress: Double) : DataKeyState()
class DataKeyNoneFound : DataKeyState()
class DataKeyInvalidDocument : DataKeyState()
class DataKeyIsArray(val fields: List<String>) : DataKeyState()
class DataKeyValue(key: String, val allOptions: Map<String, List<String>>) : DataKeyState() {
    var key: String by property(key)
    fun keyProperty() = getProperty(DataKeyValue::key)
}

abstract class IdGenerationState
class IdGenerationInit : IdGenerationState()
class IdGenerationProcessing : IdGenerationState()
class IdGenerationDisabled(val document: JsonDocument /* HACK */) : IdGenerationState()
class IdGenerationEnabled(
    val idField: Property<String>,
    val availableFields: List<String>,
    val hashFields: ObservableList<String>,
    val document: JsonDocument /* HACK */
) : IdGenerationState()

class JsonDocument(private val file: File) {
    val path: String get() = file.canonicalPath

    var dataKeyState by property<DataKeyState>(DataKeyInit())
    fun dataKeyStateProperty() = getProperty(JsonDocument::dataKeyState)

    var idGenerationState by property<IdGenerationState>(IdGenerationInit())
    fun idGenerationStateProperty() = getProperty(JsonDocument::idGenerationState)

    init {
        dataKeyStateProperty().onChange {
            val idGenerationState = this.idGenerationState

            if (idGenerationState is IdGenerationEnabled) {
                val newAvailableFields = getAvailableFields()

                this.idGenerationState = IdGenerationEnabled(
                    idGenerationState.idField,
                    newAvailableFields,
                    idGenerationState.hashFields,
                    this
                )
            }
        }
    }

    fun getAvailableFields(): List<String> {
        val dataKeyState = this.dataKeyState
        val availableFields = when (dataKeyState) {
            is DataKeyValue -> dataKeyState.allOptions[dataKeyState.key].orEmpty()
            is DataKeyIsArray -> dataKeyState.fields
            else -> emptyList()
        }
        return availableFields
    }
}