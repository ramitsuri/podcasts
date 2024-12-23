package com.ramitsuri.podcasts.android.ui.review

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.R
import com.ramitsuri.podcasts.android.ui.components.Image
import com.ramitsuri.podcasts.android.utils.dateFormatted
import com.ramitsuri.podcasts.android.utils.dayOfWeekFormatted
import com.ramitsuri.podcasts.android.utils.monthFormatted
import com.ramitsuri.podcasts.model.ui.YearEndReviewViewState
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearEndReviewScreen(
    state: YearEndReviewViewState,
    onBack: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showError by remember { mutableStateOf(false) }
    var backgroundColors by remember {
        mutableStateOf(
            listOf(
                Color.Transparent,
                Color.Transparent,
            ),
        )
    }
    Column(
        modifier =
        modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = backgroundColors,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                ),
            )
            .displayCutoutPadding()
            .statusBarsPadding(),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (state) {
                is YearEndReviewViewState.Data -> {
                    LaunchedEffect(state.currentPage) {
                        val colorSets = listOf(greens, blues, reds)
                        backgroundColors = listOf(
                            colorSets.random().random(),
                            colorSets.random().random(),
                            colorSets.random().random(),
                        )
                    }
                    YearEndReviewContent(
                        data = state,
                        onNext = onNextPage,
                        onPrevious = onPreviousPage,
                        onExit = onBack,
                    )
                }

                is YearEndReviewViewState.Error -> {
                    LaunchedEffect(Unit) {
                        showError = true
                    }
                }

                is YearEndReviewViewState.Loading -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
    if (showError) {
        BasicAlertDialog(
            onDismissRequest = {
                showError = false
            },
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(text = stringResource(R.string.generic_error))
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(onClick = onBack) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        }
    }
}

@Composable
private fun YearEndReviewContent(
    data: YearEndReviewViewState.Data,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExit: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
        Modifier
            .pointerInput(Unit) {
                val maxWidth = this.size.width
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val totalPressTime = measureTime {
                            this.tryAwaitRelease()
                        }
                        if (totalPressTime < 200.milliseconds) {
                            val isTapOnRight = (it.x > (maxWidth / 2))
                            if (isTapOnRight) {
                                onNext()
                            } else {
                                onPrevious()
                            }
                        }
                        isPressed = false
                    },
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            IconButton(
                onClick = onExit,
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(id = R.string.back),
                    modifier = Modifier
                        .size(24.dp),
                    tint = Color.White,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ProgressBar(
            steps = data.totalPages,
            currentStep = data.currentPage,
            paused = isPressed,
            onCurrentStepAnimationDone = onNext,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val modifier = Modifier.weight(1f)
            when (val pageInfo = data.getPageInfo()) {
                is YearEndReviewViewState.Data.PageInfo.ListenedDuration -> {
                    ListenedDurationContent(pageInfo, modifier)
                }

                is YearEndReviewViewState.Data.PageInfo.ConsumedDuration -> {
                    ConsumedDurationContent(pageInfo, modifier)
                }

                is YearEndReviewViewState.Data.PageInfo.ListeningSince -> {
                    ListeningSinceContent(pageInfo, modifier)
                }

                is YearEndReviewViewState.Data.PageInfo.MostListenedMonth -> {
                    MostListenedMonthContent(pageInfo, modifier)
                }

                is YearEndReviewViewState.Data.PageInfo.MostListenedDay -> {
                    MostListenedDayContent(pageInfo, modifier)
                }

                is YearEndReviewViewState.Data.PageInfo.MostListenedDayOfWeek -> {
                    MostListenedDayOfWeekContent(pageInfo, modifier)
                }

                is YearEndReviewViewState.Data.PageInfo.MostListenedToPodcasts -> {
                    MostListenedToPodcastsContent(pageInfo, modifier)
                }

                is YearEndReviewViewState.Data.PageInfo.TotalEpisodesListened -> {
                    TotalEpisodesListenedContent(pageInfo, modifier)
                }

                is YearEndReviewViewState.Data.PageInfo.Bye -> {
                    ByeContent(modifier)
                }
            }
        }
    }
}

