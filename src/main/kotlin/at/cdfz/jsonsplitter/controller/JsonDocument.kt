package at.cdfz.jsonsplitter.controller

import javafx.beans.property.Property
import tornadofx.*
import java.io.File

abstract class DataKeyState {}
class DataKeyInit : DataKeyState() {}
class DataKeyProcessing(val progress: Double) : DataKeyState() {}
class DataKeyIsArray : DataKeyState() {}
class DataKeyNoneFound : DataKeyState() {}
class DataKeyValue(key: String, val allOptions: Map<String, List<String>>) : DataKeyState() {
    var key by property(key)
    fun keyProperty() = getProperty(DataKeyValue::key)
}

class JsonDocument(private val file: File) {
    val path: String get() = file.canonicalPath
    var dataKeyState by property<DataKeyState>(DataKeyInit())
    fun dataKeyStateProperty() = getProperty(JsonDocument::dataKeyState)

    var selectedFields: Set<String> = emptySet()
}