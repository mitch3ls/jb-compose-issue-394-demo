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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import at.cdfz.jsonsplitter.controller.*
import at.cdfz.jsonsplitter.padding
import java.io.File

@Composable
fun DocumentRow(document: JsonDocument, onUpdate: ((JsonDocument) -> JsonDocument) -> Unit, onRemove: () -> Unit) {

    val hoveringOverPath = remember { mutableStateOf(false) }
    val showKeyDropdown = remember { mutableStateOf(false) }
    val showIdGenerationDropdown = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
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

        when (document.dataKeyState) {
            is DataKeyProcessing -> {
                Text("processing...")
            }
            is DataKeyValue -> {
                val state = document.dataKeyState
                if (state.allOptions.size == 1) {
                    Text("\"${state.key}\"")
                } else {
                    DropdownMenu(
                        toggle = {
                            Row(
                                modifier = Modifier.clickable(onClick = { showKeyDropdown.value = true }),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "\"${state.key}\"",
                                )

                                Icon(Icons.Filled.ArrowDropDown)
                            }
                        },
                        expanded = showKeyDropdown.value,
                        onDismissRequest = { showKeyDropdown.value = false }
                    ) {
                        state.allOptions.forEach {
                            DropdownMenuItem(
                                onClick = {
                                    onUpdate { document ->
                                        document.copy(
                                            dataKeyState = state.copy(
                                                key = it.key
                                            )
                                        )
                                    }
                                }
                            ) {
                                Text("\"${it.key}\"")
                            }
                        }
                    }
                }
            }
            is DataKeyIsArray -> {
                Text("global array")
            }
            is DataKeyNoneFound -> Text("no key found", color = MaterialTheme.colors.secondary)
            is DataKeyInvalidDocument -> Text("invalid document", color = MaterialTheme.colors.secondary)
        }

        when (document.idGenerationState) {
            is IdGenerationDisabled -> {
                Spacer(Modifier.preferredWidth(padding))
                Spacer(Modifier.preferredWidth(padding))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Generate ID")
                    Checkbox(
                        checked = false,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onUpdate { document ->
                                    document.copy(
                                        idGenerationState = IdGenerationEnabled("", hashFields = emptyList())
                                    )
                                }
                            }
                        }
                    )
                }
            }
            is IdGenerationEnabled -> {

                Spacer(Modifier.preferredWidth(padding))
                Spacer(Modifier.preferredWidth(padding))
                Column {

                    Spacer(Modifier.preferredHeight(padding))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Generate ID")
                        Checkbox(
                            checked = true,
                            onCheckedChange = { checked ->
                                if (!checked) {
                                    onUpdate { document ->
                                        document.copy(
                                            idGenerationState = IdGenerationDisabled()
                                        )
                                    }
                                }
                            }
                        )
                    }

                    Spacer(Modifier.preferredHeight(padding))

                    TextField(
                        value = document.idGenerationState.idField,
                        onValueChange = {
                            onUpdate { document ->
                                document.copy(
                                    idGenerationState = (document.idGenerationState as IdGenerationEnabled).copy(
                                        idField = it
                                    )
                                )
                            }
                        },
                        label = {
                            Text("name of id field")
                        }
                    )

                    Spacer(Modifier.preferredHeight(padding))

                    DropdownMenu(
                        toggle = {
                            Row(
                                modifier = Modifier.clickable(onClick = { showIdGenerationDropdown.value = true }),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "fields to hash",
                                )

                                Icon(Icons.Filled.ArrowDropDown)
                            }
                        },
                        expanded = showIdGenerationDropdown.value,
                        onDismissRequest = { showIdGenerationDropdown.value = false }
                    ) {
                        if (document.dataKeyState is WithAvailableKeys) {
                            document.dataKeyState.getAvailableFields().forEach { thisField ->

                                fun clicked() {
                                    onUpdate { document ->
                                        document.idGenerationState as IdGenerationEnabled
                                        val currentlyChecked = document.idGenerationState.hashFields.contains(thisField)

                                        val newHashFields: List<String>

                                        if (currentlyChecked) {
                                            newHashFields =
                                                document.idGenerationState.hashFields.filter { it != thisField }
                                        } else {
                                            newHashFields = document.idGenerationState.hashFields.plus(thisField)
                                        }

                                        document.copy(
                                            idGenerationState = document.idGenerationState.copy(
                                                hashFields = newHashFields
                                            )
                                        )
                                    }
                                }

                                DropdownMenuItem(onClick = { clicked() }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = document.idGenerationState.hashFields.contains(thisField),
                                            onCheckedChange = {
                                                clicked()
                                            }
                                        )
                                        Text("\"$thisField\"")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.preferredHeight(padding))
                }
            }
        }

        IconButton({
            onRemove()
        }) {
            Icon(Icons.Filled.Delete)
        }
    }
}