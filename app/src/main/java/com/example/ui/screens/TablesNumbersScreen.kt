package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.PathshalaTopBar
import com.example.ui.components.SpeakButton

@Composable
fun TablesNumbersScreen(
    onSpeakBangla: (String) -> Unit,
    onSpeakEnglish: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTable by remember { mutableIntStateOf(2) }
    val bnDigits = remember { charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯') }
    fun toBn(n: Int): String = n.toString().map { bnDigits[it - '0'] }.joinToString("")

    Scaffold(
        topBar = { PathshalaTopBar(title = "নামতা ও সংখ্যা (১-২০)", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "ঘরের নামতা বাছাই করুন:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((1..20).toList()) { num ->
                    FilterChip(
                        selected = selectedTable == num,
                        onClick = { selectedTable = num },
                        label = { Text("${toBn(num)} (${num})") }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeakButton(
                    label = "পুরো নামতা শুনুন (বাংলা)",
                    onClick = {
                        val full = (1..10).joinToString("। ") { i ->
                            "${toBn(selectedTable)} একে ${toBn(selectedTable * i)}"
                                .replace("একে", when (i) {
                                    1 -> "একে"
                                    2 -> "দুগুণে"
                                    3 -> "তিনে"
                                    4 -> "চারে"
                                    5 -> "পাঁচে"
                                    6 -> "ছয়ে"
                                    7 -> "সাতে"
                                    8 -> "আটে"
                                    9 -> "নয়ে"
                                    else -> "দশে"
                                })
                        }
                        onSpeakBangla(full)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items((1..10).toList()) { mul ->
                    val product = selectedTable * mul
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSpeakBangla("${toBn(selectedTable)} গুণ ${toBn(mul)} সমান ${toBn(product)}")
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${toBn(selectedTable)} × ${toBn(mul)} = ${toBn(product)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$selectedTable × $mul = $product",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
