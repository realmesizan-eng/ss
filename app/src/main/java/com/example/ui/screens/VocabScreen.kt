package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.data.DictionaryEntry
import com.example.data.local.UserProgressState
import com.example.ui.DefineAiState
import com.example.ui.components.PathshalaTopBar
import com.example.ui.components.SpeakButton
import java.util.Calendar

@Composable
fun VocabScreen(
    dictionary: List<DictionaryEntry>,
    defineAiState: DefineAiState,
    progress: UserProgressState,
    onDefineOnline: (String) -> Unit,
    onSpeakEnglish: (String) -> Unit,
    onSpeakBangla: (String) -> Unit,
    onSaveWord: (String, String, String, String) -> Unit,
    onOpenPremium: () -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val wordOfTheDay = remember(dictionary) {
        if (dictionary.isEmpty()) null
        else {
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            dictionary[dayOfYear % dictionary.size]
        }
    }

    val filtered = remember(query, dictionary) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) dictionary.take(60)
        else dictionary.filter {
            it.en.lowercase().contains(q) || it.bn.lowercase().contains(q) || it.pronBn.contains(q)
        }
    }

    Scaffold(
        topBar = { PathshalaTopBar(title = "শব্দভাণ্ডার (অভিধান)", onBack = onBack) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (wordOfTheDay != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("আজকের শব্দ (Word of the Day)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text("${wordOfTheDay.en} — ${wordOfTheDay.bn}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("উচ্চারণ: ${wordOfTheDay.pronBn}", style = MaterialTheme.typography.bodyMedium)
                            if (wordOfTheDay.example.isNotBlank()) {
                                Text("উদাহরণ: ${wordOfTheDay.example}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SpeakButton(label = "EN", onClick = { onSpeakEnglish(wordOfTheDay.en) })
                                SpeakButton(label = "বাংলা", onClick = { onSpeakBangla(wordOfTheDay.bn) })
                                IconButton(onClick = {
                                    onSaveWord(wordOfTheDay.en, wordOfTheDay.bn, wordOfTheDay.pronBn, wordOfTheDay.example)
                                }) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = "সেভ")
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("বাংলা বা ইংরেজি শব্দ খুঁজুন (${dictionary.size}টি অফলাইন)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "খুঁজুন") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vocab_search_input")
                )
            }

            if (query.isNotBlank() && filtered.isEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("অফলাইন তালিকায় '$query' পাওয়া যায়নি।", fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { onDefineOnline(query) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "এআই অর্থ")
                                Spacer(Modifier.width(8.dp))
                                Text("Gemini এআই দিয়ে অর্থ ও উদাহরণ খুঁজুন")
                            }
                            when (defineAiState) {
                                DefineAiState.Loading -> CircularProgressIndicator()
                                is DefineAiState.Success -> {
                                    val e = defineAiState.entry
                                    Text("${e.en} — ${e.bn}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("উচ্চারণ: ${e.pronBn}")
                                    Text("উদাহরণ: ${e.example}")
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        SpeakButton(label = "শুনুন", onClick = { onSpeakEnglish(e.en) })
                                        Button(onClick = { onSaveWord(e.en, e.bn, e.pronBn, e.example) }) {
                                            Text("আমার শব্দে যোগ")
                                        }
                                    }
                                }
                                is DefineAiState.Error -> {
                                    Text(defineAiState.message, color = MaterialTheme.colorScheme.error)
                                    if (!progress.isPremium) {
                                        Button(onClick = onOpenPremium) { Text("প্রিমিয়াম আনলক করুন") }
                                    }
                                }
                                DefineAiState.Idle -> {}
                            }
                        }
                    }
                }
            }

            items(filtered, key = { "${it.en}_${it.bn}" }) { entry ->
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${entry.en} — ${entry.bn}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("উচ্চারণ: ${entry.pronBn}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            if (entry.example.isNotBlank()) {
                                Text(entry.example, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SpeakButton(label = "EN", onClick = { onSpeakEnglish(entry.en) })
                            IconButton(onClick = { onSaveWord(entry.en, entry.bn, entry.pronBn, entry.example) }) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = "আমার শব্দে যোগ করুন")
                            }
                        }
                    }
                }
            }
        }
    }
}
