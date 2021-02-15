package at.cdfz.jsonsplitter.util

import at.cdfz.jsonsplitter.controller.DataKeyState
import at.cdfz.jsonsplitter.controller.IdGenerationState
import at.cdfz.jsonsplitter.controller.JsonDocument
import at.cdfz.jsonsplitter.controller.ProcessingState
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.ByteString
import okio.Okio
import java.io.File
import java.security.MessageDigest

private val UTF8_BOM: ByteString = ByteString.decodeHex("EFBBBF")


class DocumentNotReadyException : Exception()

class UnexpectedValueException : Exception()

class UnexpectedTokenException(val token: JsonReader.Token) : Exception()

object Processing {

    private fun parseRecords(document: JsonDocument) = sequence {
        val dataKeyState = document.dataKeyState
        val idGenerationState = document.idGenerationState

        if (!document.isReady()) {
            // document not ready
            throw DocumentNotReadyException()
        }

        try {

            val reader = jsonReaderFromFile(document.file)

            if (dataKeyState is DataKeyState.ObjectLike) {
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

                if (idGenerationState is IdGenerationState.Enabled) {
                    val id = generateId(document, record)

                    record[idGenerationState.idField] = id
                }

                yield(record)
            }

            reader.endArray()
        } catch (ex: JsonDataException) {
            // TODO: proper error handling
        }
    }

    fun splitDocument(
        document: JsonDocument,
        destinationPath: String,
        chunkSize: Int,
        prettyPrint: Boolean,
        callback: (ProcessingState) -> Unit
    ) {
        callback(ProcessingState.Init)

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
                callback(ProcessingState.Progress(progress))
            }

            callback(ProcessingState.Done)
        } catch (ex: DocumentNotReadyException) {
            callback(ProcessingState.Error)
        }
    }

    sealed class ProcessingEvent {
        object IsArray : ProcessingEvent()
        object ArrayNoFieldsFound : ProcessingEvent()
        data class ArrayFieldsFound(val fields: List<String>) : ProcessingEvent()
        object IsObject : ProcessingEvent()
        data class KeyFound(val key: String, val fields: List<String>) : ProcessingEvent()
        object ObjectFinished : ProcessingEvent()
        data class Error(val ex: Exception) : ProcessingEvent()
    }

    fun findPossibleDataKeys(file: File, notify: (ProcessingEvent) -> Unit) {
        try {

            val reader = jsonReaderFromFile(file)

            val firstToken = reader.peek()

            when (firstToken) {
                JsonReader.Token.BEGIN_ARRAY -> {
                    notify(ProcessingEvent.IsArray)

                    val fields = findPossibleRecordFields(reader)

                    if (fields.isEmpty()) {
                        notify(ProcessingEvent.ArrayNoFieldsFound)
                        return
                    }

                    notify(ProcessingEvent.ArrayFieldsFound(fields))
                }
                JsonReader.Token.BEGIN_OBJECT -> {
                    notify(ProcessingEvent.IsObject)

                    val possibleKeys = mutableMapOf<String, List<String>>()

                    reader.beginObject()

                    while (reader.hasNext()) {
                        val key = reader.nextName()
                        val peekReader = reader.peekJson()
                        val fields = findPossibleRecordFields(peekReader)

                        if (fields.isNullOrEmpty()) {
                            continue
                        }

                        notify(ProcessingEvent.KeyFound(key, fields))
                        possibleKeys[key] = fields

                        reader.skipValue()
                    }

                    notify(ProcessingEvent.ObjectFinished)
                }
                else -> throw UnexpectedTokenException(firstToken)
            }
        } catch (ex: Exception) {
            notify(ProcessingEvent.Error(ex))
        }
    }

    private fun findPossibleRecordFields(reader: JsonReader): List<String> {
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

    private fun jsonReaderFromFile(file: File): JsonReader {
        val source = Okio.source(file)
        val bufferedSource = Okio.buffer(source)

        bufferedSource.inputStream().available()

        // handle UTF-8 BOM
        // taken from https://github.com/square/retrofit/blob/2583d360fa39792f87faf7f69afefaadf3944536/retrofit-converters/moshi/src/main/java/retrofit2/converter/moshi/MoshiResponseBodyConverter.java#L36
        if (bufferedSource.rangeEquals(0, UTF8_BOM)) {
            bufferedSource.skip(UTF8_BOM.size().toLong())
        }

        return JsonReader.of(bufferedSource)
    }

    private fun generateId(document: JsonDocument, record: Map<*, *>): String {
        val idGenerationState = document.idGenerationState

        if (idGenerationState !is IdGenerationState.Enabled) {
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