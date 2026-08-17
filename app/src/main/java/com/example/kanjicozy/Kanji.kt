package com.example.kanjicozy

data class Kanji(
    val character: String,
    val reading: String,
    val meaning: String
)

object KanjiBank {
    val all = listOf(
        Kanji("静", "しずか", "quiet"),
        Kanji("光", "ひかり", "light"),
        Kanji("森", "もり", "forest"),
        Kanji("夢", "ゆめ", "dream"),
        Kanji("空", "そら", "sky"),
        Kanji("雨", "あめ", "rain"),
        Kanji("花", "はな", "flower"),
        Kanji("月", "つき", "moon"),
        Kanji("星", "ほし", "star"),
        Kanji("風", "かぜ", "wind"),
        Kanji("旅", "たび", "journey"),
        Kanji("音", "おと", "sound")
    )
}
