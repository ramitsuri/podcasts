package com.ramitsuri.podcasts.widget.ui

import androidx.compose.ui.unit.DpSize

/**
 * Mirrors how the launcher picks a layout from SizeMode.Responsive sizes on Android 12+:
 * the closest size that fits inside the widget, or the smallest one if nothing fits.
 */
internal fun pickResponsiveSize(
    sizes: Collection<DpSize>,
    widget: DpSize,
): DpSize {
    var best: DpSize? = null
    var bestDistance = Float.MAX_VALUE
    for (size in sizes) {
        if (size.width <= widget.width && size.height <= widget.height) {
            val dw = widget.width.value - size.width.value
            val dh = widget.height.value - size.height.value
            val distance = dw * dw + dh * dh
            if (distance < bestDistance) {
                bestDistance = distance
                best = size
            }
        }
    }
    return best ?: sizes.minBy { it.width.value * it.height.value }
}
