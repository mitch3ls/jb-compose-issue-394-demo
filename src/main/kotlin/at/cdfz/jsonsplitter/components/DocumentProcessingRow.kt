package at.cdfz.jsonsplitter.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.style.TextOverflow
import at.cdfz.jsonsplitter.controller.*
import at.cdfz.jsonsplitter.padding
import java.io.File

@Composable
fun DocumentProcessingRow(document: JsonDocument) {

    val hoveringOverPath = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.preferredWidth(padding))

        Row(Modifier.weight(1.0F)) {
            PathRow {
                Text(
                    modifier = Modifier.pointerMoveFilter(
                        onEnter = {
                            hoveringOverPath.value = true
                            false
                        },
                        onExit = {
                            hoveringOverPath.value = false
                            false
                        }
                    ),
                    text = document.file.parentFile?.path ?: "",
                    maxLines = if (hoveringOverPath.value) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
                Text(File.separator + document.file.name)
            }
        }

        Spacer(Modifier.preferredWidth(padding))
        Spacer(Modifier.preferredWidth(padding))

        when (document.processingState) {
            is ProcessingInit -> Text("pending...")
            is ProcessingProgress -> LinearProgressIndicator(progress = document.processingState.progress)
            is ProcessingDone -> Text("done")
            is ProcessingError -> Text("ERROR", color = MaterialTheme.colors.secondary)
        }
    }
}