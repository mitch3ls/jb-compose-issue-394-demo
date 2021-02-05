package at.cdfz.jsonsplitter.components

import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import at.cdfz.jsonsplitter.controller.JsonDocument
import at.cdfz.jsonsplitter.padding
import at.cdfz.jsonsplitter.util.FilePicker
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
fun ProcessingScreen(
    documents: SnapshotStateList<JsonDocument>,
    onCancel: () -> Unit
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
                    DocumentProcessingRow(it)
                    Divider(thickness = 1.dp)
                }
            }
            Surface {
                Row(Modifier.fillMaxWidth()) {
                    Button({
                        onCancel()
                    }) {
                        Text("Cancel")
                    }
                }
            }
        }


        Spacer(Modifier.preferredHeight(padding))
    }
}