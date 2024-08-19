package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview
import com.ramitsuri.podcasts.model.ui.SleepTimer
import kotlinx.coroutines.flow.drop
import java.util.Locale
import kotlin.time.Duration

@Composable
fun SleepTimerControl(
    sleepTimer: SleepTimer,
    sleepTimerDuration: Duration?,
    onEndOfEpisodeTimerSet: () -> Unit,
    onCustomTimerSet: (Int) -> Unit,
    onTimerCanceled: () -> Unit,
    onTimerIncrement: () -> Unit,
    onTimerDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.player_sleep_timer),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (sleepTimer) {
            is SleepTimer.None -> {
                SleepTimerNotSet(
                    onEndOfEpisodeTimerSet = onEndOfEpisodeTimerSet,
                    onCustomTimerSet = onCustomTimerSet,
                )
            }

            is SleepTimer.Custom -> {
                SleepTimerCustom(
                    sleepTimerDuration = sleepTimerDuration ?: Duration.ZERO,
                    onTimerCanceled = onTimerCanceled,
                    onTimerIncrement = onTimerIncrement,
                    onTimerDecrement = onTimerDecrement,
                )
            }

            is SleepTimer.EndOfEpisode -> {
                SleepTimerEndOfEpisode(
                    sleepTimerDuration = sleepTimerDuration ?: Duration.ZERO,
                    onTimerCanceled = onTimerCanceled,
                )
            }
        }
    }
}

@Composable
private fun SleepTimerEndOfEpisode(
    sleepTimerDuration: Duration,
    onTimerCanceled: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = sleepTimerDuration.formatted(), style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.player_sleep_timer_to_episode_completion),
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            icon = Icons.Outlined.Stop,
            label = stringResource(R.string.stop),
            onClick = onTimerCanceled,
        )
    }
}

@Composable
private fun SleepTimerCustom(
    sleepTimerDuration: Duration,
    onTimerCanceled: () -> Unit,
    onTimerIncrement: () -> Unit,
    onTimerDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.clickable { onTimerDecrement() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.DoNotDisturbOn,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.player_sleep_timer_five_min), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.width(40.dp))
            Text(text = sleepTimerDuration.formatted(), style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.width(40.dp))
            Column(
                modifier = Modifier.clickable { onTimerIncrement() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddCircle,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.player_sleep_timer_five_min), style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            icon = Icons.Outlined.Stop,
            label = stringResource(R.string.stop),
            onClick = onTimerCanceled,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SleepTimerNotSet(
    modifier: Modifier = Modifier,
    totalValues: Int = TOTAL_ITEMS,
    onEndOfEpisodeTimerSet: () -> Unit,
    onCustomTimerSet: (Int) -> Unit,
) {
    val initialSelectedTime = 30 // In minutes

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = fromTimeToIndex(initialSelectedTime))
    val density = LocalDensity.current
    val view = LocalView.current

    val timeUnit = stringResource(R.string.player_sleep_timer_minutes)
    var sleepTimerLabel by remember {
        mutableStateOf(
            indexToTimeForDisplay(
                index = fromTimeToIndex(initialSelectedTime),
                unit = timeUnit,
            ),
        )
    }

    LaunchedEffect(Unit) {
        listState.animateScrollToItem(fromTimeToIndex(initialSelectedTime))
    }

    val selectedIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(selectedIndex) {
        sleepTimerLabel = indexToTimeForDisplay(index = selectedIndex, unit = timeUnit)
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
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = sleepTimerLabel,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(8.dp))
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
                    TimerVerticalLineItem(
                        label = indexToTimeForDisplay(index),
                        showLabel = index % 5 == 0,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            label = stringResource(R.string.player_sleep_timer_episode_end),
            onClick = { onEndOfEpisodeTimerSet() },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            icon = Icons.Outlined.PlayArrow,
            label = stringResource(R.string.start),
            onClick = { onCustomTimerSet(fromIndexToTime(selectedIndex)) },
        )
    }
}

@Composable
private fun Button(
    icon: ImageVector? = null,
    label: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = label, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun TimerVerticalLineItem(
    label: String,
    showLabel: Boolean,
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
                    .background(MaterialTheme.colorScheme.onBackground, shape = CircleShape),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().alpha(if (showLabel) 1f else 0f),
            textAlign = TextAlign.Center,
        )
    }
}

private fun Duration.formatted(): String {
    val hours = inWholeHours % 24
    return if (hours == 0L) {
        String.format(
            Locale.getDefault(),
            "%02d:%02d",
            inWholeMinutes % 60,
            inWholeSeconds % 60,
        )
    } else {
        String.format(
            Locale.getDefault(),
            "%02d:%02d:%02d",
            hours,
            inWholeMinutes % 60,
            inWholeSeconds % 60,
        )
    }
}

private fun fromTimeToIndex(time: Int): Int {
    return (time / 5) - 1
}

private fun fromIndexToTime(index: Int): Int {
    return (index + 1) * 5
}

private fun indexToTimeForDisplay(
    index: Int,
    unit: String? = null,
): String {
    val time = fromIndexToTime(index)
    return timeToTimeForDisplay(time, unit)
}

private fun timeToTimeForDisplay(
    time: Int,
    unit: String?,
): String {
    return buildString {
        append(time)
        if (unit != null) {
            append(" ")
            append(unit)
        }
    }
}

private const val TOTAL_ITEMS = 18

@ThemePreview
@Composable
private fun SleepControlPreview() {
    PreviewTheme {
        Column {
            SleepTimerControl(
                sleepTimer = SleepTimer.None,
                sleepTimerDuration = null,
                onEndOfEpisodeTimerSet = {},
                onCustomTimerSet = {},
                onTimerCanceled = {},
                onTimerIncrement = {},
                onTimerDecrement = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
