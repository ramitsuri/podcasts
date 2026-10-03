package com.ramitsuri.podcasts.widget.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Describes what should be shown in the "currently playing" widget for a given size.
 *
 * Priority of content, from most to least important:
 * 1. Play/pause button (always shown)
 * 2. Album art
 * 3. Title (only when there is enough width for a few words AND enough height for a line)
 * 4. Replay / skip buttons
 */
internal data class WidgetLayout(
    val arrangement: Arrangement,
    val horizontalPadding: Dp,
    val playSize: Dp,
    // null when album art should be hidden
    val artSize: Dp?,
    // null when replay / skip buttons should be hidden
    val secondarySize: Dp?,
    // 0 when title should be hidden
    val titleLines: Int,
) {
    enum class Arrangement {
        // Optional title on top, then a single row of [art] [replay] [play] [skip]
        SingleRow,

        // Art + title side by side on top, controls row below
        ArtBesideTitle,

        // Only for 1 column wide widgets: everything stacked vertically
        // [art] [title] [replay] [play] [skip]
        SingleColumn,

        // Not wide enough for everything in one row but tall enough for two rows:
        // optional title on top, then [art] [play], then [replay] [skip]
        TwoRows,
    }

    // Higher score = more of the higher priority content is visible
    internal val score: Int
        get() {
            var score = 0
            if (artSize != null) score += 4
            if (titleLines > 0) score += 2
            if (secondarySize != null) score += 1
            return score
        }

    companion object {
        val VERTICAL_PADDING = 8.dp
        val GAP = 8.dp
        val TITLE_GAP = 4.dp

        private val HORIZONTAL_PADDING = 12.dp
        private val HORIZONTAL_PADDING_NARROW = 6.dp
        // Anything narrower than this is treated as a 1 column (1 cell wide) widget
        private val NARROW_WIDTH = 100.dp

        private val MAX_PLAY = 56.dp
        private val MIN_PLAY_WITH_TITLE = 40.dp
        private val SECONDARY_IN_TALL = 48.dp
        private val MIN_ART_TALL = 64.dp
        private val MAX_ART_TALL = 96.dp
        private const val SECONDARY_RATIO = 0.85f

        // Roughly enough room for a couple of words at 14sp
        private val MIN_TITLE_WIDTH = 72.dp

        // Approximate line height for 14sp text at font scale 1
        private val TITLE_LINE_HEIGHT = 20.dp

        /**
         * Sizes used with SizeMode.Responsive. On Android 12+ the launcher picks the closest size
         * that fits inside the widget (or the smallest one if none fit), so:
         * - The smallest entry must match minResizeWidth/minResizeHeight in widget_info.xml,
         *   otherwise the widget falls back to a layout bigger than itself and gets clipped.
         * - Every 1 column size (w < NARROW_WIDTH, h) has a matching (NARROW_WIDTH, h) entry.
         *   It is always closer for widgets 2+ columns wide, so those never stack vertically.
         *
         * Each entry sits at a breakpoint in [compute] where more content becomes visible.
         * WidgetLayoutTest checks what real launcher cell sizes end up showing; update those
         * tests along with this list. Keep at or under 16 entries (platform limit).
         */
        val RESPONSIVE_SIZES: Set<DpSize> =
            setOf(
                // Minimum resize size: play only
                DpSize(50.dp, 50.dp),
                // 1 column: stacked vertically
                DpSize(50.dp, 115.dp), // + art
                DpSize(50.dp, 185.dp), // + replay/skip
                DpSize(70.dp, 140.dp), // full size play + art
                // Counterparts of the 1 column sizes, so 2+ columns never stack vertically
                DpSize(100.dp, 115.dp),
                DpSize(100.dp, 140.dp),
                DpSize(100.dp, 185.dp),
                // 1 row tall: play + art, then + replay/skip
                DpSize(90.dp, 50.dp),
                DpSize(180.dp, 50.dp),
                // Title above a single row
                DpSize(120.dp, 80.dp), // art + play
                DpSize(200.dp, 80.dp), // + replay/skip
                DpSize(150.dp, 90.dp), // bigger art + play
                DpSize(240.dp, 90.dp), // + replay/skip
                // 2 columns, taller: two rows
                DpSize(140.dp, 180.dp),
                // 3+ columns, taller: art beside title
                DpSize(200.dp, 160.dp),
                DpSize(220.dp, 190.dp), // max art
            )

        fun compute(
            width: Dp,
            height: Dp,
            fontScale: Float = 1f,
        ): WidgetLayout {
            val horizontalPadding = if (width < NARROW_WIDTH) HORIZONTAL_PADDING_NARROW else HORIZONTAL_PADDING
            val w = (width - horizontalPadding * 2).coerceAtLeast(0.dp)
            val h = (height - VERTICAL_PADDING * 2).coerceAtLeast(0.dp)
            val lineHeight = TITLE_LINE_HEIGHT * fontScale.coerceAtLeast(1f)

            val layout =
                computeTall(w, h, lineHeight)
                    ?: run {
                        val row = computeSingleRow(w, h, lineHeight)
                        // Alternatives are only used when they show more than the row
                        val alternative =
                            if (width >= NARROW_WIDTH) {
                                computeTwoRows(w, h, lineHeight)
                            } else {
                                // 1 column wide: stack vertically
                                computeSingleColumn(w, h, lineHeight)
                            }
                        if (alternative != null && alternative.score > row.score) alternative else row
                    }
            return layout.copy(horizontalPadding = horizontalPadding)
        }

        /**
         * Big layout: art next to a multi-line title, with full controls row underneath.
         */
        private fun computeTall(
            w: Dp,
            h: Dp,
            lineHeight: Dp,
        ): WidgetLayout? {
            val controlsWidth = SECONDARY_IN_TALL * 2 + MAX_PLAY + GAP * 2
            if (w < controlsWidth) return null

            val artByHeight = h - MAX_PLAY - GAP * 2
            val artByWidth = w - GAP - MIN_TITLE_WIDTH
            val art = minOf(MAX_ART_TALL, artByHeight, artByWidth)
            if (art < MIN_ART_TALL) return null

            return WidgetLayout(
                arrangement = Arrangement.ArtBesideTitle,
                horizontalPadding = HORIZONTAL_PADDING,
                playSize = MAX_PLAY,
                artSize = art,
                secondarySize = SECONDARY_IN_TALL,
                titleLines = (art / lineHeight).toInt().coerceIn(1, 4),
            )
        }

        private fun computeSingleRow(
            w: Dp,
            h: Dp,
            lineHeight: Dp,
        ): WidgetLayout {
            // Title only if there is width for some words and the play button can still be a
            // reasonable size with a line of text above it.
            val playIfTitle = minOf(MAX_PLAY, w, h - TITLE_GAP - lineHeight)
            val showTitle = w >= MIN_TITLE_WIDTH && playIfTitle >= MIN_PLAY_WITH_TITLE

            val play =
                if (showTitle) {
                    playIfTitle
                } else {
                    minOf(MAX_PLAY, w, h)
                }.coerceAtLeast(0.dp)

            val titleLines =
                if (showTitle) {
                    ((h - play - TITLE_GAP) / lineHeight).toInt().coerceIn(1, 2)
                } else {
                    0
                }

            var usedWidth = play
            val art =
                if (w >= usedWidth + GAP + play) {
                    usedWidth += GAP + play
                    play
                } else {
                    null
                }

            val secondary = play * SECONDARY_RATIO
            val showSecondary = w >= usedWidth + (GAP + secondary) * 2

            return WidgetLayout(
                arrangement = Arrangement.SingleRow,
                horizontalPadding = HORIZONTAL_PADDING,
                playSize = play,
                artSize = art,
                secondarySize = if (showSecondary) secondary else null,
                titleLines = titleLines,
            )
        }

        /**
         * Title on top, then [art] [play], then [replay] [skip]. Null if two rows don't fit.
         */
        private fun computeTwoRows(
            w: Dp,
            h: Dp,
            lineHeight: Dp,
        ): WidgetLayout? {
            val play = minOf(MAX_PLAY, (w - GAP) / 2)
            if (play <= 0.dp) return null
            val secondary = play * SECONDARY_RATIO
            val rowsHeight = play + GAP + secondary
            if (h < rowsHeight) return null

            val titleLines =
                if (w >= MIN_TITLE_WIDTH) {
                    ((h - rowsHeight - TITLE_GAP) / lineHeight).toInt().coerceIn(0, 2)
                } else {
                    0
                }

            return WidgetLayout(
                arrangement = Arrangement.TwoRows,
                horizontalPadding = HORIZONTAL_PADDING,
                playSize = play,
                artSize = play,
                secondarySize = secondary,
                titleLines = titleLines,
            )
        }

        /**
         * Narrow and tall layout where everything is stacked: [art] [title] [replay] [play] [skip]
         */
        private fun computeSingleColumn(
            w: Dp,
            h: Dp,
            lineHeight: Dp,
        ): WidgetLayout {
            val play = minOf(MAX_PLAY, w, h).coerceAtLeast(0.dp)
            var usedHeight = play

            val art =
                if (h >= usedHeight + GAP + play) {
                    usedHeight += GAP + play
                    play
                } else {
                    null
                }

            val titleLines =
                if (w >= MIN_TITLE_WIDTH) {
                    ((h - usedHeight - TITLE_GAP) / lineHeight).toInt().coerceIn(0, 2)
                } else {
                    0
                }
            if (titleLines > 0) {
                usedHeight += TITLE_GAP + lineHeight * titleLines
            }

            val secondary = play * SECONDARY_RATIO
            val showSecondary = h >= usedHeight + (GAP + secondary) * 2

            return WidgetLayout(
                arrangement = Arrangement.SingleColumn,
                horizontalPadding = HORIZONTAL_PADDING,
                playSize = play,
                artSize = art,
                secondarySize = if (showSecondary) secondary else null,
                titleLines = titleLines,
            )
        }
    }
}
