package at.cdfz.jsonsplitter.util

import at.cdfz.jsonsplitter.controller.*
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.Okio
import java.io.File
import java.security.MessageDigest


class DocumentNotReadyException : Exception() {
}

class UnexpectedValueException : Exception() {
}

object Processing {

    fun parseRecords(document: JsonDocument) = sequence {
        val dataKeyState = document.dataKeyState
        val idGenerationState = document.idGenerationState

        if (dataKeyState !is DataKeyValue && dataKeyState !is DataKeyIsArray) {
            // document not ready
            throw DocumentNotReadyException()
        }

        try {

            val reader = jsonReaderFromFile(document.file)

            if (dataKeyState is DataKeyValue) {
                reader.beginObject()

                while (reader.nextName() != dataKeyState.key) {
                    reader.skipValue()
                }
            }

            reader.beginArray()

            while (reader.hasNext()) {
                val readValue = reader.readJsonValue()

                if (readValue !is Map<*, *>) {
                    throw UnexpectedValueException()
                }

                val record = readValue.toMutableMap()

                if (idGenerationState is IdGenerationEnabled) {
                    val id = generateId(document, record)

                    record[idGenerationState.idField] = id
                }

                yield(record)
            }

            reader.endArray()
        } catch (ex: JsonDataException) {

        }
    }

    fun splitDocument(
        document: JsonDocument,
        destinationPath: String,
        chunkSize: Int,
        prettyPrint: Boolean,
        callback: (ProcessingState) -> Unit
    ) {
        callback(ProcessingInit())

        val sourceFile = document.file
        val baseName = sourceFile.nameWithoutExtension
        val totalLength = sourceFile.length()

        var bytesWritten = 0.0

        try {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            var adapter = moshi.adapter(List::class.java)

            if (prettyPrint) {
                adapter = adapter.indent("  ")
            }

            val sequence = parseRecords(document)

            val chunks = sequence.chunked(chunkSize)

            var chunksWritten = 0
            for (chunk in chunks) {
                chunksWritten++
                val filename = "$baseName.$chunksWritten.json"
                val destinationFile = File(destinationPath).resolve(filename)

                val json = adapter.toJson(chunk)
                destinationFile.writeText(json)

                bytesWritten += json.length
                val progress = (bytesWritten / totalLength).toFloat()
                callback(ProcessingProgress(progress))
            }

            callback(ProcessingDone())
        } catch (ex: DocumentNotReadyException) {
            callback(ProcessingError())
        }
    }

    fun findPossibleDataKeys(file: File, callback: (DataKeyState) -> Unit) {
        try {

            val reader = jsonReaderFromFile(file)

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

    fun jsonReaderFromFile(file: File): JsonReader {
        val source = Okio.source(file)
        val bufferedSource = Okio.buffer(source)

        bufferedSource.inputStream().available()

        return JsonReader.of(bufferedSource)
    }

    fun generateId(document: JsonDocument, record: Map<*, *>): String {
        val idGenerationState = document.idGenerationState

        if (idGenerationState !is IdGenerationEnabled) {
            throw DocumentNotReadyException()
        }

        // sort fields for repeatability
        val fields = idGenerationState.hashFields.sorted()

        val byteMessages = fields
            .map { record[it].toString().toByteArray() }

        val md = MessageDigest.getInstance("SHA-1")

        for (message in byteMessages) {
            md.update(message)
        }

        val digest = md.digest()
        return digest.toHex()
    }
}