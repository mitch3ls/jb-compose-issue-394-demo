package at.cdfz.jsonsplitter.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.cdfz.jsonsplitter.controller.DataKeyState
import at.cdfz.jsonsplitter.controller.IdGenerationState
import at.cdfz.jsonsplitter.controller.JsonDocument
import at.cdfz.jsonsplitter.padding
import java.io.File

@Composable
fun DocumentRow(document: JsonDocument, onUpdate: ((JsonDocument) -> JsonDocument) -> Unit, onRemove: () -> Unit) {

    val hoveringOverPath = remember { mutableStateOf(false) }
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
            is DataKeyState.Init,
            is DataKeyState.Processing -> {
                Spinner(Modifier.preferredSize(25.dp))
            }
            is DataKeyState.IncompleteObject -> {
                val state = document.dataKeyState

                DataKeyField(state.key, state.allOptions, loading = true, onChange = { key ->
                    onUpdate { document ->
                        document.copy(
                            dataKeyState = state.copy(
                                key = key
                            )
                        )
                    }
                })
            }
            is DataKeyState.Object -> {
                val state = document.dataKeyState

                DataKeyField(state.key, state.allOptions, loading = false, onChange = { key ->
                    onUpdate { document ->
                        document.copy(
                            dataKeyState = state.copy(
                                key = key
                            )
                        )
                    }
                })
            }
            is DataKeyState.IncompleteArray,
            is DataKeyState.Array -> {
                Text("global array")
            }
            is DataKeyState.NoKeyFound -> Text("no key found", color = MaterialTheme.colors.secondary)
            is DataKeyState.InvalidDocument -> Text("invalid document", color = MaterialTheme.colors.secondary)
        }

        when (document.idGenerationState) {
            is IdGenerationState.Init,
            is IdGenerationState.Processing -> {
                Spacer(Modifier.preferredWidth(padding))
                Spacer(Modifier.preferredWidth(padding))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Generate ID", color = Color.LightGray)
                    Spacer(Modifier.preferredWidth(5.dp))
                    Spinner(Modifier.preferredSize(25.dp))
                }
            }
            is IdGenerationState.Unavailable -> {
                Spacer(Modifier.preferredWidth(padding))
                Spacer(Modifier.preferredWidth(padding))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Generate ID", color = Color.LightGray)
                    Spacer(Modifier.preferredWidth(5.dp))
                    Icon(Icons.Filled.Warning, modifier = Modifier.offset(y = -2.dp))
                }
            }
            is IdGenerationState.Disabled -> {
                Spacer(Modifier.preferredWidth(padding))
                Spacer(Modifier.preferredWidth(padding))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Generate ID")
                    Spacer(Modifier.preferredWidth(5.dp))
                    Checkbox(
                        checked = false,
                        onCheckedChange = { checked ->
                            if (checked) {
                                onUpdate { document ->
                                    document.copy(
                                        idGenerationState = IdGenerationState.Enabled("", hashFields = emptyList())
                                    )
                                }
                            }
                        }
                    )
                }
            }
            is IdGenerationState.Enabled -> {

                Spacer(Modifier.preferredWidth(padding))
                Spacer(Modifier.preferredWidth(padding))
                Column {

                    Spacer(Modifier.preferredHeight(padding))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Generate ID")
                        Spacer(Modifier.preferredWidth(5.dp))
                        Checkbox(
                            checked = true,
                            onCheckedChange = { checked ->
                                if (!checked) {
                                    onUpdate { document ->
                                        document.copy(
                                            idGenerationState = IdGenerationState.Disabled
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
                                    idGenerationState = (document.idGenerationState as IdGenerationState.Enabled).copy(
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
                        if (document.dataKeyState is DataKeyState.HasAvailableRecordFields) {
                            document.dataKeyState.getAvailableFields().forEach { thisField ->

                                fun clicked() {
                                    onUpdate { document ->
                                        document.idGenerationState as IdGenerationState.Enabled
                                        val currentlyChecked = document.idGenerationState.hashFields.contains(thisField)

                                        val newHashFields = if (currentlyChecked) {
                                            document.idGenerationState.hashFields.filter { it != thisField }
                                        } else {
                                            document.idGenerationState.hashFields.plus(thisField)
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

@Composable
fun DataKeyField(
    key: String?,
    allOptions: Map<String, List<String>>,
    loading: Boolean = false,
    onChange: (String) -> Unit
) {
    val showDropDown = remember { mutableStateOf(false) }

    if (key == null) {
        return Spinner(Modifier.preferredSize(25.dp))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {


        if (allOptions.size < 2) {

            Text("\"${key}\"")


        } else {
            DropdownMenu(
                toggle = {
                    Row(
                        modifier = Modifier.clickable(onClick = { showDropDown.value = true }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "\"${key}\"",
                        )

                        Icon(Icons.Filled.ArrowDropDown)
                    }
                },
                expanded = showDropDown.value,
                onDismissRequest = { showDropDown.value = false }
            ) {
                allOptions.forEach {
                    DropdownMenuItem(
                        onClick = {
                            onChange(it.key)
                        }
                    ) {
                        Text("\"${it.key}\"")
                    }
                }
            }
        }
        if (loading) {
            Spacer(Modifier.preferredWidth(5.dp))
            Spinner(Modifier.preferredSize(25.dp))
        }
    }
}