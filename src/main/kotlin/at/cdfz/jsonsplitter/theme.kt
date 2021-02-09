package at.cdfz.jsonsplitter

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.text.platform.font
import androidx.compose.ui.unit.sp

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

val SegoeUI = fontFamily(
    font("SegoeUI Regular", "font/Segoe UI.ttf", weight = FontWeight.Normal),
    font("SegoeUI Bold", "font/Segoe UI Bold.ttf", weight = FontWeight.Bold),
)

private val defaultTypography = Typography()
val ZentDokTypography = Typography(
    h1 = defaultTypography.h1.copy(fontFamily = SegoeUI, fontWeight = FontWeight.Bold, fontSize = 26.sp),
    subtitle1 = defaultTypography.subtitle1.copy(
        fontFamily = SegoeUI,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = Color(0xff808080)
    )
)

@Composable
fun ZentDokTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = LightColors,
        typography = ZentDokTypography,
        content = content
    )
}