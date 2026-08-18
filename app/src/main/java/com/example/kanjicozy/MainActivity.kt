package com.example.kanjicozy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

private val Bg = Color(0xFF121212)
private val Surface = Color(0xFF181818)
private val Card = Color(0xFF242424)

private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFB3B3B3)

private val Accent = Color(0xFF1DB954)
private val Learning = Color(0xFFFFB84D)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KanjiCozyApp(this)
        }
    }
}

@Composable
fun KanjiCozyApp(activity: ComponentActivity) {

    val context = activity

    var page by remember { mutableIntStateOf(0) }

    var kanji by remember {
        mutableStateOf(KanjiStore.current(context))
    }

    var interval by remember {
        mutableFloatStateOf(
            KanjiStore.interval(context).toFloat()
        )
    }

    var reading by remember {
        mutableStateOf(KanjiStore.showReading(context))
    }

    var translation by remember {
        mutableStateOf(KanjiStore.showTranslation(context))
    }

    var favorite by remember {
        mutableStateOf(
            KanjiStore.isFavorite(
                context,
                KanjiStore.currentIndex(context)
            )
        )
    }

    var learned by remember {
        mutableStateOf(
            KanjiStore.isLearned(
                context,
                KanjiStore.currentIndex(context)
            )
        )
    }

    var refreshKey by remember {
        mutableIntStateOf(0)
    }

    fun refreshCurrent() {
        kanji = KanjiStore.current(context)

        val index = KanjiStore.currentIndex(context)

        favorite = KanjiStore.isFavorite(context, index)
        learned = KanjiStore.isLearned(context, index)

        refreshKey++
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg,
            surface = Surface,
            surfaceVariant = Card,
            primary = Accent,
            secondary = Accent,
            onBackground = TextPrimary,
            onSurface = TextPrimary,
            onSurfaceVariant = TextSecondary
        )
    ) {

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Bg
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Header(page)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {

                    when (page) {

                        0 -> HomePage(
                            kanji = kanji,
                            reading = reading,
                            translation = translation,
                            learned = learned,
                            favorite = favorite,

                            onNext = {
                                KanjiStore.advance(context)
                                refreshCurrent()
                                KanjiWidgetProvider.updateAll(context)
                            },

                            onLearned = {
                                KanjiStore.markLearned(
                                    context,
                                    KanjiStore.currentIndex(context)
                                )

                                refreshCurrent()
                            },

                            onFavorite = {
                                favorite = KanjiStore.toggleFavorite(
                                    context,
                                    KanjiStore.currentIndex(context)
                                )

                                refreshKey++
                            }
                        )

                        1 -> CollectionPage(
                            title = "Learning",
                            subtitle = "Kanji you're still working on.",
                            indices = KanjiStore.learning(context),
                            accent = Learning,
                            empty = "Nothing here yet.",
                            refreshKey = refreshKey
                        )

                        2 -> CollectionPage(
                            title = "Learned",
                            subtitle = "Kanji you've completed.",
                            indices = KanjiStore.learned(context),
                            accent = Accent,
                            empty = "Mark a kanji as learned and it'll appear here.",
                            refreshKey = refreshKey
                        )

                        3 -> CollectionPage(
                            title = "Favorites",
                            subtitle = "Kanji you've saved.",
                            indices = KanjiStore.favorites(context),
                            accent = Accent,
                            empty = "Tap the star on a kanji to save it.",
                            refreshKey = refreshKey
                        )

                        4 -> ReviewPage(
                            context = context,
                            refreshKey = refreshKey,
                            onRefresh = {
                                refreshCurrent()
                            }
                        )

                        5 -> WidgetPage(
                            interval = interval,
                            reading = reading,
                            translation = translation,

                            onInterval = {
                                interval = it

                                KanjiStore.setInterval(
                                    context,
                                    it.toLong()
                                )

                                KanjiWidgetProvider.schedule(context)
                            },

                            onReading = {
                                reading = it

                                KanjiStore.setShowReading(
                                    context,
                                    it
                                )

                                KanjiWidgetProvider.updateAll(context)
                            },

                            onTranslation = {
                                translation = it

                                KanjiStore.setShowTranslation(
                                    context,
                                    it
                                )

                                KanjiWidgetProvider.updateAll(context)
                            }
                        )
                    }
                }

                BottomNavigation(
                    page = page,
                    onPageChanged = {
                        page = it
                        refreshKey++
                    }
                )
            }
        }
    }
}

