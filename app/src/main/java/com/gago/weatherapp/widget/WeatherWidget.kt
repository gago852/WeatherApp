package com.gago.weatherapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.gago.weatherapp.R
import com.gago.weatherapp.data.datastore.Settings
import com.gago.weatherapp.data.datastore.WeatherCache
import com.gago.weatherapp.ui.MainActivity
import com.gago.weatherapp.ui.utils.capitalizeWords
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull

/**
 * 4×1 home-screen widget: active city, temperature and condition, read from the offline
 * cache (never calls the API itself; the periodic sync worker keeps the cache fresh and
 * asks the widget to re-render).
 */
class WeatherWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun settingsDataStore(): DataStore<Settings>
        fun weatherCacheDataStore(): DataStore<WeatherCache>
    }

    override suspend fun provideGlance(context: Context, id: androidx.glance.GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, WidgetEntryPoint::class.java
        )
        val settings = entryPoint.settingsDataStore().data.firstOrNull() ?: Settings()
        val cache = entryPoint.weatherCacheDataStore().data.firstOrNull() ?: WeatherCache()
        val data = buildWidgetData(settings, cache)

        provideContent {
            GlanceTheme {
                WidgetContent(data)
            }
        }
    }

    @Composable
    private fun WidgetContent(data: WidgetData?) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(16.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (data == null) {
                Text(
                    text = LocalContextText(R.string.widget_no_city),
                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp)
                )
                return@Row
            }
            Image(
                provider = ImageProvider(data.icon),
                contentDescription = data.description,
                modifier = GlanceModifier.size(44.dp)
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Text(
                text = data.temperature,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Column {
                Text(
                    text = data.cityName,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                Text(
                    text = data.description.capitalizeWords(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }

    @Composable
    private fun LocalContextText(resId: Int): String =
        androidx.glance.LocalContext.current.getString(resId)
}

class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()
}
