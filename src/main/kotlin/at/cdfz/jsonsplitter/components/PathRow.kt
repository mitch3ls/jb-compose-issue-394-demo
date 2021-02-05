package at.cdfz.jsonsplitter.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout

@Composable
fun PathRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        val lastMeasurable = measurables.last()
        val lastPlaceable = lastMeasurable.measure(constraints)

        val remainingWidth = constraints.maxWidth - lastPlaceable.width
        val newConstraints = constraints.copy(maxWidth = remainingWidth)

        val otherPlaceables = measurables.dropLast(1).map { measurable ->
            measurable.measure(newConstraints)
        }

        val placeables = otherPlaceables.plus(lastPlaceable)

        val preferredWidth = placeables.map { it.width }.sum()
        val calculatedWidth = Math.min(preferredWidth, constraints.maxWidth)

        val preferredHeight = placeables.map { it.height }.maxOrNull() ?: 0
        val calculatedHeight = Math.min(preferredHeight, constraints.maxHeight)

        // Set the size of the layout as big as it can
        layout(calculatedWidth, calculatedHeight) {
            // Track the y co-ord we have placed children up to
            var xPosition = 0

            // Place children in the parent layout
            placeables.forEach { placeable ->

                // Position item on the screen
                placeable.placeRelative(x = xPosition, y = 0)

                // Record the y co-ord placed up to
                xPosition += placeable.width
            }
        }
    }
}
