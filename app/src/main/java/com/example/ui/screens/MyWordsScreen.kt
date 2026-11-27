package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.SavedWordEntity
import com.example.ui.components.PathshalaTopBar
import com.example.ui.components.SpeakButton
import com.example.ui.theme.SuccessGreen

@Composable
fun MyWordsScreen(
    words: List<SavedWordEntity>,
    onSpeakBangla: (String) -> Unit,
    onSpeakEnglish: (String) -> Unit,
    onAddWord: (word: String, meaningBn: String, pronBn: String, example: String) -> Unit,
    onToggleMastered: (SavedWordEntity) -> Unit,
    onDeleteWord: (Int) -> Unit,
    onBack: () -> Unit
) {
    var modeTab by remember { mutableIntStateOf(0) } // 0 = তালিকা, 1 = ফ্ল্যাশকার্ড
    var filterTab by remember { mutableIntStateOf(0) } // 0 = সব, 1 = বাকি, 2 = শেখা হয়েছে
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredWords = remember(words, filterTab) {
        when (filterTab) {
            1 -> words.filter { !it.isMastered }
            2 -> words.filter { it.isMastered }
            else -> words
        }
    }

    Scaffold(
        topBar = {
            PathshalaTopBar(title = "আমার শব্দ (${words.size})", onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("add_my_word_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন শব্দ যোগ করুন")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = modeTab) {
                Tab(
                    selected = modeTab == 0,
                    onClick = { modeTab = 0 },
                    text = { Text("শব্দের তালিকা") },
                    modifier = Modifier.testTag("my_words_list_tab")
                )
                Tab(
                    selected = modeTab == 1,
                    onClick = { modeTab = 1 },
                    text = { Text("ফ্ল্যাশকার্ড") },
                    modifier = Modifier.testTag("my_words_flashcard_tab")
                )
            }

            if (words.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(shape = RoundedCornerShape(24.dp)) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = "আমার শব্দ খালি",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "এখনো কোনো শব্দ সংরক্ষণ করা হয়নি",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "বর্ণমালা, পড়া শিখি, অভিধান বা লাইভ ক্যামেরা থেকে 'আমার শব্দে যোগ করুন' চাপুন অথবা নিচের '+' বোতামে নতুন শব্দ লিখুন।",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = { showAddDialog = true }) {
                                Text("প্রথম শব্দ যোগ করুন")
                            }
                        }
                    }
                }
            } else if (modeTab == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("সব (${words.size})", "বাকি", "শেখা হয়েছে").forEachIndexed { idx, label ->
                        FilterChip(
                            selected = filterTab == idx,
                            onClick = { filterTab = idx },
                            label = { Text(label) }
                        )
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredWords, key = { it.id }) { item ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isMastered) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.word,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (item.meaningBn.isNotBlank()) {
                                            Text(
                                                text = "অর্থ: ${item.meaningBn}",
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        if (item.pronunciationBn.isNotBlank()) {
                                            Text(
                                                text = "উচ্চারণ: ${item.pronunciationBn}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onToggleMastered(item) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(
                                                imageVector = if (item.isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = if (item.isMastered) "শেখা সম্পন্ন" else "শেখা বাকি",
                                                tint = if (item.isMastered) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteWord(item.id) },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "মুছে ফেলুন",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                if (item.example.isNotBlank()) {
                                    Text(
                                        text = "উদাহরণ: ${item.example}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val isAscii = item.word.any { it in 'A'..'Z' || it in 'a'..'z' }
                                    SpeakButton(
                                        label = if (isAscii) "EN শুনুন" else "বাংলা শুনুন",
                                        onClick = {
                                            if (isAscii) onSpeakEnglish(item.word) else onSpeakBangla(item.word)
                                        }
                                    )
                                    if (item.meaningBn.isNotBlank() && isAscii) {
                                        SpeakButton(
                                            label = "অর্থ শুনুন",
                                            onClick = { onSpeakBangla(item.meaningBn) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                FlashcardDeckView(
                    words = words,
                    onSpeakBangla = onSpeakBangla,
                    onSpeakEnglish = onSpeakEnglish,
                    onToggleMastered = onToggleMastered
                )
            }
        }
    }

    if (showAddDialog) {
        AddWordDialog(
            onDismiss = { showAddDialog = false },
            onSave = { word, meaning, pron, example ->
                onAddWord(word, meaning, pron, example)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FlashcardDeckView(
    words: List<SavedWordEntity>,
    onSpeakBangla: (String) -> Unit,
    onSpeakEnglish: (String) -> Unit,
    onToggleMastered: (SavedWordEntity) -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    val safeIndex = index.coerceIn(0, words.lastIndex)
    val current = words[safeIndex]
    val rotation by animateFloatAsState(targetValue = if (flipped) 180f else 0f, label = "card_flip")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "কার্ড ${safeIndex + 1} / ${words.size}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (flipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
                .clickable { flipped = !flipped }
                .testTag("flashcard_surface")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = current.word,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (current.pronunciationBn.isNotBlank()) {
                            Text(
                                text = "[${current.pronunciationBn}]",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("অর্থ দেখতে কার্ডে চাপুন", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = current.meaningBn.ifBlank { current.word },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (current.example.isNotBlank()) {
                            Text(
                                text = current.example,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val isAscii = current.word.any { it in 'A'..'Z' || it in 'a'..'z' }
                SpeakButton(
                    label = "শব্দ শুনুন",
                    onClick = { if (isAscii) onSpeakEnglish(current.word) else onSpeakBangla(current.word) }
                )
                OutlinedButton(
                    onClick = { onToggleMastered(current) },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(if (current.isMastered) "✓ শেখা হয়েছে" else "শেখা সম্পন্ন চিহ্ন")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        flipped = false
                        index = if (safeIndex > 0) safeIndex - 1 else words.lastIndex
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("আগের")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        flipped = false
                        index = (safeIndex + 1) % words.size
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("পরের")
                }
            }
        }
    }
}

@Composable
private fun AddWordDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var pron by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন শব্দ যোগ করুন") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text("শব্দ (বাংলা বা English)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = meaning,
                    onValueChange = { meaning = it },
                    label = { Text("বাংলা অর্থ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pron,
                    onValueChange = { pron = it },
                    label = { Text("বাংলা উচ্চারণ (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = example,
                    onValueChange = { example = it },
                    label = { Text("উদাহরণ বাক্য (ঐচ্ছিক)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(word, meaning, pron, example) },
                enabled = word.isNotBlank()
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
