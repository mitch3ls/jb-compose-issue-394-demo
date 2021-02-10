package at.cdfz.jsonsplitter.components

// taken from https://github.com/Gurupreet/ComposeCookBook/blob/0508b271dc28b778267ced4af6d6099fac92303c/app/src/main/java/com/guru/composecookbook/ui/animation/AnimationScreen.kt#L398


import androidx.compose.animation.core.*
import androidx.compose.animation.core.AnimationConstants.Infinite
import androidx.compose.animation.transition
import androidx.compose.foundation.Canvas
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

object AnimationDefinitions {
    // Each animation should be explained as a definition and using states.
    enum class AnimationState {
        START, END
    }

    //float animation
    val floatPropKey = FloatPropKey("value")
    fun floatAnimDefinition(
        start: Float,
        end: Float,
        repeat: Boolean,
        duration: Int = 2000
    ) = transitionDefinition<AnimationState> {
        state(AnimationState.START) { this[floatPropKey] = start }
        state(AnimationState.END) { this[floatPropKey] = end }

        transition(AnimationState.START, AnimationState.END) {
            floatPropKey using repeatable(
                iterations = if (repeat) Infinite else 1,
                animation = tween(
                    durationMillis = duration,
                    easing = LinearEasing
                )
            )
        }
    }
}

@Composable
fun Spinner(modifier: Modifier = Modifier, color: Color = MaterialTheme.colors.secondary) {
    val floatStateStart by remember { mutableStateOf(AnimationDefinitions.AnimationState.START) }
    val floadStateFinal by remember { mutableStateOf(AnimationDefinitions.AnimationState.END) }
    val floatAnim = transition(
        definition = AnimationDefinitions.floatAnimDefinition(0f, 360f, true),
        initState = floatStateStart,
        toState = floadStateFinal
    )
    val stroke = Stroke(8f)
    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val insideRadius = radius - stroke.width
        val topLeftOffset = Offset(
            5f,
            5f
        )
        val size = Size(insideRadius * 2, insideRadius * 2)
        var rotationAngle = floatAnim[AnimationDefinitions.floatPropKey] - 90f
        drawArc(
            color = color,
            startAngle = rotationAngle,
            sweepAngle = 150f,
            topLeft = topLeftOffset,
            size = size,
            useCenter = false,
            style = stroke,
        )
        rotationAngle += 40
    }
}