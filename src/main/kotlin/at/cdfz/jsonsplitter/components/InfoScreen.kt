package at.cdfz.jsonsplitter.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import at.cdfz.jsonsplitter.padding

@Composable
fun InfoScreen(onOkClicked: () -> Unit) = Column(Modifier.fillMaxSize()) {
    Box(Modifier.fillMaxWidth().weight(1.0F)) {
        val state = rememberScrollState(0f)
        Column(Modifier.verticalScroll(state).fillMaxSize().padding(padding)) {
            Text("JsonSplitter", fontWeight = FontWeight.Bold)

            Spacer(Modifier.preferredHeight(padding))

            Text(
                "Developed by:\n" +
                        "LVAk/ZentDok/CDFZ\n" +
                        "created in: 2021\n" +
                        "Rekr Kudler (2021)\n" +
                        "last update:\n" +
                        "09.02.2021, Kudler\n" +
                        "version: 1.2"  // also change in build.gradle.kts
            )

            Spacer(Modifier.preferredHeight(padding))

            Text(
                "Description:\n" +
                        "Splits big JSON files into smaller ones, which are easier to process. The source " +
                        "files are analyzed automatically, which enables to program to offer the best possible support " +
                        "to the user."
            )
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(state)
        )
    }

    Row(Modifier.fillMaxWidth().background(Color(0xffeeeeee)), horizontalArrangement = Arrangement.End) {
        Button({ onOkClicked() }, modifier = Modifier.padding(padding)) {
            Text("Ok")
        }
    }
}