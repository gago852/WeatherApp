package com.gago.weatherapp.ui.main.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.gago.weatherapp.R

/**
 * Asks what to do when the daily notification is enabled in Settings but notifications were
 * revoked at the system level: re-enable them (opens the app's notification settings, the
 * path that works on every API level even when the permission is permanently denied) or turn
 * the feature off. Turning it off does not touch the background sync, which also feeds the
 * offline cache. Shown at most once per session; dismissing postpones it to the next one.
 */
@Composable
fun NotificationsRevokedDialog(
    notificationsEnabled: Boolean,
    onTurnOff: () -> Unit
) {
    val context = LocalContext.current
    var dismissedThisSession by rememberSaveable { mutableStateOf(false) }
    var show by remember { mutableStateOf(false) }

    LifecycleResumeEffect(notificationsEnabled, dismissedThisSession) {
        show = notificationsEnabled && !dismissedThisSession &&
                !NotificationManagerCompat.from(context).areNotificationsEnabled()
        onPauseOrDispose {}
    }

    if (!show) return
    AlertDialog(
        onDismissRequest = {
            show = false
            dismissedThisSession = true
        },
        title = { Text(stringResource(R.string.notifications_revoked_title)) },
        text = { Text(stringResource(R.string.notifications_revoked_message)) },
        confirmButton = {
            TextButton(onClick = {
                show = false
                dismissedThisSession = true
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                )
            }) { Text(stringResource(R.string.notifications_revoked_reenable)) }
        },
        dismissButton = {
            TextButton(onClick = {
                show = false
                dismissedThisSession = true
                onTurnOff()
            }) { Text(stringResource(R.string.notifications_revoked_turn_off)) }
        }
    )
}
