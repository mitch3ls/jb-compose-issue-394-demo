package at.cdfz.jsonsplitter

import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DesktopDialogProperties
import androidx.compose.ui.window.Dialog
import at.cdfz.jsonsplitter.components.InfoScreen
import at.cdfz.jsonsplitter.components.MainScreen
import at.cdfz.jsonsplitter.components.ProcessingScreen
import at.cdfz.jsonsplitter.components.ViewBase
import at.cdfz.jsonsplitter.controller.*
import at.cdfz.jsonsplitter.util.Processing
import at.cdfz.jsonsplitter.util.Processing.findPossibleDataKeys
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.imageio.ImageIO

val padding = 10.dp

val executor: ExecutorService = run {
    val cores = Runtime.getRuntime().availableProcessors()
    Executors.newFixedThreadPool(cores)
}

fun main() {
    val image = getWindowIcon()

    Window(title = "CDFZ ToolBox", size = IntSize(800, 600), icon = image) {

        val showProcessingScreen = remember { mutableStateOf(false) }
        val showInfoDialog = remember { mutableStateOf(false) }

        val documents = remember { mutableStateListOf<JsonDocument>() }
        val recordsPerFile = remember { mutableStateOf<Int?>(100) }
        val prettyPrintJson = remember { mutableStateOf(false) }
        val outputPath = remember { mutableStateOf("") }

        val runningProcessingJobs = remember { mutableStateOf<List<Future<*>>>(emptyList()) }

        fun replaceDocument(current: JsonDocument, new: JsonDocument): JsonDocument? {
            val index = documents.indexOfFirst { it == current }

            if (index == -1) {
                return null
            }

            documents[index] = new
            return new
        }

        fun updateDocument(oldDocument: JsonDocument, updateFunction: (JsonDocument) -> JsonDocument) {
            val currentDocumentVersion = documents.find { it == oldDocument } ?: return
            val newDocument = updateFunction(currentDocumentVersion)
            replaceDocument(currentDocumentVersion, newDocument)
        }

        fun startInitialProcessing(document: JsonDocument) {
            var workingDocument = document.copy(
                dataKeyState = DataKeyProcessing(0.0F),
                idGenerationState = IdGenerationProcessing()
            )
            replaceDocument(document, workingDocument)

            val worker = Runnable {
                findPossibleDataKeys(workingDocument.file) { dataKeyState ->

                    val idGenerationState = when (dataKeyState) {
                        is DataKeyIsArray, is DataKeyValue -> IdGenerationDisabled()
                        else -> IdGenerationProcessing()
                    }

                    val newDocument = workingDocument.copy(
                        dataKeyState = dataKeyState,
                        idGenerationState = idGenerationState
                    )

                    workingDocument = replaceDocument(workingDocument, newDocument) ?: return@findPossibleDataKeys
                }
            }

            executor.execute(worker)
        }

        fun addFile(file: File) {
            val document = JsonDocument(file, DataKeyInit(), IdGenerationInit(), ProcessingInit())
            documents.add(document)
            startInitialProcessing(document)
        }

        fun startProcessing() {
            val workers = documents
                .filter { document -> document.dataKeyState is DataKeyValue || document.dataKeyState is DataKeyIsArray }
                .map { document ->
                    Runnable {
                        Processing.splitDocument(
                            document,
                            outputPath.value,
                            recordsPerFile.value!!,
                            prettyPrintJson.value
                        ) { processingState ->

                            updateDocument(document) { document ->
                                document.copy(processingState = processingState)
                            }
                        }
                    }
                }

            runningProcessingJobs.value = workers.map { executor.submit(it) }
        }

        fun cancelProcessing() {
            runningProcessingJobs.value.forEach { it.cancel(true) }

            showProcessingScreen.value = false
        }

        ZentDokTheme {
            ViewBase(onInfoClicked = { showInfoDialog.value = true }) {
                if (showProcessingScreen.value) {
                    ProcessingScreen(documents, onCancel = ::cancelProcessing)
                } else {

                    MainScreen(
                        documents,
                        recordsPerFile,
                        prettyPrintJson,
                        outputPath,
                        addFile = ::addFile,
                        updateDocument = ::updateDocument,
                        onProcessingStart = {
                            showProcessingScreen.value = true
                            startProcessing()
                        }
                    )
                }
            }
        }

        if (showInfoDialog.value) {
            Dialog(
                onDismissRequest = { showInfoDialog.value = false }, // somehow this doesn't work
                properties = DesktopDialogProperties(size = IntSize(400, 350), title = "Info")
            ) {
                ZentDokTheme {
                    InfoScreen(onOkClicked = { showInfoDialog.value = false })
                }
            }
        }
    }
}

fun getWindowIcon(): BufferedImage {
    val imageFile = File("src/main/resources/images/owl.png")
    var image: BufferedImage? = null
    try {
        image = ImageIO.read(imageFile)
    } catch (e: Exception) {
        // image file does not exist
        println(imageFile.canonicalPath)
        println(e)
    }

    if (image == null) {
        image = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    }

    return image
}