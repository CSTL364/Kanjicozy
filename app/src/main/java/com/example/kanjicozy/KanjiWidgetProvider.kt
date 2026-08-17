package com.example.kanjicozy

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.widget.RemoteViews

class KanjiWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        ids: IntArray
    ) {
        updateAll(context)
        schedule(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidget(context, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        schedule(context)
    }

    override fun onDisabled(context: Context) {
        val alarm =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarm.cancel(pendingIntent(context))
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action == ACTION_TICK) {
            KanjiStore.advance(context)
            updateAll(context)
            schedule(context)
        } else {
            super.onReceive(context, intent)
        }
    }

    companion object {

        private const val ACTION_TICK =
            "com.example.kanjicozy.TICK"

        fun updateAll(context: Context) {

            val manager =
                AppWidgetManager.getInstance(context)

            val component =
                ComponentName(
                    context,
                    KanjiWidgetProvider::class.java
                )

            val ids =
                manager.getAppWidgetIds(component)

            ids.forEach {
                updateWidget(context, it)
            }
        }

        private fun updateWidget(
            context: Context,
            widgetId: Int
        ) {

            val manager =
                AppWidgetManager.getInstance(context)

            val options =
                manager.getAppWidgetOptions(widgetId)

            val width =
                options.getInt(
                    AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                    110
                )

            val height =
                options.getInt(
                    AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                    70
                )

            val layout =
                when {
                    width < 150 && height < 90 ->
                        R.layout.kanji_widget_small

                    width < 260 || height < 160 ->
                        R.layout.kanji_widget_medium

                    else ->
                        R.layout.kanji_widget_large
                }

            val views =
                RemoteViews(
                    context.packageName,
                    layout
                )

            val kanji =
                KanjiStore.current(context)

            views.setTextViewText(
                R.id.widget_kanji,
                kanji.character
            )

            views.setTextViewText(
                R.id.widget_reading,
                kanji.reading
            )

            views.setTextViewText(
                R.id.widget_translation,
                kanji.meaning
            )

            val textColor =
                getWallpaperTextColor(context)

            val secondaryColor =
                getWallpaperSecondaryColor(context)

            views.setTextColor(
                R.id.widget_kanji,
                textColor
            )

            views.setTextColor(
                R.id.widget_reading,
                secondaryColor
            )

            views.setTextColor(
                R.id.widget_translation,
                secondaryColor
            )

            manager.updateAppWidget(
                widgetId,
                views
            )
        }

        private fun getWallpaperTextColor(
            context: Context
        ): Int {
            if (android.os.Build.VERSION.SDK_INT >= 27) {
                val colors = WallpaperManager
                    .getInstance(context)
                    .getWallpaperColors(
                        WallpaperManager.FLAG_SYSTEM
                    )

                val color = colors?.primaryColor?.toArgb()
                    ?: return Color.WHITE

                return if (isDark(color))
                    Color.WHITE
                else
                    Color.rgb(35, 32, 30)
            }

            return Color.WHITE
        }

        private fun getWallpaperSecondaryColor(
            context: Context
        ): Int {
            if (android.os.Build.VERSION.SDK_INT >= 27) {
                val colors = WallpaperManager
                    .getInstance(context)
                    .getWallpaperColors(
                        WallpaperManager.FLAG_SYSTEM
                    )

                val color = colors?.primaryColor?.toArgb()
                    ?: return Color.LTGRAY

                return if (isDark(color))
                    Color.rgb(220, 220, 220)
                else
                    Color.rgb(90, 85, 80)
            }

            return Color.LTGRAY
        }

        private fun isDark(color: Int): Boolean {
            val luminance =
                Color.red(color) * 0.299 +
                Color.green(color) * 0.587 +
                Color.blue(color) * 0.114

            return luminance < 145
        }

        fun schedule(context: Context) {

            val alarm =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            val intervalMs =
                KanjiStore.interval(context) * 60_000L

            alarm.cancel(
                pendingIntent(context)
            )

            alarm.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + intervalMs,
                intervalMs,
                pendingIntent(context)
            )
        }

        private fun pendingIntent(
            context: Context
        ): PendingIntent {

            val intent =
                Intent(
                    context,
                    KanjiWidgetProvider::class.java
                ).setAction(ACTION_TICK)

            return PendingIntent.getBroadcast(
                context,
                77,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
