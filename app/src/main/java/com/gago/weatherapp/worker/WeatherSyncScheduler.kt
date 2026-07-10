package com.gago.weatherapp.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Keeps the periodic sync aligned with Settings.refreshIntervalMinutes: WorkManager does not
 * go below 15 minutes, and 0 (manual) cancels the work entirely.
 */
object WeatherSyncScheduler {

    const val WORK_NAME = "weather-sync"
    const val MIN_INTERVAL_MINUTES = 15

    fun schedule(context: Context, intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(context)
        if (intervalMinutes < MIN_INTERVAL_MINUTES) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
