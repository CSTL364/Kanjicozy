package com.example.kanjicozy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KanjiCozyApp(this) }
    }
}

@Composable
fun KanjiCozyApp(activity: ComponentActivity) {
    val context = activity

    var page by remember { mutableStateOf(0) }
    var dark by remember { mutableStateOf(false) }
    var interval by remember { mutableFloatStateOf(KanjiStore.interval(context).toFloat()) }
    var reading by remember { mutableStateOf(KanjiStore.showReading(context)) }
    var translation by remember { mutableStateOf(KanjiStore.showTranslation(context)) }
    var kanji by remember { mutableStateOf(KanjiStore.current(context)) }

    val bg = if (dark) Color(0xFF141210) else Color(0xFFF7F2EA)
    val card = if (dark) Color(0xFF25211E) else Color(0xFFFFFCF7)
    val ink = if (dark) Color(0xFFF4EEE7) else Color(0xFF302C2A)
    val muted = if (dark) Color(0xFFA69C95) else Color(0xFF8E8580)

    MaterialTheme(
        colorScheme = if (dark) {
            darkColorScheme(
                background = bg,
                surface = card,
                onBackground = ink,
                onSurface = ink,
                primary = Color(0xFFD8C1A5)
            )
        } else {
            lightColorScheme(
                background = bg,
                surface = card,
                onBackground = ink,
                onSurface = ink,
                primary = Color(0xFF806650)
            )
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = bg
        ) {
            Column(Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Kanji Cozy",
                                fontSize = 29.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ink
                            )

                            Text(
                                when (page) {
                                    0 -> "Your little kanji corner."
                                    1 -> "Shape your widget."
                                    else -> "Make it yours."
                                },
                                fontSize = 13.sp,
                                color = muted
                            )
                        }

                        Text(
                            if (dark) "☾" else "☀",
                            fontSize = 25.sp,
                            color = ink,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { dark = !dark }
                                .padding(10.dp)
                        )
                    }
                }

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (page) {
                        0 -> HomePage(
                            card = card,
                            ink = ink,
                            muted = muted,
                            kanji = kanji,
                            reading = reading,
                            translation = translation,
                            onNext = {
                                kanji = KanjiStore.advance(context)
                                KanjiWidgetProvider.updateAll(context)
                            },
                            onWidget = { page = 1 }
                        )

                        1 -> WidgetPage(
                            card = card,
                            ink = ink,
                            muted = muted,
                            interval = interval,
                            reading = reading,
                            translation = translation,
                            onInterval = {
                                interval = it
                                KanjiStore.setInterval(context, it.toLong())
                                KanjiWidgetProvider.schedule(context)
                            },
                            onReading = {
                                reading = it
                                KanjiStore.setShowReading(context, it)
                                KanjiWidgetProvider.updateAll(context)
                            },
                            onTranslation = {
                                translation = it
                                KanjiStore.setShowTranslation(context, it)
                                KanjiWidgetProvider.updateAll(context)
                            }
                        )

                        2 -> SettingsPage(
                            card = card,
                            ink = ink,
                            muted = muted,
                            dark = dark,
                            onDark = { dark = it }
                        )
                    }
                }

                NavigationBar(
                    containerColor = card
                ) {
                    NavigationBarItem(
                        selected = page == 0,
                        onClick = { page = 0 },
                        icon = { Text("⌂", fontSize = 21.sp) },
                        label = { Text("Home") }
                    )

                    NavigationBarItem(
                        selected = page == 1,
                        onClick = { page = 1 },
                        icon = { Text("◈", fontSize = 20.sp) },
                        label = { Text("Widget") }
                    )

                    NavigationBarItem(
                        selected = page == 2,
                        onClick = { page = 2 },
                        icon = { Text("⚙", fontSize = 19.sp) },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomePage(
    card: Color,
    ink: Color,
    muted: Color,
    kanji: Kanji,
    reading: Boolean,
    translation: Boolean,
    onNext: () -> Unit,
    onWidget: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Today's kanji", color = muted, fontSize = 13.sp)

            Spacer(Modifier.height(8.dp))

            Column(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(34.dp))
                    .background(card)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    kanji.character,
                    fontSize = 82.sp,
                    color = ink
                )

                if (reading) {
                    Text(
                        kanji.reading,
                        fontSize = 18.sp,
                        color = muted
                    )
                }

                if (translation) {
                    Text(
                        kanji.meaning,
                        fontSize = 13.sp,
                        color = muted.copy(alpha = .65f)
                    )
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Next kanji")
                }

                OutlinedButton(
                    onClick = onWidget,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Customize")
                }
            }
        }

        item {
            InfoCard(
                card,
                ink,
                muted,
                "Home screen widget",
                "Your kanji changes automatically on your launcher."
            )
        }
    }
}

@Composable
private fun WidgetPage(
    card: Color,
    ink: Color,
    muted: Color,
    interval: Float,
    reading: Boolean,
    translation: Boolean,
    onInterval: (Float) -> Unit,
    onReading: (Boolean) -> Unit,
    onTranslation: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(card)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("静", fontSize = 64.sp, color = ink)

                if (reading) {
                    Text("しずか", fontSize = 16.sp, color = muted)
                }

                if (translation) {
                    Text(
                        "quiet",
                        fontSize = 12.sp,
                        color = muted.copy(alpha = .65f)
                    )
                }

                Spacer(Modifier.height(7.dp))

                Text(
                    "Transparent widget preview",
                    fontSize = 11.sp,
                    color = muted
                )
            }
        }

        item {
            Text(
                "Change every ${interval.toInt()} min",
                color = ink,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Slider(
                value = interval,
                onValueChange = onInterval,
                valueRange = 5f..1440f
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("5 min", fontSize = 11.sp, color = muted)
                Text("24 hours", fontSize = 11.sp, color = muted)
            }
        }

        item {
            ToggleCard(
                card,
                ink,
                muted,
                "Hiragana",
                "Show the Japanese reading.",
                reading,
                onReading
            )
        }

        item {
            ToggleCard(
                card,
                ink,
                muted,
                "Translation",
                "Show the darker translation.",
                translation,
                onTranslation
            )
        }

        item {
            InfoCard(
                card,
                ink,
                muted,
                "Resizable widget",
                "Small widgets show the kanji. Larger widgets reveal more information."
            )
        }
    }
}

@Composable
private fun SettingsPage(
    card: Color,
    ink: Color,
    muted: Color,
    dark: Boolean,
    onDark: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        item {
            ToggleCard(
                card,
                ink,
                muted,
                "Dark mode",
                "Use the warm dark palette.",
                dark,
                onDark
            )
        }

        item {
            InfoCard(
                card,
                ink,
                muted,
                "Kanji Cozy",
                "A small, quiet kanji widget for your home screen."
            )
        }

        item {
            InfoCard(
                card,
                ink,
                muted,
                "Widget sizes",
                "Android lets you resize the widget horizontally and vertically."
            )
        }
    }
}

@Composable
private fun ToggleCard(
    card: Color,
    ink: Color,
    muted: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(card)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = ink,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                subtitle,
                color = muted,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onChecked
        )
    }
}

@Composable
private fun InfoCard(
    card: Color,
    ink: Color,
    muted: Color,
    title: String,
    subtitle: String
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(card)
            .padding(18.dp)
    ) {
        Text(
            title,
            color = ink,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(4.dp))

        Text(
            subtitle,
            color = muted,
            fontSize = 12.sp
        )
    }
}
