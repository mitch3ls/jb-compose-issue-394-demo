package at.cdfz.jsonsplitter

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZentDokGreen = Color(0xff485643)
private val ZentDokGreenVariant = Color(0xff202d1c)
private val OebhRed = Color(0xff941921)
private val OebhRedVariant = Color(0xff600000)


private val LightColors = lightColors(
    primary = ZentDokGreen,
    primaryVariant = ZentDokGreenVariant,
    secondary = OebhRed,
    secondaryVariant = OebhRedVariant,
    background = Color.White,
    surface = Color.White,
    error = OebhRedVariant,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

@Composable
fun ZentDokTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = LightColors,
        content = content
    )
}