@Composable
private fun Header(page: Int) {

    val subtitle = when (page) {
        0 -> "Your daily kanji."
        1 -> "Still learning."
        2 -> "Already learned."
        3 -> "Your favorites."
        4 -> "Time for review."
        5 -> "Widget settings."
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 22.dp,
                vertical = 18.dp
            )
    ) {

        Text(
            "KanjiCozy",
            fontSize = 29.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Text(
            subtitle,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun BottomNavigation(
    page: Int,
    onPageChanged: (Int) -> Unit
) {

    NavigationBar(
        containerColor = Surface
    ) {

        NavigationBarItem(
            selected = page == 0,
            onClick = { onPageChanged(0) },
            icon = {
                Text("⌂", fontSize = 20.sp)
            },
            label = {
                Text("Home")
            }
        )

        NavigationBarItem(
            selected = page == 1,
            onClick = { onPageChanged(1) },
            icon = {
                Text("◌", fontSize = 19.sp)
            },
            label = {
                Text("Learning")
            }
        )

        NavigationBarItem(
            selected = page == 2,
            onClick = { onPageChanged(2) },
            icon = {
                Text("✓", fontSize = 19.sp)
            },
            label = {
                Text("Learned")
            }
        )

        NavigationBarItem(
            selected = page == 3,
            onClick = { onPageChanged(3) },
            icon = {
                Text("★", fontSize = 19.sp)
            },
            label = {
                Text("Favorites")
            }
        )

        NavigationBarItem(
            selected = page == 4,
            onClick = { onPageChanged(4) },
            icon = {
                Text("↻", fontSize = 19.sp)
            },
            label = {
                Text("Review")
            }
        )

        NavigationBarItem(
            selected = page == 5,
            onClick = { onPageChanged(5) },
            icon = {
                Text("◈", fontSize = 19.sp)
            },
            label = {
                Text("Widget")
            }
        )
    }
}

@Composable
private fun HomePage(
    kanji: Kanji,
    reading: Boolean,
    translation: Boolean,
    learned: Boolean,
    favorite: Boolean,
    onNext: () -> Unit,
    onLearned: () -> Unit,
    onFavorite: () -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),

        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {

            Text(
                "Today's kanji",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(
                Modifier.height(8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Card,
                        RoundedCornerShape(30.dp)
                    )
                    .padding(24.dp),

                horizontalAlignment = Alignment.CenterHorizontally,

                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    kanji.character,
                    fontSize = 86.sp,
                    color = TextPrimary
                )

                if (reading) {

                    Text(
                        kanji.reading,
                        fontSize = 18.sp,
                        color = TextSecondary
                    )
                }

                if (translation) {

                    Text(
                        kanji.meaning,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Spacer(
                    Modifier.height(14.dp)
                )

                Text(
                    if (learned) "✓ Learned"
                    else "Learning",

                    color = if (learned)
                        Accent
                    else
                        Learning,

                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {

                Button(
                    onClick = onLearned,

                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),

                    shape = RoundedCornerShape(17.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent
                    )
                ) {

                    Text(
                        if (learned)
                            "Learned"
                        else
                            "Mark learned"
                    )
                }

                OutlinedButton(
                    onClick = onFavorite,

                    modifier = Modifier
                        .width(65.dp)
                        .height(52.dp),

                    shape = RoundedCornerShape(17.dp)
                ) {

                    Text(
                        if (favorite)
                            "★"
                        else
                            "☆",

                        fontSize = 21.sp,

                        color = if (favorite)
                            Accent
                        else
                            TextPrimary
                    )
                }
            }
        }

        item {

            Button(
                onClick = onNext,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),

                shape = RoundedCornerShape(17.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Card
                )
            ) {

                Text(
                    "Next kanji",
                    color = TextPrimary
                )
            }
        }

        item {
            ProgressCard()
        }
    }
}

@Composable
private fun ProgressCard() {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Surface,
                RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {

        Text(
            "Progress",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(
            Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Stat(
                "Learning",
                KanjiStore.learningCount(context),
                Learning
            )

            Stat(
                "Learned",
                KanjiStore.learnedCount(context),
                Accent
            )

            Stat(
                "Favorites",
                KanjiStore.favoriteCount(context),
                Accent
            )
        }
    }
}

@Composable
private fun Stat(
    label: String,
    value: Int,
    color: Color
) {

    Column {

        Text(
            value.toString(),
            color = color,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            label,
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun CollectionPage(
    title: String,
    subtitle: String,
    indices: List<Int>,
    accent: Color,
    empty: String,
    refreshKey: Int
) {

    refreshKey

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),

        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        item {

            Text(
                title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Text(
                subtitle,
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(
                Modifier.height(8.dp)
            )
        }

        if (indices.isEmpty()) {

            item {

                InfoCard(
                    "Nothing here yet.",
                    empty
                )
            }

        } else {

            items(indices) { index ->

                val kanji = KanjiBank.all[index]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Card,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        kanji.character,
                        fontSize = 40.sp,
                        color = TextPrimary
                    )

                    Spacer(
                        Modifier.width(16.dp)
                    )

                    Column(
                        Modifier.weight(1f)
                    ) {

                        Text(
                            kanji.reading,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        Text(
                            kanji.meaning,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        "●",
                        color = accent,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewPage(
    context: android.content.Context,
    refreshKey: Int,
    onRefresh: () -> Unit
) {

    refreshKey

    val review = KanjiStore.review(context)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),

        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            Text(
                "Review",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Text(
                "${review.size} kanji ready for review.",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        if (review.isEmpty()) {

            item {

                InfoCard(
                    "Review",
                    "Your review queue is empty."
                )
            }

        } else {

            items(review) { index ->

                val kanji = KanjiBank.all[index]

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Card,
                            RoundedCornerShape(22.dp)
                        )
                        .padding(18.dp)
                ) {

                    Text(
                        kanji.character,
                        fontSize = 48.sp,
                        color = TextPrimary
                    )

                    Text(
                        kanji.reading,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )

                    Text(
                        kanji.meaning,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(
                        Modifier.height(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {

                        OutlinedButton(
                            onClick = {

                                KanjiStore.markLearningAgain(
                                    context,
                                    index
                                )

                                KanjiStore.removeFromReview(
                                    context,
                                    index
                                )

                                onRefresh()
                            },

                            modifier = Modifier.weight(1f)
                        ) {

                            Text("Again")
                        }

                        Button(
                            onClick = {

                                KanjiStore.removeFromReview(
                                    context,
                                    index
                                )

                                onRefresh()
                            },

                            modifier = Modifier.weight(1f),

                            colors = ButtonDefaults.buttonColors(
                                containerColor = Accent
                            )
                        ) {

                            Text("Good")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetPage(
    interval: Float,
    reading: Boolean,
    translation: Boolean,
    onInterval: (Float) -> Unit,
    onReading: (Boolean) -> Unit,
    onTranslation: (Boolean) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),

        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {

        item {

            InfoCard(
                "Widget",
                "Your kanji changes automatically on your launcher."
            )
        }

        item {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Card,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(18.dp)
            ) {

                Text(
                    "Change every ${interval.toInt()} min",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Slider(
                    value = interval,
                    onValueChange = onInterval,
                    valueRange = 5f..1440f,

                    colors = SliderDefaults.colors(
                        thumbColor = Accent,
                        activeTrackColor = Accent
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        "5 min",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Text(
                        "24 hours",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        item {

            ToggleCard(
                "Hiragana",
                "Show the Japanese reading.",
                reading,
                onReading
            )
        }

        item {

            ToggleCard(
                "Translation",
                "Show the English meaning.",
                translation,
                onTranslation
            )
        }
    }
}

@Composable
private fun ToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Card,
                RoundedCornerShape(22.dp)
            )
            .padding(18.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            Modifier.weight(1f)
        ) {

            Text(
                title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                subtitle,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onChecked,

            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent
            )
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    subtitle: String
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Card,
                RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {

        Text(
            title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(
            Modifier.height(4.dp)
        )

        Text(
            subtitle,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}
