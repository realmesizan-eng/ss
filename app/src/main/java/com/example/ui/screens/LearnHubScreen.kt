package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.UserProgressState
import com.example.ui.theme.CardCobalt
import com.example.ui.theme.CardEmerald
import com.example.ui.theme.CardIndigo
import com.example.ui.theme.CardMarigold
import com.example.ui.theme.CardPlum
import com.example.ui.theme.CardRose
import com.example.ui.theme.CardTeal
import com.example.ui.theme.CardTerracotta

data class LearnModuleCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

object LearnRoutes {
    const val ALPHABET = "alphabet"
    const val TRACING = "tracing"
    const val READING = "reading"
    const val ENGLISH = "english"
    const val VOCAB = "vocab"
    const val QUIZ = "quiz"
    const val MY_WORDS = "my_words"
    const val TABLES = "tables"
    const val PREMIUM = "premium"
    const val ADMIN = "admin"
}

@Composable
fun OnboardingOverlay(
    onComplete: (name: String, goal: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("দুটোই") }
    val goals = listOf("বাংলা", "English", "দুটোই")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_learn_hero),
                    contentDescription = "শিখি স্বাগতম ছবি",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Text(
                    text = "শিখি-তে স্বাগতম!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "ধাপে ধাপে বাংলা ও ইংরেজি অক্ষর, লেখা, পড়া ও শব্দ শিখুন। আপনার সব অগ্রগতি আপনার ফোনেই সুরক্ষিত থাকবে।",
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("তোমার / আপনার নাম") },
                    placeholder = { Text("যেমন: রাফি বা তানিয়া") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input")
                )
                Text(
                    text = "আমি শিখতে চাই:",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    goals.forEach { option ->
                        FilterChip(
                            selected = goal == option,
                            onClick = { goal = option },
                            label = { Text(option) }
                        )
                    }
                }
                Button(
                    onClick = { onComplete(name, goal) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_start_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "শেখা শুরু করি",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun LearnHubScreen(
    progress: UserProgressState,
    onNavigate: (String) -> Unit
) {
    val modules = remember {
        listOf(
            LearnModuleCard("alphabet", "বর্ণমালা", "স্বর, ব্যঞ্জন, কার ও যুক্তবর্ণ", Icons.Default.Abc, CardEmerald, LearnRoutes.ALPHABET),
            LearnModuleCard("tracing", "হাতে খড়ি", "আঙুলে লিখে অক্ষর শেখা", Icons.Default.Draw, CardTerracotta, LearnRoutes.TRACING),
            LearnModuleCard("reading", "পড়া শিখি", "৫টি স্তর ও ১০টি গল্প", Icons.Default.AutoStories, CardCobalt, LearnRoutes.READING),
            LearnModuleCard("english", "English শিখি", "A-Z, শব্দ, বাক্য ও ব্যাকরণ", Icons.Default.Language, CardPlum, LearnRoutes.ENGLISH),
            LearnModuleCard("vocab", "শব্দভাণ্ডার", "৫০০+ অফলাইন ও এআই অভিধান", Icons.Default.MenuBook, CardTeal, LearnRoutes.VOCAB),
            LearnModuleCard("quiz", "কুইজ ও গেম", "মিলাও, বানান ও ৬০ সে. খেলা", Icons.Default.Extension, CardMarigold, LearnRoutes.QUIZ),
            LearnModuleCard("my_words", "আমার শব্দ", "সংরক্ষিত শব্দ ও ফ্ল্যাশকার্ড", Icons.Default.BookmarkAdded, CardRose, LearnRoutes.MY_WORDS),
            LearnModuleCard("tables", "নামতা ও সংখ্যা", "১-২০ নামতা ও বাংলা-ইংরেজি", Icons.Default.Calculate, CardIndigo, LearnRoutes.TABLES)
        )
    }

    val goalRatio = (progress.todayActivities.toFloat() / progress.dailyGoalTarget.coerceAtLeast(1)).coerceIn(0f, 1f)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (progress.userName.isNotBlank()) "হ্যালো, ${progress.userName}!" else "শিখি",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "লক্ষ্য: ${progress.learningGoal} শিক্ষা",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    onClick = { onNavigate(LearnRoutes.PREMIUM) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (progress.isPremium) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.testTag("hub_premium_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "প্রিমিয়াম",
                            tint = if (progress.isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (progress.isPremium) "প্রিমিয়াম চালু" else "প্রিমিয়াম",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // "আজকের লক্ষ্য" Card + Hero Banner
        item(span = { GridItemSpan(2) }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_learn_hero),
                            contentDescription = "আজকের শিক্ষার ব্যানার",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                                    )
                                )
                        )
                        Text(
                            text = "আজকের লক্ষ্য",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            GoalMetricPill(
                                icon = Icons.Default.LocalFireDepartment,
                                value = "${progress.streakDays} দিন",
                                label = "ধারাবাহিক স্ট্রিক",
                                tint = Color(0xFFE05D27)
                            )
                            GoalMetricPill(
                                icon = Icons.Default.Star,
                                value = "${progress.totalStars} ★",
                                label = "মোট তারকা",
                                tint = Color(0xFFD98B14)
                            )
                            GoalMetricPill(
                                icon = Icons.Default.AutoStories,
                                value = "${progress.todayActivities}/${progress.dailyGoalTarget}",
                                label = "আজকের পাঠ",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { goalRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                }
            }
        }

        items(modules, key = { it.id }) { card ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp)
                    .clickable { onNavigate(card.route) }
                    .testTag("hub_card_${card.id}"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = card.color),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = card.icon,
                            contentDescription = card.title,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = card.subtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalMetricPill(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
