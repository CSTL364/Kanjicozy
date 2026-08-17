package com.example.kanjicozy

import android.content.Context

object KanjiStore {
    private const val PREFS = "kanji_cozy"
    private const val INTERVAL = "interval"
    private const val SHOW_READING = "show_reading"
    private const val SHOW_TRANSLATION = "show_translation"
    private const val INDEX = "index"

    fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun interval(context: Context) = prefs(context).getLong(INTERVAL, 60L)
    fun setInterval(context: Context, minutes: Long) =
        prefs(context).edit().putLong(INTERVAL, minutes.coerceIn(5L, 1440L)).apply()

    fun showReading(context: Context) = prefs(context).getBoolean(SHOW_READING, true)
    fun setShowReading(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(SHOW_READING, value).apply()

    fun showTranslation(context: Context) = prefs(context).getBoolean(SHOW_TRANSLATION, true)
    fun setShowTranslation(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(SHOW_TRANSLATION, value).apply()

    fun current(context: Context): Kanji {
        val i = prefs(context).getInt(INDEX, 0).coerceIn(0, KanjiBank.all.lastIndex)
        return KanjiBank.all[i]
    }

    fun advance(context: Context): Kanji {
        val next = (prefs(context).getInt(INDEX, 0) + 1) % KanjiBank.all.size
        prefs(context).edit().putInt(INDEX, next).apply()
        return KanjiBank.all[next]
    }
}
