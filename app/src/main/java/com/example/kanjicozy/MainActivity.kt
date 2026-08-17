package com.example.kanjicozy

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
    var dark by remember { mutableStateOf(false) }
    var interval by remember { mutableFloatStateOf(KanjiStore.interval(context).toFloat()) }
    var reading by remember { mutableStateOf(KanjiStore.showReading(context)) }
    var translation by remember { mutableStateOf(KanjiStore.showTranslation(context)) }
    var kanji by remember { mutableStateOf(KanjiStore.current(context)) }

    val bg = if (dark) Color(0xFF171513) else Color(0xFFF7F2EA)
    val card = if (dark) Color(0xFF25211E) else Color(0xFFFFFDF9)
    val ink = if (dark) Color(0xFFF2ECE5) else Color(0xFF302C2A)
    val muted = if (dark) Color(0xFFAAA09A) else Color(0xFF8E8580)

    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(
            background = bg, surface = card, onBackground = ink, onSurface = ink
        ) else lightColorScheme(
            background = bg, surface = card, onBackground = ink, onSurface = ink
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = bg) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Kanji Cozy", fontSize = 27.sp, fontWeight = FontWeight.SemiBold, color = ink)
                        Text("A little kanji, every now and then.", color = muted, fontSize = 13.sp)
                    }
                    Switch(checked = dark, onCheckedChange = { dark = it })
                }

                Spacer(Modifier.height(24.dp))

                Text("Your widget", color = muted, fontSize = 13.sp, modifier = Modifier.align(Alignment.Start))
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().height(210.dp)
                        .clip(RoundedCornerShape(30.dp)).background(card)
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(kanji.character, fontSize = 62.sp, color = ink)
                    if (reading) Text(kanji.reading, fontSize = 17.sp, color = muted)
                    if (translation) Text(kanji.meaning, fontSize = 13.sp, color = muted.copy(alpha = .65f))
                }

                Spacer(Modifier.height(24.dp))

                Text("Change every ${interval.toInt()} min", color = ink, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Slider(
                    value = interval,
                    onValueChange = { interval = it },
                    onValueChangeFinished = {
                        KanjiStore.setInterval(context, interval.toLong())
                        KanjiWidgetProvider.schedule(context)
                    },
                    valueRange = 5f..1440f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hiragana", color = ink)
                    Switch(checked = reading, onCheckedChange = {
                        reading = it
                        KanjiStore.setShowReading(context, it)
                        KanjiWidgetProvider.updateAll(context)
                    })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Translation", color = ink)
                    Switch(checked = translation, onCheckedChange = {
                        translation = it
                        KanjiStore.setShowTranslation(context, it)
                        KanjiWidgetProvider.updateAll(context)
                    })
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        kanji = KanjiStore.advance(context)
                        KanjiWidgetProvider.updateAll(context)
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("Next kanji")
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Add “Kanji Cozy” from your launcher’s widget picker.",
                    color = muted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
