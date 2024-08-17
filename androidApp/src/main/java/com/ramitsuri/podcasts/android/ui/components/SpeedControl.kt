package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * @param selectedValue The selected playback speed times ten so that it can be represented as integer.
 */
@Composable
fun SpeedControl(
    selectedValue: Int,
    onSpeedSet: (Int) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = fromIntSpeedToIndex(selectedValue))
    var speedLabel by remember {
        mutableStateOf(
            indexToSpeedForDisplay(
                fromIntSpeedToIndex(selectedValue),
                showX = true,
            ),
        )
    }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.player_playback_speed),
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = speedLabel,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            tint = color,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SpeedSlider(
            listState = listState,
            color = color,
            onIndexSet = {
                speedLabel = indexToSpeedForDisplay(it, showX = true)
                onSpeedSet(fromIndexToIntSpeed(it))
            },
        )
        Spacer(modifier = Modifier.height(32.dp))

        fun setSpeed(speed: Int) {
            coroutineScope.launch {
                listState.animateScrollToItem(fromIntSpeedToIndex(speed))
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
        ) {
            PresetButton(
                intSpeed = 8,
                onClick = { setSpeed(it) },
            )
            Spacer(modifier = Modifier.width(16.dp))
            PresetButton(
                intSpeed = 10,
                onClick = { setSpeed(it) },
            )
            Spacer(modifier = Modifier.width(16.dp))
            PresetButton(
                intSpeed = 12,
                onClick = { setSpeed(it) },
            )
            Spacer(modifier = Modifier.width(16.dp))
            PresetButton(
                intSpeed = 15,
                onClick = { setSpeed(it) },
            )
            Spacer(modifier = Modifier.width(16.dp))
            PresetButton(
                intSpeed = 20,
                onClick = { setSpeed(it) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpeedSlider(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    totalValues: Int = TOTAL_SPEED_ITEMS,
    color: Color,
    onIndexSet: (Int) -> Unit,
) {
    val density = LocalDensity.current
    val view = LocalView.current

    val selectedIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(selectedIndex) {
        onIndexSet(selectedIndex)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .drop(1)
            .collect { index ->
                when (index) {
                    0, listState.layoutInfo.totalItemsCount - 1 -> {
                        view.performHapticFeedback(
                            HapticFeedbackConstantsCompat.LONG_PRESS,
                        )
                    }

                    else -> {
                        view.performHapticFeedback(
                            HapticFeedbackConstantsCompat.CLOCK_TICK,
                        )
                    }
                }
            }
    }

    var padding by remember { mutableStateOf(0.dp) }
    var height by remember { mutableStateOf(0.dp) }
    Column(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .onSizeChanged {
                        padding = with(density) { (it.width / 2).toDp() }
                        height = with(density) { it.height.toDp() }
                    }
                    .fillMaxWidth(),
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(horizontal = (padding - 13.dp).coerceAtLeast(0.dp)),
                verticalAlignment = Alignment.CenterVertically,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            ) {
                items(totalValues) { index ->
                    SpeedVerticalLineItem(
                        label = indexToSpeedForDisplay(index),
                        showLabel = index % 5 == 0,
                        color = color,
                    )
                }
            }
            EdgeTransparency(modifier = Modifier.height(height))
        }
    }
}

@Composable
private fun PresetButton(
    intSpeed: Int,
    onClick: (Int) -> Unit,
) {
    OutlinedButton(
        onClick = { onClick(intSpeed) },
        modifier =
            Modifier
                .size(48.dp)
                .clip(CircleShape),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(
            text = intSpeedToSpeedForDisplay(speed = intSpeed, showX = false),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun fromIntSpeedToIndex(speed: Int): Int {
    return speed - START_ITEM
}

private fun fromIndexToIntSpeed(index: Int): Int {
    return START_ITEM + index
}

private fun indexToSpeedForDisplay(
    index: Int,
    showX: Boolean = false,
): String {
    val speed = fromIndexToIntSpeed(index)
    return intSpeedToSpeedForDisplay(speed, showX)
}

private fun intSpeedToSpeedForDisplay(
    speed: Int,
    showX: Boolean,
): String {
    return buildString {
        append("${speed / 10}.${speed % 10}")
        if (showX) {
            append("x")
        }
    }
}

@Composable
private fun SpeedVerticalLineItem(
    label: String,
    showLabel: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(horizontal = 12.dp)
                    .height(40.dp)
                    .width(2.dp)
                    .background(color, shape = CircleShape),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.fillMaxWidth().alpha(if (showLabel) 1f else 0f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EdgeTransparency(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                ),
    )
}

private const val TOTAL_SPEED_ITEMS = 26
private const val START_ITEM = 5

@ThemePreview
@Composable
private fun SpeedControlPreview() {
    PreviewTheme {
        Column {
            SpeedControl(
                selectedValue = 10,
                onSpeedSet = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
