package com.example.kanjicozy

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        updateWidget(context, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        schedule(context)
    }

    override fun onDisabled(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
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
            val manager = AppWidgetManager.getInstance(context)

            val component = ComponentName(
                context,
                KanjiWidgetProvider::class.java
            )

            val ids = manager.getAppWidgetIds(component)

            ids.forEach {
                updateWidget(context, it)
            }
        }

        private fun updateWidget(
            context: Context,
            widgetId: Int
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val options = manager.getAppWidgetOptions(widgetId)

            val width = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                180
            )

            val height = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                110
            )

            val layout = when {
                width < 180 || height < 100 ->
                    R.layout.kanji_widget_small

                width < 280 || height < 170 ->
                    R.layout.kanji_widget_medium

                else ->
                    R.layout.kanji_widget_large
            }

            val kanji = KanjiStore.current(context)

            val views = RemoteViews(
                context.packageName,
                layout
            )

            views.setTextViewText(
                R.id.widget_kanji,
                kanji.character
            )

            views.setTextViewText(
                R.id.widget_reading,
                if (KanjiStore.showReading(context))
                    kanji.reading
                else
                    ""
            )

            views.setTextViewText(
                R.id.widget_translation,
                if (KanjiStore.showTranslation(context))
                    kanji.meaning
                else
                    ""
            )

            manager.updateAppWidget(widgetId, views)
        }

        fun schedule(context: Context) {
            val alarm =
                context.getSystemService(Context.ALARM_SERVICE)
                    as AlarmManager

            val intervalMs =
                KanjiStore.interval(context) * 60_000L

            alarm.cancel(pendingIntent(context))

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
            val intent = Intent(
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
