package dev.scottpierce.animation.compose.reveal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val BackgroundShape = ShapeDefaults.Medium
private val CheckBoxShape = RoundedCornerShape(4.dp)
private val Height = 52.dp
private val HorizontalPadding = 12.dp

val lightGrey = Color(0x1B000000)

@Composable
fun CircularRevealCheckItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (checked: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFingerDown: Boolean by remember { mutableStateOf(false) }

    CircularRevealAnimation(
        revealPercentTarget = if (isFingerDown) {
            0.12f
        } else {
            if (checked) 1f else 0f
        },
        startContent = {
            CheckItemContent(
                text = text,
                checked = false,
            )
        },
        endContent = {
            CheckItemContent(
                text = text,
                checked = true,
            )
        },
        modifier = modifier,
        onPointerEvent = { event, fingerDown ->
            when (event.type) {
                PointerEventType.Release -> {
                    if (isFingerDown) {
                        onCheckedChange(!checked)
                    }
                }
            }
            isFingerDown = fingerDown
        }
    )
}

@Composable
private fun CheckItemContent(
    text: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val typography = MaterialTheme.typography
    val backgroundColor: Color = if (checked) Color.Black else Color.White
    val textColor: Color = if (checked) Color.White else Color.Black
    val fontWeight: FontWeight = if (checked) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = modifier
            .height(Height)
            .fillMaxWidth()
            .border(2.dp, lightGrey, BackgroundShape)
            .background(backgroundColor, BackgroundShape)
            .clip(BackgroundShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .padding(start = HorizontalPadding),
            style = typography.bodyLarge.copy(
                color = textColor,
                fontWeight = fontWeight
            ),
        )

        Box(
            modifier = Modifier
                .padding(end = HorizontalPadding)
                .size(24.dp)
                .let {
                    if (checked) {
                        it
                    } else {
                        it.border(2.dp, lightGrey, CheckBoxShape)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checkmark",
                    tint = textColor,
                )
            }
        }
    }
}