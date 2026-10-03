package com.ramitsuri.podcasts.widget.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WidgetLayoutTest {
    @Test
    fun `tiny widget shows only play button`() {
        val layout = WidgetLayout.compute(width = 70.dp, height = 50.dp)
        assertTrue(layout.playSize > 0.dp)
        assertNull(layout.artSize)
        assertNull(layout.secondarySize)
        assertEquals(0, layout.titleLines)
    }

    @Test
    fun `short but wider widget adds art without title`() {
        val layout = WidgetLayout.compute(width = 130.dp, height = 60.dp)
        assertNotNull(layout.artSize)
        assertNull(layout.secondarySize)
        assertEquals(0, layout.titleLines)
    }

    @Test
    fun `wide and short widget shows art and all controls`() {
        val layout = WidgetLayout.compute(width = 260.dp, height = 70.dp)
        assertNotNull(layout.artSize)
        assertNotNull(layout.secondarySize)
        assertEquals(0, layout.titleLines)
    }

    @Test
    fun `enough height shows title above row`() {
        val layout = WidgetLayout.compute(width = 260.dp, height = 100.dp)
        assertEquals(WidgetLayout.Arrangement.SingleRow, layout.arrangement)
        assertTrue(layout.titleLines >= 1)
        assertNotNull(layout.secondarySize)
    }

    @Test
    fun `big square uses art beside title`() {
        val layout = WidgetLayout.compute(width = 220.dp, height = 200.dp)
        assertEquals(WidgetLayout.Arrangement.ArtBesideTitle, layout.arrangement)
        assertNotNull(layout.artSize)
        assertNotNull(layout.secondarySize)
        assertTrue(layout.titleLines >= 1)
    }

    @Test
    fun `responsive sizes include minimum resize size`() {
        // Must match minResizeWidth / minResizeHeight in widget_info.xml
        val min = DpSize(50.dp, 50.dp)
        assertTrue(WidgetLayout.RESPONSIVE_SIZES.contains(min))
        assertTrue(WidgetLayout.RESPONSIVE_SIZES.all { it.width >= min.width && it.height >= min.height })
        assertTrue(WidgetLayout.RESPONSIVE_SIZES.size <= 16)
    }

    // Picks the responsive size the launcher would use, and returns its layout
    private fun launcherLayout(
        w: Int,
        h: Int,
    ): WidgetLayout {
        val picked = pickResponsiveSize(WidgetLayout.RESPONSIVE_SIZES, DpSize(w.dp, h.dp))
        return WidgetLayout.compute(picked.width, picked.height)
    }

    // Sizes below are measured on a Pixel launcher (5 column grid)

    @Test
    fun `1x1 shows play only`() {
        // 65dp wide is too narrow for art next to play, 101dp too short to stack them
        val layout = launcherLayout(65, 101)
        assertNull(layout.artSize)
        assertTrue(layout.playSize >= 34.dp)
    }

    @Test
    fun `2x1 shows art and title in a row`() {
        val layout = launcherLayout(146, 101)
        assertEquals(WidgetLayout.Arrangement.SingleRow, layout.arrangement)
        assertNotNull(layout.artSize)
        assertTrue(layout.titleLines >= 1)
    }

    @Test
    fun `4x1 shows everything in a row`() {
        val layout = launcherLayout(308, 101)
        assertEquals(WidgetLayout.Arrangement.SingleRow, layout.arrangement)
        assertNotNull(layout.artSize)
        assertNotNull(layout.secondarySize)
        assertTrue(layout.titleLines >= 1)
    }

    @Test
    fun `3x1 shows everything in a row`() {
        val layout = launcherLayout(227, 101)
        assertEquals(WidgetLayout.Arrangement.SingleRow, layout.arrangement)
        assertNotNull(layout.artSize)
        assertNotNull(layout.secondarySize)
        assertTrue(layout.titleLines >= 1)
    }

    @Test
    fun `1 column tall stacks everything vertically`() {
        listOf(218, 329, 400).forEach { h ->
            val layout = launcherLayout(65, h)
            assertEquals(WidgetLayout.Arrangement.SingleColumn, layout.arrangement, "65x$h")
            assertNotNull(layout.artSize, "65x$h")
            assertNotNull(layout.secondarySize, "65x$h")
        }
    }

    @Test
    fun `1 column, 1 row taller than a square stacks art and play`() {
        val layout = launcherLayout(65, 150)
        assertEquals(WidgetLayout.Arrangement.SingleColumn, layout.arrangement)
        assertNotNull(layout.artSize)
    }

    @Test
    fun `2x2 shows everything in two rows`() {
        listOf(218, 329).forEach { h ->
            val layout = launcherLayout(146, h)
            assertEquals(WidgetLayout.Arrangement.TwoRows, layout.arrangement)
            assertNotNull(layout.artSize)
            assertNotNull(layout.secondarySize)
            assertTrue(layout.titleLines >= 1)
        }
    }

    @Test
    fun `3x2 shows art beside title`() {
        val layout = launcherLayout(227, 218)
        assertEquals(WidgetLayout.Arrangement.ArtBesideTitle, layout.arrangement)
        assertNotNull(layout.secondarySize)
        assertTrue(layout.titleLines >= 3)
    }

    @Test
    fun `one column tall widget stacks content vertically`() {
        val layout = WidgetLayout.compute(width = 70.dp, height = 300.dp)
        assertEquals(WidgetLayout.Arrangement.SingleColumn, layout.arrangement)
        assertNotNull(layout.artSize)
        assertNotNull(layout.secondarySize)
        // Too narrow for a few words
        assertEquals(0, layout.titleLines)
    }

    @Test
    fun `two column tall widget does not stack vertically`() {
        val layout = WidgetLayout.compute(width = 100.dp, height = 300.dp)
        assertTrue(layout.arrangement != WidgetLayout.Arrangement.SingleColumn)
        assertTrue(layout.titleLines >= 1)
    }

    @Test
    fun `only 1 column wide widgets use responsive sizes that stack vertically`() {
        for (w in 50..400 step 5) {
            for (h in 50..400 step 5) {
                val bucket = pickResponsiveSize(WidgetLayout.RESPONSIVE_SIZES, DpSize(w.dp, h.dp))
                val layout = WidgetLayout.compute(bucket.width, bucket.height)
                if (w >= 100) {
                    assertTrue(
                        layout.arrangement != WidgetLayout.Arrangement.SingleColumn,
                        "${w}x$h picked $bucket which stacks vertically",
                    )
                }
            }
        }
    }

    @Test
    fun `content never exceeds available space`() {
        val grid =
            (50..400 step 10).flatMap { w ->
                (40..400 step 10).map { h -> DpSize(w.dp, h.dp) }
            }
        (grid + WidgetLayout.RESPONSIVE_SIZES).forEach { size ->
            val l = WidgetLayout.compute(width = size.width, height = size.height)
            val availW = size.width - l.horizontalPadding * 2
            val availH = size.height - WidgetLayout.VERTICAL_PADDING * 2
            when (l.arrangement) {
                WidgetLayout.Arrangement.SingleRow -> {
                    var rowW = l.playSize
                    l.artSize?.let { rowW += it + WidgetLayout.GAP }
                    l.secondarySize?.let { rowW += (it + WidgetLayout.GAP) * 2 }
                    assertTrue(rowW <= availW, "width overflow at $size: $l")
                    assertTrue(l.playSize <= availH, "height overflow at $size: $l")
                }

                WidgetLayout.Arrangement.SingleColumn -> {
                    var colH = l.playSize
                    l.artSize?.let { colH += it + WidgetLayout.GAP }
                    if (l.titleLines > 0) colH += WidgetLayout.TITLE_GAP + 20.dp * l.titleLines
                    l.secondarySize?.let { colH += (it + WidgetLayout.GAP) * 2 }
                    assertTrue(l.playSize <= availW, "width overflow at $size: $l")
                    assertTrue(colH <= availH, "height overflow at $size: $l")
                }

                WidgetLayout.Arrangement.ArtBesideTitle -> {
                    val artW = (l.artSize ?: 0.dp) + WidgetLayout.GAP
                    val controlsW = l.playSize + ((l.secondarySize ?: 0.dp) + WidgetLayout.GAP) * 2
                    val totalH = (l.artSize ?: 0.dp) + WidgetLayout.GAP + l.playSize
                    assertTrue(artW < availW && controlsW <= availW, "width overflow at $size: $l")
                    assertTrue(totalH <= availH, "height overflow at $size: $l")
                }

                WidgetLayout.Arrangement.TwoRows -> {
                    val row1W = (l.artSize?.let { it + WidgetLayout.GAP } ?: 0.dp) + l.playSize
                    val row2W = l.secondarySize?.let { it * 2 + WidgetLayout.GAP } ?: 0.dp
                    var totalH = l.playSize
                    l.secondarySize?.let { totalH += it + WidgetLayout.GAP }
                    if (l.titleLines > 0) totalH += WidgetLayout.TITLE_GAP + 20.dp * l.titleLines
                    assertTrue(row1W <= availW && row2W <= availW, "width overflow at $size: $l")
                    assertTrue(totalH <= availH, "height overflow at $size: $l")
                }
            }
        }
    }
}
