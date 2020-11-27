package at.cdfz.jsonsplitter.controller

import com.beust.klaxon.JsonReader
import com.beust.klaxon.KlaxonException
import tornadofx.*
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.concurrent.thread

class ProcessingController : Controller() {
    val documents = ArrayList<JsonDocument>().asObservable()

    fun addDocument(document: JsonDocument) {
        documents.add(document)

        document.dataKeyState = DataKeyProcessing(0.0)
        thread() {
            findPossibleDataKeys(document.path) { dataKeyState ->
                document.dataKeyState = dataKeyState
            }
        }

    }

    fun removeDocument(path: String) {
        documents.removeIf { it.path.equals(path) }
    }

    fun setKeyEverywhereIfPossible(key: String) {
        for (document in documents) {
            when (document.dataKeyState) {
                is DataKeyValue -> {
                    println("Change for ${document.path}")
                    if (key in (document.dataKeyState as DataKeyValue).allOptions.keys) {
                        document.dataKeyState = DataKeyValue(key, (document.dataKeyState as DataKeyValue).allOptions)
                    }
                }
            }
        }
    }

    fun findPossibleDataKeys(path: String, callback: (DataKeyState) -> Unit) {

        val possibleKeys = HashMap<String, List<String>>()

        val fileStream = FileInputStream(path)
        val streamReader = InputStreamReader(fileStream)

        val totalFileSize = fileStream.channel.size().toDouble()

        JsonReader(streamReader).use { reader ->
            try {
                // check if JSON has global object
                reader.beginObject {
                    while (reader.hasNext()) {
                        // read property name
                        val readName = reader.nextName()
                        try {
                            var keys: List<String> = emptyList()
                            // check if value is array
                            reader.beginArray {

                                try {
                                    // check if first element is object
                                    val record = reader.nextObject()
                                    keys = record.keys.map { it.toString() }
                                } catch (ex: KlaxonException) {
                                    // no object, it seems
                                    println("no object")
                                }

                                // HACK: skip rest of array
                                var arrayLevelCounter = 0
                                var progressNotificationCounter = 0
                                while (true) {
                                    val nextToken = reader.lexer.peek().toString()

                                    // only update progress every 100 tokens
                                    if (++progressNotificationCounter == 100) {
                                        val progress = fileStream.channel.position() / totalFileSize
                                        callback(DataKeyProcessing(progress))

                                        progressNotificationCounter = 0
                                    }

                                    when {
                                        nextToken == "[" -> arrayLevelCounter++
                                        nextToken == "]" && arrayLevelCounter > 0 -> arrayLevelCounter--
                                        nextToken == "]" && arrayLevelCounter == 0 -> {
                                            break
                                        }
                                    }
                                    reader.lexer.next()
                                }
                                callback(DataKeyProcessing(1.0))
                            }

                            // didn't throw exception, so it must be a valid key
                            possibleKeys.put(readName, keys)
                        } catch (ex: KlaxonException) {
                            // property is no array, skip
                            println("internal $ex")
                            continue
                        }
                    }
                }

                if (possibleKeys.size > 0) {
                    println(possibleKeys)
                    callback(DataKeyValue(possibleKeys.keys.first(), possibleKeys))
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