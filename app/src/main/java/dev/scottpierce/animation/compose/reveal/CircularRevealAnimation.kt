package dev.scottpierce.animation.compose.reveal

import android.util.Log
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.max

private const val TAG = "CircularRevealAnimation"

/**
 * Animates between the [startContent] and the [endContent] via a CircularReveal that radiates
 * outward from the location of the fingers touch location.
 *
 * @param revealPercentTarget the target percentage of the reveal to animate to.
 * @param startContent the starting content of the animation
 * @param endContent the ending content of the animation
 * @param onPointerEvent
 * @param animationSpec
 * @param topContentIsTransparent
 */
@Composable
fun CircularRevealAnimation(
    revealPercentTarget: Float,
    startContent: @Composable @UiComposable () -> Unit,
    endContent: @Composable @UiComposable () -> Unit,
    modifier: Modifier = Modifier,
    onPointerEvent: ((event: PointerEvent, isFingerDown: Boolean) -> Unit)? = null,
    animationSpec: AnimationSpec<Float> = spring(),
    topContentIsTransparent: Boolean = false,
) {
    // Tracks if the finger is up or down in real time
    var isFingerDown: Boolean by remember { mutableStateOf(false) }
    // Tracks the last position of the finger for the duration of the animation
    val fingerOffsetState: MutableState<Offset?> = remember { mutableStateOf(null) }
    // The percent of the top layer to clip
    val endContentClipPercent by animateFloatAsState(
        targetValue = revealPercentTarget,
        label = "Circular Reveal Clip Percent",
        animationSpec = animationSpec,
        finishedListener = {
            if (!isFingerDown) {
                fingerOffsetState.value = null
            }
        }
    )

    Box(
        modifier.pointerInput(onPointerEvent) {
            awaitPointerEventScope {
                while (true) {
                    val event: PointerEvent = awaitPointerEvent()

                    when (event.type) {
                        PointerEventType.Press -> {
                            isFingerDown = true
                            val offset = event.changes.last().position
                            fingerOffsetState.value = offset
                        }
                        PointerEventType.Release -> {
                            if (isFingerDown) {
                                isFingerDown = false
                            }
                        }
                        PointerEventType.Move -> {
                            if (isFingerDown) {
                                val offset = event.changes.last().position
                                if (
                                    offset.x < 0 ||
                                    offset.y < 0 ||
                                    offset.x > size.width ||
                                    offset.y > size.height
                                ) {
                                    isFingerDown = false
                                } else {
                                    fingerOffsetState.value = offset
                                }
                            }
                        }
                        else -> Log.v(TAG, "Unexpected Event type ${event.type}")
                    }

                    onPointerEvent?.invoke(event, isFingerDown)
                }
            }

            // Explicitly don't return the awaitPointerEventScope to avoid lint warning
            @Suppress("RedundantUnitExpression", "RedundantSuppression")
            Unit
        },
    ) {
        // Draw the bottom layer if the top layer is transparent, or the top isn't fully animated in.
        if (endContentClipPercent < 1f || topContentIsTransparent) {
            startContent()
        }

        val fingerOffset: Offset? = fingerOffsetState.value
        // Draw the top layer if it's not being fully clipped by the mask
        if (endContentClipPercent > 0f) {
            val path: Path = remember { Path() }

            val clipModifier: Modifier = if (endContentClipPercent < 1f && fingerOffset != null) {
                Modifier.drawWithContent {
                    path.rewind()

                    val largestDimension = max(size.width, size.height)

                    path.addOval(
                        Rect(
                            center = fingerOffset,
                            radius = endContentClipPercent * largestDimension
                        )
                    )

                    clipPath(path) {
                        this@drawWithContent.drawContent()
                    }
                }
            } else {
                Modifier
            }

            Box(
                modifier = clipModifier
            ) {
                endContent()
            }
        }
    }
}
