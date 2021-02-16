package at.cdfz.jsonsplitter.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import at.cdfz.jsonsplitter.models.FilterItem
import at.cdfz.jsonsplitter.models.FilterList
import at.cdfz.jsonsplitter.padding

@Composable
fun FilterScreen(
    filters: FilterList,
    possibleFields: Array<String>,
    onListChanged: (FilterList) -> Unit,
    onCanceled: () -> Unit
) =
    Column(Modifier.fillMaxSize()) {
        val workingFilters = remember { mutableStateListOf(*filters.items) }

        Box(Modifier.fillMaxWidth().weight(1.0F)) {
            val state = rememberScrollState(0f)
            Column(Modifier.verticalScroll(state).fillMaxSize()) {
                workingFilters.forEachIndexed { index, filterItem ->
                    Row(Modifier.fillMaxWidth().padding(padding), verticalAlignment = Alignment.CenterVertically) {
                        FieldSelector(
                            selectedField = filterItem.fieldName,
                            allOptions = possibleFields,
                            onChange = {
                                workingFilters[index] = filterItem.copy(fieldName = it)
                            },
                        )

                        Spacer(Modifier.preferredWidth(padding))

                        TextField(
                            modifier = Modifier.weight(1f),
                            value = filterItem.valueToBeContained,
                            onValueChange = {
                                workingFilters[index] = filterItem.copy(valueToBeContained = it)
                            },
                            label = { Text("must contain") }
                        )

                        IconButton({
                            workingFilters.removeRange(index, index + 1)
                        }) {
                            Icon(Icons.Filled.Delete)
                        }
                    }
                    Divider()
                }
            }
            Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
                Divider()
                IconButton({ workingFilters.add(FilterItem("", "")) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, modifier = Modifier.offset(y = -2.dp))
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(state)
            )
        }

        Row(Modifier.fillMaxWidth().background(Color(0xffeeeeee)), horizontalArrangement = Arrangement.End) {
            Button({
                val newFilterList = FilterList(workingFilters.toTypedArray())
                onListChanged(newFilterList)
            }, modifier = Modifier.padding(padding),
                enabled = workingFilters.all { it.fieldName.isNotEmpty() && it.valueToBeContained.isNotEmpty() }) {
                Text("Ok")
            }
            Spacer(Modifier.preferredWidth(padding))
            Button({ onCanceled() }, modifier = Modifier.padding(padding)) {
                Text("Cancel")
            }
        }
    }

@Composable
private fun FieldSelector(
    modifier: Modifier = Modifier,
    selectedField: String,
    allOptions: Array<String>,
    onChange: (String) -> Unit
) {
    val showDropDown = remember { mutableStateOf(false) }

    DropdownMenu(
        toggleModifier = modifier,
        toggle = {
            Row(
                modifier = Modifier.clickable(onClick = { showDropDown.value = true }),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (selectedField.isEmpty()) "Select field" else "\"${selectedField}\"",
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
                    onChange(it)
                }
            ) {
                Text("\"${it}\"")
            }
        }
    }
}