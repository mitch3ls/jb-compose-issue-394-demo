package at.cdfz.jsonsplitter.controller

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.Okio
import tornadofx.toProperty
import java.io.File
import javax.smartcardio.Card


class DocumentNotReadyException : Exception() {
}

class UnexpectedValueException : Exception() {
}

object Processing {

    fun parseRecords(document: JsonDocument) = sequence {
        val dataKeyState = document.dataKeyState

        if (dataKeyState !is DataKeyValue && dataKeyState !is DataKeyIsArray) {
            // document not ready
            throw DocumentNotReadyException()
        }

        try {

            val reader = jsonReaderFromPath(document.path)

            if (dataKeyState is DataKeyValue) {
                reader.beginObject()

                while (reader.nextName() != dataKeyState.key) {
                    reader.skipValue()
                }
            }

            reader.beginArray()

            while (reader.hasNext()) {
                val value = reader.readJsonValue()

                if (value !is Map<*, *>) {
                    throw UnexpectedValueException()
                }

                yield(value)
            }

            reader.endArray()
        } catch (ex: JsonDataException) {

        }
    }

    fun splitDocument(
        document: JsonDocument,
        destinationPath: String,
        chunkSize: Int,
        callback: (ProcessingState) -> Unit
    ) {
        callback(ProcessingInit())

        val sourceFile = File(document.path)
        val baseName = sourceFile.nameWithoutExtension
        val totalLength = sourceFile.length()

        var bytesWritten = 0.0

        try {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(List::class.java)

            val sequence = parseRecords(document)
            //.onEach { callback(ProcessingProgress(it.second)) }

            val chunks = sequence.chunked(chunkSize)

            var chunksWritten = 0
            for (chunk in chunks) {
                chunksWritten++
                val filename = "$baseName.$chunksWritten.json"
                val destinationFile = File(destinationPath).resolve(filename)

                val json = adapter.toJson(chunk)
                destinationFile.writeText(json)

                bytesWritten += json.length
                val progress = bytesWritten / totalLength
                callback(ProcessingProgress(progress.toProperty()))
            }

            callback(ProcessingDone())
        } catch (ex: DocumentNotReadyException) {
            callback(ProcessingError())
        }
    }

    fun findPossibleDataKeys(path: String, callback: (DataKeyState) -> Unit) {
        try {

            val reader = jsonReaderFromPath(path)

            val firstToken = reader.peek()

            when (firstToken) {
                JsonReader.Token.BEGIN_ARRAY -> {
                    val fields = findPossibleRecordFields(reader)

                    if (fields.isEmpty()) {
                        callback(DataKeyNoneFound())
                        return
                    }

                    callback(DataKeyIsArray(fields))
                }
                JsonReader.Token.BEGIN_OBJECT -> {
                    val possibleKeys = mutableMapOf<String, List<String>>()

                    reader.beginObject()

                    while (reader.hasNext()) {
                        val key = reader.nextName()
                        val peekReader = reader.peekJson()
                        val fields = findPossibleRecordFields(peekReader)

                        reader.skipValue()

                        if (fields.isNullOrEmpty()) {
                            continue
                        }

                        possibleKeys.put(key, fields)
                    }

                    if (possibleKeys.isNotEmpty()) {
                        callback(DataKeyValue(possibleKeys.keys.first(), possibleKeys))
                    } else {
                        callback(DataKeyNoneFound())
                    }
                }

                else -> callback(DataKeyInvalidDocument())
            }
        } catch (ex: JsonDataException) {
            callback(DataKeyInvalidDocument())
            println(ex.message)
        }
    }

    fun findPossibleRecordFields(reader: JsonReader): List<String> {
        val possibleRecordFields = mutableListOf<String>()


        reader.beginArray()
        reader.beginObject()

        while (reader.hasNext()) {
            val name = reader.nextName()
            reader.skipValue()

            possibleRecordFields.add(name)
        }

        reader.endObject()
        // discard reader

        return possibleRecordFields
    }

    fun jsonReaderFromPath(path: String): JsonReader {
        val file = File(path)
        val source = Okio.source(file)
        val bufferedSource = Okio.buffer(source)

        bufferedSource.inputStream().available()

        return JsonReader.of(bufferedSource)
    }
}