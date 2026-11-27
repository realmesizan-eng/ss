package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.LicenseRequestEntity
import com.example.data.local.UserProgressState
import com.example.ui.components.PathshalaTopBar
import com.example.ui.theme.SuccessGreen

@Composable
fun AdminPanelScreen(
    progress: UserProgressState,
    requests: List<LicenseRequestEntity>,
    savedWordsCount: Int,
    tracedLettersCount: Int,
    onGenerateKey: (seed: String) -> String,
    onApproveRequest: (LicenseRequestEntity) -> String,
    onDeleteRequest: (Int) -> Unit,
    onTogglePremium: (Boolean) -> Unit,
    onUpdateSettings: (newPin: String, bkash: String, whatsapp: String, fee: Int) -> Unit,
    onShowMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var unlocked by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var seedInput by remember { mutableStateOf("") }
    var generatedKey by remember { mutableStateOf("") }

    var editPin by remember(progress.adminPin) { mutableStateOf(progress.adminPin) }
    var editBkash by remember(progress.bkashNumber) { mutableStateOf(progress.bkashNumber) }
    var editWhatsapp by remember(progress.whatsappNumber) { mutableStateOf(progress.whatsappNumber) }
    var editFee by remember(progress.licenseFeeTaka) { mutableStateOf(progress.licenseFeeTaka.toString()) }

    Scaffold(
        topBar = { PathshalaTopBar(title = "এডমিন পেনেল", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!unlocked) {
                Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "এডমিন লগইন",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "লাইসেন্স কী তৈরি, বিকাশ পেমেন্ট অনুমোদন ও নম্বর কনফিগার করতে এডমিন পিন দিন (ডিফল্ট পিন: 1234)।",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                pinInput = it
                                pinError = false
                            },
                            label = { Text("এডমিন পিন") },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = pinError,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_pin_input")
                        )
                        if (pinError) {
                            Text("ভুল পিন! সঠিক পিন দিন।", color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = {
                                if (pinInput == progress.adminPin) {
                                    unlocked = true
                                } else {
                                    pinError = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("admin_unlock_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("পেনেল খুলুন")
                        }
                    }
                }
            } else {
                // Quick License Key Generator
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("লাইসেন্স কী জেনারেটর", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = seedInput,
                            onValueChange = { seedInput = it },
                            label = { Text("ক্রেতার মোবাইল নম্বর বা TrxID লিখুন") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_seed_input")
                        )
                        Button(
                            onClick = { generatedKey = onGenerateKey(seedInput) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_generate_key_button")
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("লাইসেন্স কী তৈরি করুন")
                        }
                        if (generatedKey.isNotBlank()) {
                            Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = generatedKey,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(generatedKey))
                                            onShowMessage("লাইসেন্স কী $generatedKey কপি হয়েছে")
                                        },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "কপি করুন")
                                    }
                                }
                            }
                        }
                    }
                }

                // Pending bKash Requests
                Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "বিকাশ পেমেন্ট আবেদন (${requests.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (requests.isEmpty()) {
                            Text("কোনো পেন্ডিং বা পুরনো আবেদন নেই।", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            requests.forEach { req ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${req.buyerPhone} | TrxID: ${req.trxId}",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = req.status,
                                                color = if (req.status == "APPROVED") SuccessGreen else MaterialTheme.colorScheme.tertiary,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                        if (req.note.isNotBlank()) {
                                            Text("নোট: ${req.note}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (req.generatedKey.isNotBlank()) {
                                            Text(
                                                text = "কী: ${req.generatedKey}",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (req.status != "APPROVED") {
                                                Button(onClick = {
                                                    val k = onApproveRequest(req)
                                                    clipboard.setText(AnnotatedString(k))
                                                }) {
                                                    Icon(Icons.Default.Check, contentDescription = null)
                                                    Spacer(Modifier.width(4.dp))
                                                    Text("অনুমোদন ও কপি")
                                                }
                                            } else {
                                                OutlinedButton(onClick = {
                                                    clipboard.setText(AnnotatedString(req.generatedKey))
                                                    onShowMessage("কী কপি হয়েছে: ${req.generatedKey}")
                                                }) {
                                                    Text("কী কপি")
                                                }
                                            }
                                            IconButton(onClick = { onDeleteRequest(req.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Stats & Testing Switch
                Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("অ্যাপ পরিসংখ্যান ও টেস্ট", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("মোট তারকা: ${progress.totalStars} ★ | স্ট্রিক: ${progress.streakDays} দিন")
                        Text("সংরক্ষিত শব্দ: $savedWordsCount | অনুশীলন বর্ণ: $tracedLettersCount")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("এই ডিভাইসে প্রিমিয়াম চালু (টেস্ট)")
                            Switch(
                                checked = progress.isPremium,
                                onCheckedChange = onTogglePremium,
                                modifier = Modifier.testTag("admin_premium_switch")
                            )
                        }
                    }
                }

                // Editable Payment & PIN Settings
                Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("পেমেন্ট ও এডমিন কনফিগারেশন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = editBkash,
                            onValueChange = { editBkash = it },
                            label = { Text("বিকাশ Send Money নম্বর") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editWhatsapp,
                            onValueChange = { editWhatsapp = it },
                            label = { Text("WhatsApp নম্বর") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editFee,
                            onValueChange = { editFee = it.filter { ch -> ch.isDigit() } },
                            label = { Text("লাইসেন্স ফি (টাকা)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editPin,
                            onValueChange = { editPin = it },
                            label = { Text("নতুন এডমিন পিন (কমপক্ষে ৪ সংখ্যা)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                onUpdateSettings(
                                    editPin,
                                    editBkash,
                                    editWhatsapp,
                                    editFee.toIntOrNull() ?: 100
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("কনফিগারেশন সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }
    }
}
