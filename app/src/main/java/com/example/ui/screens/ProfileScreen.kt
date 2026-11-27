package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.LetterProgressEntity
import com.example.data.local.SavedWordEntity
import com.example.data.local.UserProgressState
import com.example.ui.components.PathshalaTopBar
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    progress: UserProgressState,
    letterProgress: List<LetterProgressEntity>,
    savedWords: List<SavedWordEntity>,
    onUpdateReaderPrefs: (darkMode: Boolean, fontSizeSp: Int) -> Unit,
    onOpenPremium: () -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val tracedCount = letterProgress.count { it.bestStars > 0 }
    val readingPercent = ((progress.unlockedReadingLevel / 5f) * 100f).roundToInt().coerceIn(0, 100)
    val weakLetters = remember(letterProgress) {
        letterProgress
            .filter { it.mistakes > 0 || it.bestStars <= 1 }
            .sortedByDescending { it.mistakes }
            .take(8)
    }

    Scaffold(
        topBar = { PathshalaTopBar(title = "প্রোফাইল ও অগ্রগতি") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // User Header
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = progress.userName.ifBlank { "শিক্ষার্থী" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "লক্ষ্য: ${progress.learningGoal} | স্ট্রিক: ${progress.streakDays} দিন | তারকা: ${progress.totalStars} ★",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (progress.isPremium) {
                            "স্ট্যাটাস: প্রিমিয়াম সক্রিয় (${progress.activeLicenseKey})"
                        } else {
                            "স্ট্যাটাস: ফ্রি সংস্করণ"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Progress Summary
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("শেখার পরিসংখ্যান", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    ProgressBarItem(label = "হাতে খড়ি সম্পন্ন বর্ণ ($tracedCount / ৫০)", ratio = (tracedCount / 50f).coerceIn(0f, 1f))
                    ProgressBarItem(label = "পড়ার স্তর (${progress.unlockedReadingLevel} / ৫)", ratio = readingPercent / 100f)
                    Text("সংরক্ষিত শব্দ: ${savedWords.size}টি (শেখা হয়েছে ${savedWords.count { it.isMastered }}টি)")
                    Text("সম্পন্ন পাঠ: ${progress.completedLessonIds.size}টি")
                }
            }

            // Weak Letters
            if (weakLetters.isNotEmpty()) {
                Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("অনুশীলন প্রয়োজন এমন অক্ষর", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            weakLetters.forEach { lp ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text("${lp.letterChar} (${lp.bestAccuracy}%)") }
                                )
                            }
                        }
                    }
                }
            }

            // Badges
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Text("অর্জিত ব্যাজ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    if (progress.earnedBadges.isEmpty()) {
                        Text("প্রথম পাঠ বা কুইজ শেষ করলে ব্যাজ আনলক হবে।", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            progress.earnedBadges.forEach { badge ->
                                AssistChip(onClick = {}, label = { Text("🏅 $badge") })
                            }
                        }
                    }
                }
            }

            // Appearance & Reader Settings
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("পড়ার সেটিংস ও থিম", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ডার্ক মোড (রাতের পড়া)")
                        Switch(
                            checked = progress.readerDarkMode,
                            onCheckedChange = { onUpdateReaderPrefs(it, progress.readerFontSizeSp) },
                            modifier = Modifier.testTag("profile_dark_mode_switch")
                        )
                    }
                    Text("পড়ার ফন্ট সাইজ: ${progress.readerFontSizeSp} sp")
                    Slider(
                        value = progress.readerFontSizeSp.toFloat(),
                        onValueChange = { onUpdateReaderPrefs(progress.readerDarkMode, it.roundToInt()) },
                        valueRange = 16f..34f
                    )
                }
            }

            // License & Admin Panel Actions
            Button(
                onClick = onOpenPremium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("profile_premium_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (progress.isPremium) "প্রিমিয়াম লাইসেন্স বিবরণ"
                    else "প্রিমিয়াম লাইসেন্স নিন (বিকাশ ${progress.licenseFeeTaka} টাকা)"
                )
            }

            OutlinedButton(
                onClick = onOpenAdminPanel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_admin_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("এডমিন পেনেল (লাইসেন্স ও সেটিংস)")
            }

            TextButton(
                onClick = { showPrivacyDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.PrivacyTip, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("শিশু-নিরাপদ গোপনীয়তা নীতি")
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("গোপনীয়তা ও নিরাপত্তা নীতি") },
            text = {
                Text(
                    "শিখি একটি শিশু-বান্ধব শিক্ষামূলক অ্যাপ। আপনার নাম, শেখার অগ্রগতি, সংরক্ষিত শব্দ এবং অনুশীলনের তথ্য শুধুমাত্র আপনার নিজের ডিভাইসেই সংরক্ষিত থাকে এবং কোনো ব্যক্তিগত তথ্য আমাদের সার্ভারে পাঠানো হয় না। লাইভ ক্যামেরা ও এআই ফিচার শুধুমাত্র আপনি ছবি বা প্রশ্ন পাঠালে ব্যবহার করা হয়।"
                )
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) { Text("বুঝেছি") }
            }
        )
    }
}

@Composable
private fun ProgressBarItem(label: String, ratio: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
    }
}
