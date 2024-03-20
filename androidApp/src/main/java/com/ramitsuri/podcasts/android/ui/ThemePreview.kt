package com.ramitsuri.podcasts.android.ui

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Light")
annotation class ThemePreview

@Composable
fun PreviewTheme(content: @Composable () -> Unit) {
    AppTheme {
        Surface {
            content()
        }
    }
}
