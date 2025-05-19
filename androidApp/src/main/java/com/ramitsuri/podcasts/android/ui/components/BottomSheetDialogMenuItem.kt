package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ramitsuri.podcasts.android.ui.PreviewTheme
import com.ramitsuri.podcasts.android.ui.ThemePreview

@Composable
fun BottomSheetDialogMenuItem(
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    text: String,
    subtitle: String? = null,
    switchState: SwitchState? = null,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                startIcon?.let {
                    Icon(imageVector = it, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column {
                    Text(text)
                    subtitle?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = it, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                switchState?.let {
                    Switch(
                        checked = it.checked,
                        onCheckedChange = null,
                        thumbContent =
                            if (it.checked) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            } else {
                                null
                            },
                    )
                }
                endIcon?.let {
                    Icon(imageVector = it, contentDescription = null)
                }
            }
        },
        onClick = onClick,
    )
}

data class SwitchState(
    val checked: Boolean,
)

@ThemePreview
@Composable
private fun MenuItem() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BottomSheetDialogMenuItem(
                startIcon = Icons.Filled.Check,
                text = "Text",
                subtitle = "Subtitle",
                switchState = SwitchState(checked = true),
                onClick = {},
            )
            BottomSheetDialogMenuItem(
                startIcon = Icons.Filled.Check,
                text = "Text",
                switchState = SwitchState(checked = true),
                onClick = {},
            )
            BottomSheetDialogMenuItem(
                text = "Text",
                subtitle = "Subtitle",
                switchState = SwitchState(checked = true),
                onClick = {},
            )
            BottomSheetDialogMenuItem(
                text = "Text",
                subtitle = "Subtitle",
                endIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                onClick = {},
            )
            BottomSheetDialogMenuItem(
                text = "Text",
                endIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                onClick = {},
            )
        }
    }
}
