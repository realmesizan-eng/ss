package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.SpeechEvalState
import com.example.data.EnglishBundle
import com.example.data.EnglishTopic
import com.example.data.PhraseItem
import com.example.data.local.UserProgressState
import com.example.ui.components.PathshalaTopBar
import com.example.ui.components.PremiumLockBanner
import com.example.ui.components.SpeakButton
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnglishScreen(
    bundle: EnglishBundle,
    phrases: List<PhraseItem>,
    progress: UserProgressState,
    speechEval: SpeechEvalState,
    onSpeakEnglish: (String) -> Unit,
    onSpeakBangla: (String) -> Unit,
    onStartEnglishListening: (String) -> Unit,
    onSaveWord: (String, String, String) -> Unit,
    onOpenTracing: () -> Unit,
    onOpenPremium: () -> Unit,
    onBack: () -> Unit
) {
    val tabs = listOf("Alphabet A-Z", "বিষয়ভিত্তিক শব্দ", "১০০টি বাক্য", "সহজ ব্যাকরণ", "English পড়া")
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeTopic by remember { mutableStateOf<EnglishTopic?>(null) }

    val context = LocalContext.current
    var pendingSpeechTarget by remember { mutableStateOf("") }
    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingSpeechTarget.isNotBlank()) {
            onStartEnglishListening(pendingSpeechTarget)
        }
    }

    Scaffold(
        topBar = {
            PathshalaTopBar(
                title = activeTopic?.titleBn ?: "English শিখি",
                onBack = {
                    if (activeTopic != null) activeTopic = null else onBack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (activeTopic != null) {
                val topic = activeTopic!!
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(topic.words) { word ->
                        Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${word.emoji} ${word.en}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("অর্থ: ${word.bn} • উচ্চারণ: ${word.pronBn}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SpeakButton(label = "EN", onClick = { onSpeakEnglish(word.en) })
                                    Spacer(Modifier.width(6.dp))
                                    SpeakButton(
                                        label = "বাং",
                                        onClick = { onSpeakBangla(word.bn) },
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    IconButton(onClick = { onSaveWord(word.en, word.bn, word.pronBn) }) {
                                        Icon(Icons.Default.BookmarkAdd, contentDescription = "সেভ")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
                    tabs.forEachIndexed { idx, label ->
                        Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(label) })
                    }
                }

                when (selectedTab) {
                    0 -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Button(
                                    onClick = onOpenTracing,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("হাতে খড়িতে A-Z ও a-z লেখা অনুশীলন করুন")
                                }
                            }
                            items(bundle.phonics) { p ->
                                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Surface(
                                                shape = RoundedCornerShape(14.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.padding(end = 12.dp)
                                            ) {
                                                Text(
                                                    text = "${p.upper} ${p.lower}",
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                                )
                                            }
                                            Column {
                                                Text("${p.emoji} ${p.upper} for ${p.word}", fontWeight = FontWeight.Bold)
                                                Text("উচ্চারণ: ${p.pronBn} • অর্থ: ${p.bnMeaning}", style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                        SpeakButton(
                                            label = "শুনুন",
                                            onClick = { onSpeakEnglish("${p.upper} for ${p.word}") }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!progress.isPremium) {
                                item {
                                    PremiumLockBanner(
                                        feeTaka = progress.licenseFeeTaka,
                                        onUnlockClick = onOpenPremium
                                    )
                                }
                            }
                            items(bundle.topics) { topic ->
                                val locked = topic.isPremium && !progress.isPremium
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (locked) onOpenPremium() else activeTopic = topic
                                        },
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(topic.emoji, fontSize = 28.sp)
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(topic.titleBn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                                Text("${topic.words.size}টি শব্দ", style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                        if (locked) {
                                            Icon(Icons.Default.Lock, contentDescription = "প্রিমিয়াম", tint = MaterialTheme.colorScheme.tertiary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (!progress.isPremium) {
                                item {
                                    PremiumLockBanner(
                                        feeTaka = progress.licenseFeeTaka,
                                        onUnlockClick = onOpenPremium
                                    )
                                }
                            }
                            val visiblePhrases = if (progress.isPremium) phrases else phrases.take(15)
                            items(visiblePhrases) { ph ->
                                Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ph.en, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text("উচ্চারণ: ${ph.pronBn}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                            Text("অর্থ: ${ph.bn}", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        SpeakButton(label = "EN", onClick = { onSpeakEnglish(ph.en) })
                                    }
                                }
                            }
                            if (!progress.isPremium && phrases.size > 15) {
                                item {
                                    Text(
                                        text = "আরও ${phrases.size - 15}টি দৈনন্দিন বাক্য দেখতে প্রিমিয়াম আনলক করুন।",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.clickable { onOpenPremium() }
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bundle.grammar) { g ->
                                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(g.titleBn, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(g.ruleBn, style = MaterialTheme.typography.bodyLarge)
                                        g.examples.forEach { ex ->
                                            Surface(
                                                onClick = { onSpeakEnglish(ex.en) },
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text("• ${ex.en}", fontWeight = FontWeight.Bold)
                                                    Text("  ${ex.bn} (${ex.pronBn})", style = MaterialTheme.typography.bodyMedium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bundle.reading) { item ->
                                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(item.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(item.text, style = MaterialTheme.typography.bodyLarge)
                                        Text("বাংলা অনুবাদ: ${item.bnTranslation}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            SpeakButton(label = "শুনুন (EN)", onClick = { onSpeakEnglish(item.text) })
                                            Button(
                                                onClick = {
                                                    val granted = ContextCompat.checkSelfPermission(
                                                        context,
                                                        Manifest.permission.RECORD_AUDIO
                                                    ) == PackageManager.PERMISSION_GRANTED
                                                    if (granted) {
                                                        onStartEnglishListening(item.text)
                                                    } else {
                                                        pendingSpeechTarget = item.text
                                                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (speechEval.isListening) ErrorRed else MaterialTheme.colorScheme.secondary
                                                )
                                            ) {
                                                Icon(Icons.Default.Mic, contentDescription = "নিজে পড়ো")
                                                Spacer(Modifier.width(4.dp))
                                                Text("নিজে পড়ো")
                                            }
                                        }
                                        if (speechEval.wordResults.isNotEmpty()) {
                                            Text("স্কোর: ${speechEval.scorePercent}%", fontWeight = FontWeight.Bold)
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                speechEval.wordResults.forEach { wr ->
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (wr.isMatched) SuccessGreen else ErrorRed
                                                    ) {
                                                        Text(
                                                            text = wr.expected,
                                                            color = Color.White,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