@Composable
private fun ByeContent(modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            text = "🥂",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.year_end_bye),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TotalEpisodesListenedContent(
    info: YearEndReviewViewState.Data.PageInfo.TotalEpisodesListened,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.year_end_total_episodes_start),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.year_end_total_episodes_format, info.episodes),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun MostListenedToPodcastsContent(
    info: YearEndReviewViewState.Data.PageInfo.MostListenedToPodcasts,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.year_end_most_listened_to_podcasts),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            info.podcasts.forEach { podcast ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        url = podcast.artwork,
                        contentDescription = podcast.title,
                        modifier =
                        Modifier
                            .size(88.dp)
                            .padding(4.dp)
                            .clip(MaterialTheme.shapes.small),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = podcast.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun MostListenedDayOfWeekContent(
    info: YearEndReviewViewState.Data.PageInfo.MostListenedDayOfWeek,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            text = dayOfWeekFormatted(info.dayOfWeek),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.year_end_most_listened_day_of_week),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MostListenedDayContent(
    info: YearEndReviewViewState.Data.PageInfo.MostListenedDay,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        val text = buildAnnotatedString {
            withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                append(stringResource(R.string.year_end_most_listened_date_start))
            }
            withStyle(MaterialTheme.typography.headlineLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                append(dateFormatted(toFormat = info.day, useShortMonthNames = false))
            }
            withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                append(stringResource(R.string.year_end_most_listened_date_end))
            }
        }
        Text(
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MostListenedMonthContent(
    info: YearEndReviewViewState.Data.PageInfo.MostListenedMonth,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.year_end_most_listened_month),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = monthFormatted(month = info.month, useShortMonthNames = false),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun ConsumedDurationContent(
    info: YearEndReviewViewState.Data.PageInfo.ConsumedDuration,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        val text = buildAnnotatedString {
            withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                append(stringResource(R.string.year_end_duration_consumed_start))
            }
            withStyle(MaterialTheme.typography.headlineLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.year_end_duration_consumed_speed, info.speedRounded.toString()))
            }
            withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                append(stringResource(R.string.year_end_duration_consumed_middle))
            }
            withStyle(MaterialTheme.typography.headlineLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.year_end_minutes_format, info.duration.inWholeMinutes))
            }
            withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                append(stringResource(R.string.year_end_duration_consumed_end))
            }
        }
        Text(
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ListenedDurationContent(
    info: YearEndReviewViewState.Data.PageInfo.ListenedDuration,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        val text = buildAnnotatedString {
            withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                append(stringResource(R.string.year_end_duration_listened_start))
            }
            withStyle(MaterialTheme.typography.headlineLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                append(stringResource(R.string.year_end_minutes_format, info.duration.inWholeMinutes))
            }
            withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                append(stringResource(R.string.year_end_duration_listened_end))
            }
        }
        Text(
            text = text,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ListeningSinceContent(
    info: YearEndReviewViewState.Data.PageInfo.ListeningSince,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.year_end_listening_since),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = dateFormatted(toFormat = info.time.date, useShortMonthNames = false),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}


@Suppress("SameParameterValue")
@Composable
private fun ProgressBar(
    steps: Int,
    currentStep: Int,
    paused: Boolean,
    onCurrentStepAnimationDone: () -> Unit
) {
    val currentStepValue = remember(currentStep) { Animatable(0f) }
    LaunchedEffect(currentStep, paused) {
        if (paused) {
            currentStepValue.stop()
        } else {
            currentStepValue.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = (3000 * (1f - currentStepValue.value)).toInt(),
                    easing = LinearEasing,
                ),
            )
            onCurrentStepAnimationDone()
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(48.dp)
            .padding(horizontal = 24.dp),
    ) {
        for (index in 1..steps) {
            Row(
                modifier = Modifier
                    .height(4.dp)
                    .clip(RoundedCornerShape(50, 50, 50, 50))
                    .weight(1f)
                    .background(Color.White.copy(alpha = 0.4f)),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxHeight().let {
                            when (index) {
                                currentStep -> it.fillMaxWidth(currentStepValue.value)
                                in 0..currentStep -> it.fillMaxWidth(1f)
                                else -> it
                            }
                        },
                ) {}
            }
            if (index != steps) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

private val blues = listOf(
    Color(0xFF0000FF),
    Color(0xFF0000CC),
    Color(0xFF0000AA),
    Color(0xFF000088),
    Color(0xFF000066),
)
private val reds = listOf(
    Color(0xFFEE4444),
    Color(0xFFCC0000),
    Color(0xFFAA0000),
    Color(0xFF880000),
    Color(0xFF660000),
)
private val greens = listOf(
    Color(0xFF00CC00),
    Color(0xFF00AA00),
    Color(0xFF008800),
    Color(0xFF006600),
    Color(0xFF004400),
)
