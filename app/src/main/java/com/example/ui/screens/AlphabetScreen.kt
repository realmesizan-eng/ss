package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LetterItem
import com.example.data.LettersBundle
import com.example.ui.components.PathshalaTopBar
import com.example.ui.components.SpeakButton

@Composable
fun AlphabetScreen(
    bundle: LettersBundle,
    onSpeakBangla: (String) -> Unit,
    onSpeakEnglish: (String) -> Unit,
    onSaveWord: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    val tabs = listOf("স্বরবর্ণ (১১)", "ব্যঞ্জনবর্ণ (৩৯)", "সংখ্যা (০-৯)", "কার-চিহ্ন", "ফলা ও যুক্তবর্ণ")
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedLetter by remember { mutableStateOf<LetterItem?>(null) }

    Scaffold(
        topBar = {
            PathshalaTopBar(title = "বাংলা বর্ণমালা", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = {
                            selectedTab = idx
                            selectedLetter = null
                        },
                        text = { Text(title) }
                    )
                }
            }

            if (selectedLetter != null) {
                val item = selectedLetter!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = item.char,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.emoji} ${item.wordBn} (${item.wordEn})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "উচ্চারণ: ${item.nameBn} • English: ${item.pronEn}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SpeakButton(
                                    label = "বাংলা",
                                    onClick = { onSpeakBangla("${item.char} তে ${item.wordBn}") }
                                )
                                SpeakButton(
                                    label = "EN (${item.pronEn})",
                                    onClick = { onSpeakEnglish("${item.pronEn}, ${item.wordEn}") },
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> LetterGrid(
                    items = bundle.vowels,
                    onTap = { item ->
                        selectedLetter = item
                        onSpeakBangla("${item.char} তে ${item.wordBn}")
                    }
                )
                1 -> LetterGrid(
                    items = bundle.consonants,
                    onTap = { item ->
                        selectedLetter = item
                        onSpeakBangla("${item.char} তে ${item.wordBn}")
                    }
                )
                2 -> LetterGrid(
                    items = bundle.digits,
                    onTap = { item ->
                        selectedLetter = item
                        onSpeakBangla("${item.nameBn}, ${item.wordBn}")
                    }
                )
                3 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "ক-এর সাথে কার-চিহ্ন যোগ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(bundle.kaars) { kaar ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSpeakBangla("${kaar.nameBn}, ${kaar.baseWithKo}, ${kaar.exampleWord}")
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
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = kaar.baseWithKo,
                                                    fontSize = 26.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "ক + ${kaar.symbol} = ${kaar.baseWithKo}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${kaar.nameBn} • উদাহরণ: ${kaar.exampleWord}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    SpeakButton(
                                        label = kaar.pronEn,
                                        onClick = { onSpeakEnglish(kaar.pronEn) }
                                    )
                                }
                            }
                        }
                    }
                }
                4 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "৪টি ফলা চিহ্ন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(bundle.folas) { fola ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSpeakBangla("${fola.nameBn}, ${fola.example}") },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("${fola.nameBn} (${fola.parts})", fontWeight = FontWeight.Bold)
                                        Text("উদাহরণ: ${fola.example}")
                                    }
                                    SpeakButton(
                                        label = fola.pronEn,
                                        onClick = { onSpeakBangla("${fola.nameBn}, ${fola.example}") }
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "যুক্তবর্ণ ভাঙা (${bundle.juktoborno.size}টি)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(bundle.juktoborno) { jb ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSpeakBangla("${jb.parts}, শব্দ ${jb.example}")
                                    },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = jb.conjunct,
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "${jb.conjunct} = ${jb.parts}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "শব্দ: ${jb.example} (${jb.pronEn})",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    SpeakButton(
                                        label = "শুনুন",
                                        onClick = { onSpeakBangla("${jb.parts}, ${jb.example}") }
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

@Composable
private fun LetterGrid(
    items: List<LetterItem>,
    onTap: (LetterItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 104.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.char }) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTap(item) }
                    .testTag("letter_tile_${item.char}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item.char,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${item.emoji} ${item.wordBn}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = item.pronEn,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
