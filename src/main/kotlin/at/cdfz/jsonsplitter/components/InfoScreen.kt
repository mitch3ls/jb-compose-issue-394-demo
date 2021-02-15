package at.cdfz.jsonsplitter.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import at.cdfz.jsonsplitter.padding

@Composable
fun InfoScreen(onOkClicked: () -> Unit) = Column(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxWidth().weight(1.0F).padding(padding)) {
        Text("JsonSplitter", fontWeight = FontWeight.Bold)

        Spacer(Modifier.preferredHeight(padding))

        Text(
            "Developed by:\n" +
                    "LVAk/ZentDok/CDFZ\n" +
                    "created in: 2021\n" +
                    "Rekr Kudler (2021)\n" +
                    "last update:\n" +
                    "09.02.2021, Kudler\n"
        )

        Spacer(Modifier.preferredHeight(padding))

        Text(
            "Description:\n" +
                    "Zerteilt große JSON Dateien in kleinere leichter zu verarbeitete Dateien. Dabei " +
                    "analysiert es die Quelldatei automatisch und bietet so größmögliche Unterstützung " +
                    "bei der Bedienung."
        )
    }

    Row(Modifier.fillMaxWidth().background(Color(0xffeeeeee)), horizontalArrangement = Arrangement.End) {
        Button({ onOkClicked() }, modifier = Modifier.padding(padding)) {
            Text("Ok")
        }
    }
}