package at.cdfz.jsonsplitter.components

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import at.cdfz.jsonsplitter.controller.JsonDocument
import at.cdfz.jsonsplitter.controller.WithAvailableKeys
import at.cdfz.jsonsplitter.padding
import at.cdfz.jsonsplitter.util.FilePicker
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

@Composable
fun MainScreen(
    documents: SnapshotStateList<JsonDocument>,
    recordsPerFile: MutableState<Int?>,
    prettyPrintJson: MutableState<Boolean>,
    outputPath: MutableState<String>,
    addFile: (File) -> Unit,
    updateDocument: (JsonDocument, (JsonDocument) -> JsonDocument) -> Unit,
    onProcessingStart: () -> Unit
) = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    Column(Modifier.fillMaxSize().padding(padding)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
                .border(width = 1.dp, brush = SolidColor(Color.Black), shape = MaterialTheme.shapes.large)
        ) {
            ScrollableColumn(Modifier.fillMaxWidth().weight(1.0F)) {
                documents.forEach {
                    DocumentRow(it,
                        onUpdate = { updateFunction ->
                            updateDocument(it, updateFunction)
                        },
                        onRemove = {
                            documents.remove(it)
                        })
                    Divider(thickness = 1.dp)
                }
            }
            Surface {
                Row(Modifier.fillMaxWidth()) {
                    Button({
                        val fd = FileDialog(null as Frame?, "Add source file", FileDialog.LOAD)
                        fd.filenameFilter = FilenameFilter { _, name -> name.endsWith(".json") }
                        fd.isVisible = true
                        fd.isMultipleMode = true

                        if (fd.file == null) {
                            return@Button
                        }

                        val chosenFile = File(fd.directory).resolve(fd.file)
                        addFile(chosenFile)
                    }) {
                        Text("Add source file")
                    }

                    Spacer(Modifier.preferredWidth(padding))

                    Button({

                        val chosenDirectory = FilePicker.chooseFolder("Add source folder") ?: return@Button

                        val files = chosenDirectory
                            .walkTopDown()
                            .filter { it.extension == "json" }
                            .toList()

                        for (file in files) {
                            addFile(file)
                        }
                    }) {
                        Text("Add source folder")
                    }
                }
            }
        }

        Spacer(Modifier.preferredHeight(padding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = (recordsPerFile.value ?: "").toString(),
                onValueChange = {
                    if (it.isEmpty()) {
                        recordsPerFile.value = null
                    }
                    recordsPerFile.value = it.toIntOrNull() ?: recordsPerFile.value
                },
                label = { Text("records per file") }
            )

            Spacer(Modifier.preferredWidth(padding))

            Column {
                Text("pretty print JSON")
                Checkbox(
                    checked = prettyPrintJson.value,
                    onCheckedChange = { prettyPrintJson.value = it }
                )
            }
        }

        Spacer(Modifier.preferredHeight(padding))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier.weight(1.0F),
                value = outputPath.value,
                onValueChange = { outputPath.value = it },
                label = {
                    Text("output directory")
                }
            )
            Spacer(Modifier.preferredWidth(padding))
            Button({
                val chosenDirectory = FilePicker.chooseFolder("Choose output directory") ?: return@Button

                outputPath.value = chosenDirectory.canonicalPath
            }) {
                Text("choose")
            }
        }

        Spacer(Modifier.preferredHeight(padding))

        Button(
            { onProcessingStart() },
            enabled = documents.isNotEmpty() &&
                    recordsPerFile.value != null &&
                    outputPath.value.isNotEmpty() &&
                    documents.all { it.dataKeyState is WithAvailableKeys }
        ) {
            Text("Split files")
        }
    }
}