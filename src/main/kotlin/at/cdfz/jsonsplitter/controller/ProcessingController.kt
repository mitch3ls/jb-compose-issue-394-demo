package at.cdfz.jsonsplitter.controller

import com.beust.klaxon.JsonReader
import com.beust.klaxon.KlaxonException
import com.beust.klaxon.token.Token
import tornadofx.*
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStreamReader

class ProcessingController : Controller() {
    val documents = ArrayList<JsonDocument>().asObservable()

    fun addDocument(document: JsonDocument) {
        documents.add(document)

        document.dataKeyState = DataKeyProcessing()
        findPossibleDataKeys(document.path) { dataKeyState ->
            document.dataKeyState = dataKeyState

        }
    }

    fun removeDocument(path: String) {
        documents.removeIf { it.path.equals(path) }
    }

    fun findPossibleDataKeys(path: String, callback: (DataKeyState) -> Unit) {

        val possibleKeys = ArrayList<String>()

        val fileStream = FileInputStream(path)
        val streamReader = InputStreamReader(fileStream)

        println(path)

        JsonReader(streamReader).use { reader ->
            try {
                // check if JSON has global object
                reader.beginObject {
                    while (reader.hasNext()) {
                        // read property name
                        val readName = reader.nextName()
                        try {
                            // check if value is array
                            reader.beginArray {
                                var arrayLevelCounter = 0
                                while (true) {
                                    // this is a hack, skip rest of array
                                    val nextToken = reader.lexer.peek().toString()

                                    when {
                                        nextToken == "[" -> arrayLevelCounter++
                                        nextToken == "]" && arrayLevelCounter > 0 -> arrayLevelCounter--
                                        nextToken == "]" && arrayLevelCounter == 0 -> {
                                            break
                                        }
                                    }
                                    reader.lexer.next()
                                }
                            }

                            possibleKeys.add(readName)
                        } catch (ex: KlaxonException) {
                            // property is no array, skip
                            println("internal $ex")
                            continue
                        }
                    }
                }

                if (possibleKeys.size > 0) {
                    callback(DataKeyValue(possibleKeys[0], possibleKeys))
                } else {
                    callback(DataKeyNoneFound())
                }
            } catch (ex: KlaxonException) {
                // so it's not a global object -> must be an array
                println(ex)
                callback(DataKeyIsArray())
            }
        }
    }
}