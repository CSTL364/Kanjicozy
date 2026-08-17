package com.example.kanjicozy

import android.content.Context

object KanjiStore {
    private const val PREFS = "kanji_cozy"

    private const val INTERVAL = "interval"
    private const val SHOW_READING = "show_reading"
    private const val SHOW_TRANSLATION = "show_translation"
    private const val INDEX = "index"

    private const val LEARNED = "learned"
    private const val LEARNING = "learning"
    private const val FAVORITES = "favorites"
    private const val REVIEW = "review"

    fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun interval(context: Context) =
        prefs(context).getLong(INTERVAL, 60L)

    fun setInterval(context: Context, minutes: Long) =
        prefs(context)
            .edit()
            .putLong(INTERVAL, minutes.coerceIn(5L, 1440L))
            .apply()

    fun showReading(context: Context) =
        prefs(context).getBoolean(SHOW_READING, true)

    fun setShowReading(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(SHOW_READING, value).apply()

    fun showTranslation(context: Context) =
        prefs(context).getBoolean(SHOW_TRANSLATION, true)

    fun setShowTranslation(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean(SHOW_TRANSLATION, value).apply()

    fun currentIndex(context: Context): Int = prefs(context).getInt(INDEX, 0).coerceIn(0, KanjiBank.all.lastIndex)

    fun current(context: Context): Kanji {
        val i = prefs(context)
            .getInt(INDEX, 0)
            .coerceIn(0, KanjiBank.all.lastIndex)

        markLearning(context, i)

        return KanjiBank.all[i]
    }

    fun advance(context: Context): Kanji {
        val currentIndex = prefs(context).getInt(INDEX, 0)

        markLearning(context, currentIndex)

        val next = (currentIndex + 1) % KanjiBank.all.size

        prefs(context)
            .edit()
            .putInt(INDEX, next)
            .apply()

        markLearning(context, next)

        return KanjiBank.all[next]
    }

    private fun getSet(context: Context, key: String): MutableSet<String> {
        return prefs(context)
            .getStringSet(key, emptySet())
            ?.toMutableSet()
            ?: mutableSetOf()
    }

    private fun saveSet(
        context: Context,
        key: String,
        set: Set<String>
    ) {
        prefs(context)
            .edit()
            .putStringSet(key, set)
            .apply()
    }

    private fun id(index: Int): String =
        index.toString()

    fun isLearned(context: Context, index: Int): Boolean =
        getSet(context, LEARNED).contains(id(index))

    fun isLearning(context: Context, index: Int): Boolean =
        getSet(context, LEARNING).contains(id(index))

    fun isFavorite(context: Context, index: Int): Boolean =
        getSet(context, FAVORITES).contains(id(index))

    fun isReview(context: Context, index: Int): Boolean =
        getSet(context, REVIEW).contains(id(index))

    fun markLearning(context: Context, index: Int) {
        if (isLearned(context, index)) return

        val set = getSet(context, LEARNING)
        set.add(id(index))
        saveSet(context, LEARNING, set)
    }

    fun markLearned(context: Context, index: Int) {
        val learned = getSet(context, LEARNED)
        learned.add(id(index))
        saveSet(context, LEARNED, learned)

        val learning = getSet(context, LEARNING)
        learning.remove(id(index))
        saveSet(context, LEARNING, learning)

        val review = getSet(context, REVIEW)
        review.add(id(index))
        saveSet(context, REVIEW, review)
    }

    fun markLearningAgain(context: Context, index: Int) {
        val learned = getSet(context, LEARNED)
        learned.remove(id(index))
        saveSet(context, LEARNED, learned)

        val learning = getSet(context, LEARNING)
        learning.add(id(index))
        saveSet(context, LEARNING, learning)
    }

    fun toggleFavorite(context: Context, index: Int): Boolean {
        val set = getSet(context, FAVORITES)

        val nowFavorite = if (set.contains(id(index))) {
            set.remove(id(index))
            false
        } else {
            set.add(id(index))
            true
        }

        saveSet(context, FAVORITES, set)
        return nowFavorite
    }

    fun removeFromReview(context: Context, index: Int) {
        val set = getSet(context, REVIEW)
        set.remove(id(index))
        saveSet(context, REVIEW, set)
    }

    fun learned(context: Context): List<Int> =
        getSet(context, LEARNED)
            .mapNotNull { it.toIntOrNull() }
            .filter { it in KanjiBank.all.indices }
            .sorted()

    fun learning(context: Context): List<Int> =
        getSet(context, LEARNING)
            .mapNotNull { it.toIntOrNull() }
            .filter { it in KanjiBank.all.indices }
            .sorted()

    fun favorites(context: Context): List<Int> =
        getSet(context, FAVORITES)
            .mapNotNull { it.toIntOrNull() }
            .filter { it in KanjiBank.all.indices }
            .sorted()

    fun review(context: Context): List<Int> =
        getSet(context, REVIEW)
            .mapNotNull { it.toIntOrNull() }
            .filter { it in KanjiBank.all.indices }
            .sorted()

    fun learnedCount(context: Context) =
        learned(context).size

    fun learningCount(context: Context) =
        learning(context).size

    fun favoriteCount(context: Context) =
        favorites(context).size

    fun reviewCount(context: Context) =
        review(context).size
}
