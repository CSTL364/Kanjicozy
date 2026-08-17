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

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        updateAll(context)
        schedule(context)
    }

    override fun onEnabled(context: Context) {
        schedule(context)
    }

    override fun onDisabled(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent(context))
    }

    companion object {
        private const val ACTION_TICK = "com.example.kanjicozy.TICK"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, KanjiWidgetProvider::class.java))
            val k = KanjiStore.current(context)
            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.kanji_widget)
                views.setTextViewText(R.id.widget_kanji, k.character)
                views.setTextViewText(R.id.widget_reading, if (KanjiStore.showReading(context)) k.reading else "")
                views.setTextViewText(R.id.widget_translation, if (KanjiStore.showTranslation(context)) k.meaning else "")
                manager.updateAppWidget(id, views)
            }
        }

        fun schedule(context: Context) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intervalMs = KanjiStore.interval(context) * 60_000L
            alarm.cancel(pendingIntent(context))
            alarm.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + intervalMs,
                intervalMs,
                pendingIntent(context)
            )
        }

        private fun pendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                77,
                Intent(context, KanjiWidgetProvider::class.java).setAction(ACTION_TICK),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_TICK) {
                KanjiStore.advance(context)
                updateAll(context)
                schedule(context)
            } else {
                super.onReceive(context, intent)
            }
        }
    }
}
