package com.ramitsuri.podcasts.android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheetDialogMenuItem(
    icon: ImageVector,
    text: String,
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
                Icon(imageVector = icon, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text)
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
            }
        },
        onClick = onClick,
    )
}

data class SwitchState(
    val checked: Boolean,
)
