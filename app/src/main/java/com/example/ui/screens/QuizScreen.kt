package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LettersBundle
import com.example.data.local.UserProgressState
import com.example.ui.components.PathshalaTopBar
import com.example.ui.components.PremiumLockBanner
import com.example.ui.components.SpeakButton
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

enum class QuizMode(val titleBn: String, val descBn: String, val icon: ImageVector) {
    MATCH("মিলাও", "অক্ষর বা শব্দের সাথে ছবি মেলানো", Icons.Default.Extension),
    LISTEN("শুনে বাছাই", "ভয়েস শুনে সঠিক বর্ণ/শব্দ বাছাই", Icons.Default.Hearing),
    SPELLING("বানান তৈরি", "এলোমেলো অক্ষর সাজিয়ে শব্দ বানানো", Icons.Default.Spellcheck),
    SPEED("দ্রুত উত্তর (৬০ সে.)", "৬০ সেকেন্ডে যত বেশি সঠিক উত্তর", Icons.Default.Bolt)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(
    bundle: LettersBundle,
    progress: UserProgressState,
    onSpeakBangla: (String) -> Unit,
    onPlayChime: (Boolean) -> Unit,
    onQuizFinished: (Int) -> Unit,
    onOpenPremium: () -> Unit,
    onBack: () -> Unit
) {
    var activeMode by remember { mutableStateOf<QuizMode?>(null) }
    val freeLimitReached = !progress.isPremium && progress.dailyQuizCount >= 3

    Scaffold(
        topBar = {
            PathshalaTopBar(
                title = activeMode?.titleBn ?: "কুইজ ও গেম",
                onBack = { if (activeMode != null) activeMode = null else onBack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeMode == null) {
                if (!progress.isPremium) {
                    Text(
                        text = "আজকের ফ্রি কুইজ: ${progress.dailyQuizCount}/৩টি সম্পন্ন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (freeLimitReached) {
                    PremiumLockBanner(
                        feeTaka = progress.licenseFeeTaka,
                        onUnlockClick = onOpenPremium
                    )
                }
                QuizMode.entries.forEach { mode ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (freeLimitReached) onOpenPremium() else activeMode = mode
                            },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(mode.icon, contentDescription = mode.titleBn, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mode.titleBn, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(mode.descBn, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            } else {
                QuizGameplayView(
                    mode = activeMode!!,
                    bundle = bundle,
                    onSpeakBangla = onSpeakBangla,
                    onPlayChime = onPlayChime,
                    onComplete = { score ->
                        onQuizFinished(score)
                        activeMode = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuizGameplayView(
    mode: QuizMode,
    bundle: LettersBundle,
    onSpeakBangla: (String) -> Unit,
    onPlayChime: (Boolean) -> Unit,
    onComplete: (Int) -> Unit
) {
    val allLetters = remember(bundle) { (bundle.vowels + bundle.consonants).ifEmpty { emptyList() } }
    if (allLetters.size < 4) {
        Text("কুইজ ডেটা লোড হচ্ছে...")
        return
    }

    var questionIdx by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(60) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    val totalQuestions = 10

    if (mode == QuizMode.SPEED && !finished) {
        LaunchedEffect(Unit) {
            while (secondsLeft > 0) {
                delay(1000L)
                secondsLeft--
            }
            finished = true
        }
    }

    if (finished) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "বিজয়ী", tint = Color(0xFFD98B14))
                Text("কুইজ শেষ!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("আপনার স্কোর: $score / $totalQuestions", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { onComplete(score) }, modifier = Modifier.fillMaxWidth()) {
                    Text("তারকা সংগ্রহ করে ফিরে যান")
                }
            }
        }
        return
    }

    val questions = remember(mode, allLetters) {
        allLetters.shuffled().take(totalQuestions)
    }
    val current = questions[questionIdx]
    val options = remember(questionIdx, questions) {
        val distractors = allLetters.filter { it.char != current.char }.shuffled().take(3)
        (distractors + current).shuffled()
    }

    if (mode == QuizMode.SPELLING) {
        val spellingWords = remember {
            listOf("কলম", "নদী", "আম", "পাখি", "ফুল", "বই", "জল", "ঘর", "মাছ", "চাঁদ")
        }
        val targetWord = spellingWords[questionIdx % spellingWords.size]
        val tiles = remember(questionIdx) {
            targetWord.toCharArray().map { it.toString() }.shuffled()
        }
        var built by remember(questionIdx) { mutableStateOf("") }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("প্রশ্ন ${questionIdx + 1} / $totalQuestions • স্কোর: $score", fontWeight = FontWeight.Bold)
            SpeakButton(label = "শব্দটি শুনুন", onClick = { onSpeakBangla(targetWord) })
            Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = built.ifBlank { "অক্ষরগুলোতে চাপ দিয়ে শব্দ বানান" },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(20.dp)
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tiles.forEach { ch ->
                    OutlinedButton(onClick = { built += ch }) {
                        Text(ch, fontSize = 24.sp)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { built = "" }) { Text("মুছুন") }
                Button(
                    onClick = {
                        val correct = built == targetWord
                        onPlayChime(correct)
                        if (correct) score++
                        if (questionIdx + 1 >= totalQuestions) finished = true
                        else questionIdx++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("জমা দিন")
                }
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("প্রশ্ন ${questionIdx + 1} / $totalQuestions", fontWeight = FontWeight.Bold)
            Text(
                text = if (mode == QuizMode.SPEED) "সময়: ${secondsLeft}সে. • স্কোর: $score" else "স্কোর: $score",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (mode) {
                    QuizMode.MATCH -> {
                        Text("${current.emoji} ${current.wordBn}", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text("কোন বর্ণ দিয়ে এই শব্দটি শুরু হয়?")
                    }
                    QuizMode.LISTEN -> {
                        Text("ভয়েস শুনে সঠিক বর্ণ বাছাই করুন", style = MaterialTheme.typography.titleMedium)
                        SpeakButton(
                            label = "বর্ণ ও শব্দ শুনুন",
                            onClick = { onSpeakBangla("${current.char} তে ${current.wordBn}") }
                        )
                    }
                    else -> {
                        Text("${current.emoji} '${current.wordBn}' — প্রথম বর্ণ কী?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        options.forEach { opt ->
            val isCorrect = opt.char == current.char
            val bg = when {
                selectedAnswer == null -> MaterialTheme.colorScheme.surface
                selectedAnswer == opt.char && isCorrect -> SuccessGreen
                selectedAnswer == opt.char && !isCorrect -> ErrorRed
                isCorrect -> SuccessGreen.copy(alpha = 0.25f)
                else -> MaterialTheme.colorScheme.surface
            }
            Surface(
                onClick = {
                    if (selectedAnswer == null) {
                        selectedAnswer = opt.char
                        onPlayChime(isCorrect)
                        if (isCorrect) score++
                    }
                },
                shape = RoundedCornerShape(16.dp),
                color = bg,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${opt.char}  (${opt.pronEn})",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedAnswer == opt.char) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (selectedAnswer != null) {
            Button(
                onClick = {
                    selectedAnswer = null
                    if (questionIdx + 1 >= totalQuestions) finished = true
                    else questionIdx++
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (questionIdx + 1 >= totalQuestions) "ফলাফল দেখুন" else "পরের প্রশ্ন")
            }
        }
    }
}
