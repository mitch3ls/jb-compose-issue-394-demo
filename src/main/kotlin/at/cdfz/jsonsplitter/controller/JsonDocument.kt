package at.cdfz.jsonsplitter.controller

import tornadofx.*
import java.io.File

abstract class DataKeyState {}
class DataKeyInit : DataKeyState() {}
class DataKeyProcessing : DataKeyState() {}
class DataKeyIsArray : DataKeyState() {}
class DataKeyNoneFound : DataKeyState() {}
class DataKeyValue(key: String, val allOptions: List<String>) : DataKeyState() {
    var key by property(key)
    fun keyProperty() = getProperty(DataKeyValue::key)
}

class JsonDocument(private val file: File) {
    val path: String get() = file.canonicalPath
    var dataKeyState: DataKeyState = DataKeyInit()
}