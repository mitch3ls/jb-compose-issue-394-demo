package at.cdfz.jsonsplitter.controller

import at.cdfz.jsonsplitter.controller.Processing.findPossibleDataKeys
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.Controller
import tornadofx.asObservable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ProcessingController : Controller() {
    val documents = ArrayList<JsonDocument>().asObservable()
    val destinationPath = SimpleStringProperty()
    val recordsPerShard = SimpleIntegerProperty(100)

    val executor: ExecutorService = run {
        val cores = Runtime.getRuntime().availableProcessors()
        Executors.newFixedThreadPool(cores)
    }

    fun addDocument(document: JsonDocument) {
        documents.add(document)

        document.dataKeyState = DataKeyProcessing(0.0)
        document.idGenerationState = IdGenerationProcessing()
        val worker = Runnable() {
            findPossibleDataKeys(document.path) { dataKeyState ->
                document.dataKeyState = dataKeyState

                when (dataKeyState) {
                    is DataKeyValue -> document.idGenerationState = IdGenerationDisabled(document)
                    is DataKeyIsArray -> document.idGenerationState = IdGenerationDisabled(document)
                }
            }
        }

        executor.execute(worker)
    }

    fun removeDocument(path: String) {
        documents.removeIf { it.path.equals(path) }
    }

    fun setKeyEverywhereIfPossible(key: String) {
        for (document in documents) {
            when (document.dataKeyState) {
                is DataKeyValue -> {
                    if (key in (document.dataKeyState as DataKeyValue).allOptions.keys) {
                        document.dataKeyState = DataKeyValue(key, (document.dataKeyState as DataKeyValue).allOptions)
                    }
                }
            }
        }
    }

    fun startProcessing() {
        val workers = documents
            .filter { document -> document.dataKeyState is DataKeyValue || document.dataKeyState is DataKeyIsArray }
            .map {
                Runnable {
                    Processing.splitDocument(
                        it,
                        this.destinationPath.get(),
                        this.recordsPerShard.get(),
                    ) { processingState ->
                        // HACK needed for progressbar in UI to work (I know - eewwww)
                        if (it.processingState is ProcessingProgress && processingState is ProcessingProgress) {
                            (it.processingState as ProcessingProgress).progress.set(processingState.progress.get())
                        } else {
                            it.processingState = processingState
                        }
                    }
                }
            }

        workers.forEach { executor.execute(it) }
    }
}