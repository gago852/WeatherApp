package com.gago.weatherapp.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gago.weatherapp.BuildConfig
import com.gago.weatherapp.R
import com.gago.weatherapp.domain.model.Weather
import com.gago.weatherapp.domain.usecase.SyncActiveCityUseCase
import com.gago.weatherapp.ui.utils.capitalizeWords
import com.gago.weatherapp.ui.utils.getCurrentLanguage
import com.gago.weatherapp.widget.WeatherWidget
import androidx.glance.appwidget.updateAll
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.math.roundToInt

/**
 * Periodic background refresh of the active city. It writes through the same offline cache
 * the app and the widget read, and optionally posts the daily summary notification.
 */
@HiltWorker
class WeatherSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncActiveCity: SyncActiveCityUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return when (val outcome = syncActiveCity(
            apiKey = BuildConfig.API_KEY,
            lang = getCurrentLanguage(applicationContext)
        )) {
            is SyncActiveCityUseCase.Outcome.NoActiveCity -> Result.success()

            is SyncActiveCityUseCase.Outcome.Refreshed -> {
                if (outcome.notify) postSummaryNotification(outcome.weather)
                WeatherWidget().updateAll(applicationContext)
                Result.success()
            }

            is SyncActiveCityUseCase.Outcome.Failed ->
                if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    private fun postSummaryNotification(weather: Weather) {
        val granted = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return

        val manager = NotificationManagerCompat.from(applicationContext)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(
                CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT
            )
                .setName(applicationContext.getString(R.string.notification_channel_name))
                .build()
        )

        val current = weather.currentWeather
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(
                applicationContext.getString(
                    R.string.notification_summary_title, current.name
                )
            )
            .setContentText(
                "${current.weatherData.temp.roundToInt()}° · " +
                        current.weatherConditions.description.capitalizeWords()
            )
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "weather_summary"
        const val NOTIFICATION_ID = 1001
        const val MAX_RETRIES = 3
    }
}
