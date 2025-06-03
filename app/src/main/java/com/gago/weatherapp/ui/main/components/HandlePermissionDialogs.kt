package com.gago.weatherapp.ui.main.components

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat

@Composable
fun HandlePermissionDialogs(
    dialogQueue: List<String>,
    onDismiss: () -> Unit,
    onPermissionRequest: (String) -> Unit,
    onGoToSettings: (Context) -> Unit
) {
    val context = LocalContext.current

    dialogQueue.reversed().forEach { permission ->
        PermissionDialog(
            permissionTextProvider = when (permission) {
                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                    AccessCoarseLocationPermissionTextProvider()
                }
                else -> return@forEach
            },
            isPermanentlyDeclined = !ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                permission
            ),
            onDismiss = onDismiss,
            onOkClick = { onPermissionRequest(permission) },
            onGoToAppSettingsClick = { onGoToSettings(context) }
        )
    }
}
