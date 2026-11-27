package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.audio.SpeechEvalState
import com.example.audio.WordRangeHighlight
import com.example.data.ReadingLesson
import com.example.data.ReadingLevelData
import com.example.data.StoryItem
import com.example.data.local.UserProgressState
import com.example.ui.components.PathshalaTopBar
import com.example.ui.components.PremiumLockBanner
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadingScreen(
    levels: List<ReadingLevelData>,
    stories: List<StoryItem>,
    progress: UserProgressState,
    highlightState: WordRangeHighlight,
    speechEval: SpeechEvalState,
    onSpeakBangla: (text: String, slow: Boolean, trackHighlight: Boolean) -> Unit,
    onStopSpeak: () -> Unit,
    onStartListening: (expectedText: String) -> Unit,
    onClearSpeech: () -> Unit,
    onMarkLessonComplete: (level: Int, lessonId: String, totalInLevel: Int) -> Unit,
    onCompleteStory: (storyId: String) -> Unit,
    onSaveWord: (word: String, meaning: String, pron: String) -> Unit,
    onUpdateReaderPrefs: (darkMode: Boolean, fontSizeSp: Int) -> Unit,
    onOpenPremium: () -> Unit,
    onBack: () -> Unit
) {
    var topTab by remember { mutableIntStateOf(0) } // 0 = ৫টি স্তর, 1 = ১০টি গল্প
    var activeLesson by remember { mutableStateOf<Pair<ReadingLevelData, ReadingLesson>?>(null) }
    var activeStory by remember { mutableStateOf<StoryItem?>(null) }

    val readerBg = if (progress.readerDarkMode) Color(0xFF121816) else MaterialTheme.colorScheme.background
    val readerFg = if (progress.readerDarkMode) Color(0xFFE7EFEA) else MaterialTheme.colorScheme.onBackground

    Scaffold(
        topBar = {
            PathshalaTopBar(
                title = when {
                    activeLesson != null -> activeLesson!!.second.title
                    activeStory != null -> activeStory!!.title
                    else -> "পড়া শিখি (বাংলা)"
                },
                onBack = {
                    onStopSpeak()
                    onClearSpeech()
                    when {
                        activeLesson != null -> activeLesson = null
                        activeStory != null -> activeStory = null
                        else -> onBack()
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onUpdateReaderPrefs(!progress.readerDarkMode, progress.readerFontSizeSp)
                    }) {
                        Icon(Icons.Default.DarkMode, contentDescription = "অন্ধকার মোড")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(readerBg)
        ) {
            if (activeLesson != null) {
                val (levelData, lesson) = activeLesson!!
                ReadingLessonPlayer(
                    levelData = levelData,
                    lesson = lesson,
                    fontSizeSp = progress.readerFontSizeSp,
                    isDarkMode = progress.readerDarkMode,
                    textColor = readerFg,
                    highlightState = highlightState,
                    speechEval = speechEval,
                    onFontSizeChange = { onUpdateReaderPrefs(progress.readerDarkMode, it) },
                    onSpeakBangla = onSpeakBangla,
                    onStopSpeak = onStopSpeak,
                    onStartListening = onStartListening,
                    onSaveWord = onSaveWord,
                    onComplete = {
                        onMarkLessonComplete(levelData.level, lesson.id, levelData.lessons.size)
                        activeLesson = null
                    }
                )
            } else if (activeStory != null) {
                StoryReaderView(
                    story = activeStory!!,
                    fontSizeSp = progress.readerFontSizeSp,
                    isDarkMode = progress.readerDarkMode,
                    textColor = readerFg,
                    highlightState = highlightState,
                    onFontSizeChange = { onUpdateReaderPrefs(progress.readerDarkMode, it) },
                    onSpeakBangla = onSpeakBangla,
                    onStopSpeak = onStopSpeak,
                    onComplete = {
                        onCompleteStory(activeStory!!.id)
                        activeStory = null
                    }
                )
            } else {
                TabRow(selectedTabIndex = topTab) {
                    Tab(selected = topTab == 0, onClick = { topTab = 0 }, text = { Text("৫টি পড়ার স্তর") })
                    Tab(selected = topTab == 1, onClick = { topTab = 1 }, text = { Text("১০টি ছোট গল্প") })
                }

                if (topTab == 0) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!progress.isPremium) {
                            item {
                                PremiumLockBanner(
                                    feeTaka = progress.licenseFeeTaka,
                                    onUnlockClick = onOpenPremium
                                )
                            }
                        }
                        items(levels, key = { it.level }) { levelData ->
                            val prevCompleted = levelData.level <= progress.unlockedReadingLevel
                            val premiumLocked = levelData.isPremium && !progress.isPremium
                            val isLocked = !prevCompleted || premiumLocked

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isLocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = levelData.titleBn,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isLocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "তালাবদ্ধ",
                                                tint = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                    if (!prevCompleted) {
                                        Text(
                                            text = "আগের স্তর ${levelData.level - 1} শেষ করলে এই স্তরটি খুলবে।",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    } else if (premiumLocked) {
                                        Text(
                                            text = "প্রিমিয়াম স্তর — খুলতে চাপুন",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.clickable { onOpenPremium() }
                                        )
                                    } else {
                                        Spacer(Modifier.height(10.dp))
                                        levelData.lessons.forEach { lesson ->
                                            val done = progress.completedLessonIds.contains(lesson.id)
                                            Surface(
                                                onClick = { activeLesson = levelData to lesson },
                                                shape = RoundedCornerShape(14.dp),
                                                color = if (done) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .testTag("reading_lesson_${lesson.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(lesson.title, fontWeight = FontWeight.Bold)
                                                        Text(lesson.text, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                    if (done) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = "সম্পন্ন",
                                                            tint = SuccessGreen
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
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!progress.isPremium) {
                            item {
                                PremiumLockBanner(
                                    feeTaka = progress.licenseFeeTaka,
                                    onUnlockClick = onOpenPremium
                                )
                            }
                        }
                        items(stories.indices.toList()) { idx ->
                            val story = stories[idx]
                            val locked = idx >= 2 && !progress.isPremium
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (locked) onOpenPremium() else activeStory = story
                                    },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = storyDrawable(story.imageKey)),
                                        contentDescription = story.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(84.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${idx + 1}. ${story.title}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = story.lines.firstOrNull().orEmpty(),
                                            maxLines = 2,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    if (locked) {
                                        Icon(Icons.Default.Lock, contentDescription = "প্রিমিয়াম", tint = MaterialTheme.colorScheme.tertiary)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadingLessonPlayer(
    levelData: ReadingLevelData,
    lesson: ReadingLesson,
    fontSizeSp: Int,
    isDarkMode: Boolean,
    textColor: Color,
    highlightState: WordRangeHighlight,
    speechEval: SpeechEvalState,
    onFontSizeChange: (Int) -> Unit,
    onSpeakBangla: (String, Boolean, Boolean) -> Unit,
    onStopSpeak: () -> Unit,
    onStartListening: (String) -> Unit,
    onSaveWord: (String, String, String) -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var mode by remember { mutableIntStateOf(0) } // 0 = শুনে পড়ো, 1 = নিজে পড়ো, 2 = বানান মেলাও
    var slowMode by remember { mutableStateOf(false) }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onStartListening(lesson.text)
        }
    }

    val tokens = remember(lesson.text) {
        lesson.text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Font size slider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ফন্ট: ${fontSizeSp}sp", color = textColor, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.width(10.dp))
            Slider(
                value = fontSizeSp.toFloat(),
                onValueChange = { onFontSizeChange(it.toInt()) },
                valueRange = 18f..34f,
                modifier = Modifier.weight(1f)
            )
        }

        // Mode selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = mode == 0, onClick = { mode = 0 }, label = { Text("১) শুনে পড়ো") })
            FilterChip(selected = mode == 1, onClick = { mode = 1 }, label = { Text("২) নিজে পড়ো") })
            FilterChip(selected = mode == 2, onClick = { mode = 2 }, label = { Text("৩) বানান মেলাও") })
        }

        when (mode) {
            0 -> {
                // শুনে পড়ো with live word highlight
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1C2622) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tokens.forEachIndexed { idx, word ->
                            val isActive = highlightState.isSpeaking && highlightState.activeWordIndex == idx
                            Text(
                                text = word,
                                fontSize = fontSizeSp.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isActive) Color.White else textColor,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        onSpeakBangla(word, slowMode, false)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = slowMode,
                        onClick = { slowMode = !slowMode },
                        label = { Text(if (slowMode) "গতি: ধীর" else "গতি: স্বাভাবিক") }
                    )
                    Button(
                        onClick = {
                            if (highlightState.isSpeaking) onStopSpeak()
                            else onSpeakBangla(lesson.text, slowMode, true)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            if (highlightState.isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "পড়ুন"
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (highlightState.isSpeaking) "থামান" else "শুনুন")
                    }
                }

                if (lesson.words.isNotEmpty()) {
                    Text("শব্দের অর্থ (চাপলে সেভ হবে):", color = textColor, fontWeight = FontWeight.Bold)
                    lesson.words.forEach { w ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${w.emoji} ${w.word} — ${w.meaning}", style = MaterialTheme.typography.titleMedium)
                                IconButton(onClick = { onSaveWord(w.word, w.meaning, w.word) }) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = "আমার শব্দে যোগ করুন")
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // নিজে পড়ো (SpeechRecognizer bn-BD)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1C2622) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = lesson.text,
                            fontSize = fontSizeSp.sp,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "মাইকে চেপে উপরের লেখাটি জোরে পড়ুন। সঠিক শব্দ সবুজ এবং ভুল বা বাদ পড়া শব্দ লাল দেখাবে।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.8f)
                        )
                        Button(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    onStartListening(lesson.text)
                                } else {
                                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (speechEval.isListening) ErrorRed else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("reading_mic_button")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "মাইক")
                            Spacer(Modifier.width(8.dp))
                            Text(if (speechEval.isListening) "শুনছি... বলুন" else "মাইকে পড়ে শোনান (bn-BD)")
                        }

                        if (speechEval.errorMessage != null) {
                            Text(speechEval.errorMessage, color = ErrorRed)
                        }
                        if (speechEval.wordResults.isNotEmpty()) {
                            Text(
                                text = "স্কোর: ${speechEval.scorePercent}% (আপনি বলেছেন: ${speechEval.recognizedText})",
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                speechEval.wordResults.forEach { wr ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (wr.isMatched) SuccessGreen else ErrorRed
                                    ) {
                                        Text(
                                            text = wr.expected,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // বানান মেলাও
                SpellingMatchSubQuiz(
                    lesson = lesson,
                    onSpeakBangla = { onSpeakBangla(it, false, false) }
                )
            }
        }

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("reading_complete_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = "পাঠ শেষ")
            Spacer(Modifier.width(8.dp))
            Text("পাঠ শেষ করেছি (+৩ ★)")
        }
    }
}

@Composable
private fun SpellingMatchSubQuiz(
    lesson: ReadingLesson,
    onSpeakBangla: (String) -> Unit
) {
    val words = lesson.words
    if (words.isEmpty()) {
        Text("এই পাঠে বানান অনুশীলন নেই।")
        return
    }
    var qIndex by remember(lesson.id) { mutableIntStateOf(0) }
    val target = words[qIndex % words.size]
    val distractors = remember(qIndex, lesson.id) {
        val pool = (words.map { it.word } + listOf("কলম", "পাখি", "নদী", "বই", "ফুল", "চাঁদ"))
            .distinct()
            .filter { it != target.word }
            .shuffled()
            .take(3)
        (pool + target.word).shuffled()
    }
    var picked by remember(qIndex, lesson.id) { mutableStateOf<String?>(null) }

    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("ছবি ও ভয়েস শুনে সঠিক শব্দ বাছাই করুন:", fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${target.emoji} (${target.meaning})", fontSize = 26.sp)
                OutlinedButton(onClick = { onSpeakBangla(target.word) }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "শুনুন")
                    Spacer(Modifier.width(6.dp))
                    Text("শুনুন")
                }
            }
            distractors.forEach { option ->
                val isCorrect = option == target.word
                val bg = when {
                    picked == null -> MaterialTheme.colorScheme.surfaceVariant
                    picked == option && isCorrect -> SuccessGreen
                    picked == option && !isCorrect -> ErrorRed
                    isCorrect -> SuccessGreen.copy(alpha = 0.25f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Surface(
                    onClick = { picked = option },
                    shape = RoundedCornerShape(14.dp),
                    color = bg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = option,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (picked == option) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
            if (picked != null) {
                Button(
                    onClick = {
                        qIndex = (qIndex + 1) % words.size
                        picked = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("পরের শব্দ")
                }
            }
        }
    }
}

@Composable
private fun StoryReaderView(
    story: StoryItem,
    fontSizeSp: Int,
    isDarkMode: Boolean,
    textColor: Color,
    highlightState: WordRangeHighlight,
    onFontSizeChange: (Int) -> Unit,
    onSpeakBangla: (String, Boolean, Boolean) -> Unit,
    onStopSpeak: () -> Unit,
    onComplete: () -> Unit
) {
    var slow by remember { mutableStateOf(false) }
    val fullText = remember(story) { story.lines.joinToString(" ") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Image(
            painter = painterResource(id = storyDrawable(story.imageKey)),
            contentDescription = story.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(22.dp))
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ফন্ট: ${fontSizeSp}sp", color = textColor)
            Spacer(Modifier.width(10.dp))
            Slider(
                value = fontSizeSp.toFloat(),
                onValueChange = { onFontSizeChange(it.toInt()) },
                valueRange = 18f..34f,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = slow, onClick = { slow = !slow }, label = { Text(if (slow) "ধীর গতি" else "স্বাভাবিক গতি") })
            Button(
                onClick = {
                    if (highlightState.isSpeaking) onStopSpeak()
                    else onSpeakBangla(fullText, slow, false)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (highlightState.isSpeaking) "থামান" else "পুরো গল্প শুনুন")
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1C2622) else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                story.lines.forEach { line ->
                    Text(
                        text = line,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp + 10).sp,
                        color = textColor,
                        modifier = Modifier.clickable { onSpeakBangla(line, slow, false) }
                    )
                }
                if (story.moral.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "নীতি কথা: ${story.moral}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("গল্প পড়া শেষ (+৩ ★)")
        }
    }
}

private fun storyDrawable(key: String): Int = when (key) {
    "img_story_animals" -> R.drawable.img_story_animals
    "img_story_school" -> R.drawable.img_story_school
    else -> R.drawable.img_story_nature
}
