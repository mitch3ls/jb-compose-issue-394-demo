package at.cdfz.jsonsplitter.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import at.cdfz.jsonsplitter.padding

@Composable
fun ViewBase(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column {
        Row(Modifier.weight(1.0F)) {
            Column(Modifier.weight(1.0F)) {
                Column(
                    modifier = Modifier.preferredHeight(90.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "JsonSplitter",
                        style = MaterialTheme.typography.h1,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                }

                content()
            }

            Column(
                Modifier.preferredWidth(100.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = imageResource("images/owl.png"),
                    modifier = Modifier.fillMaxWidth().padding(padding).offset(x = 2.dp)
                )

                Button(onClick = {}) {
                    Text("Info")
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                "Developed by: Kudler (2021)",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(padding)
            )
        }
    }
}