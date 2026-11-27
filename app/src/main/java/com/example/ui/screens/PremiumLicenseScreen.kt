package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.UserProgressState
import com.example.ui.components.PathshalaTopBar
import com.example.ui.theme.SuccessGreen

@Composable
fun PremiumLicenseScreen(
    progress: UserProgressState,
    onSubmitRequest: (buyerPhone: String, trxId: String, note: String) -> Unit,
    onActivateKey: (String) -> Boolean,
    onShowMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var licenseKeyInput by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var trxId by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var showParentGate by remember { mutableStateOf(false) }
    var parentAnswer by remember { mutableStateOf("") }

    Scaffold(
        topBar = { PathshalaTopBar(title = "প্রিমিয়াম লাইসেন্স", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (progress.isPremium) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "সক্রিয়",
                            tint = SuccessGreen
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "প্রিমিয়াম লাইসেন্স সক্রিয় আছে!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text("লাইসেন্স কী: ${progress.activeLicenseKey}")
                        }
                    }
                }
            }

            // Benefits Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "প্রিমিয়াম সুবিধা (একবার পেমেন্ট ${progress.licenseFeeTaka} টাকা)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("• বাংলা পড়া স্তর ৩, ৪, ৫ ও সব ১০টি ছবিযুক্ত গল্প")
                    Text("• English সব ১১টি বিষয় ও ১০০টি দৈনন্দিন বাক্য")
                    Text("• প্রতিদিন সীমাহীন কুইজ ও অনলাইন এআই অভিধান")
                }
            }

            // bKash & WhatsApp Payment Instructions
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "লাইসেন্স নেওয়ার নিয়ম (বিকাশ ও WhatsApp)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "১) নিচের বিকাশ নম্বরে ${progress.licenseFeeTaka} টাকা Send Money করুন।",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("bKash Send Money (${progress.licenseFeeTaka} টাকা)", style = MaterialTheme.typography.labelLarge, color = Color(0xFFD12053))
                                Text(
                                    text = progress.bkashNumber,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8C0D33)
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(progress.bkashNumber))
                                    onShowMessage("বিকাশ নম্বর ${progress.bkashNumber} কপি হয়েছে")
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "বিকাশ নম্বর কপি করুন", tint = Color(0xFFD12053))
                            }
                        }
                    }

                    Text(
                        text = "২) টাকা পাঠানোর পর TrxID বা স্ক্রিনশট ${progress.whatsappNumber} নম্বরে WhatsApp করুন।",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(progress.whatsappNumber))
                                onShowMessage("WhatsApp নম্বর ${progress.whatsappNumber} কপি হয়েছে")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("নম্বর কপি")
                        }
                        Button(
                            onClick = { showParentGate = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F8A4C)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_whatsapp_button")
                        ) {
                            Text("WhatsApp খুলুন")
                        }
                    }
                }
            }

            // Submit TrxID Request inside App (for Admin Panel approval)
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "বিকাশ TrxID জমা দিন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = buyerPhone,
                        onValueChange = { buyerPhone = it },
                        label = { Text("আপনার বিকাশ নম্বর") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bkash_phone_input")
                    )
                    OutlinedTextField(
                        value = trxId,
                        onValueChange = { trxId = it },
                        label = { Text("বিকাশ TrxID (যেমন: BGD9X4K2)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bkash_trx_input")
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("নাম / নোট (ঐচ্ছিক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onSubmitRequest(buyerPhone, trxId, note)
                            trxId = ""
                            note = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_bkash_request_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("আবেদন জমা দিন")
                    }
                }
            }

            // Activate License Key
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "লাইসেন্স কী দিয়ে আনলক করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = licenseKeyInput,
                        onValueChange = { licenseKeyInput = it },
                        label = { Text("লাইসেন্স কী (PS-XXXX-XXXX)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("license_key_input")
                    )
                    Button(
                        onClick = {
                            if (onActivateKey(licenseKeyInput)) {
                                licenseKeyInput = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("activate_license_button")
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("লাইসেন্স সক্রিয় করুন")
                    }
                }
            }
        }
    }

    if (showParentGate) {
        AlertDialog(
            onDismissRequest = { showParentGate = false },
            title = { Text("অভিভাবক যাচাই") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("বাইরের WhatsApp অ্যাপ খুলতে নিচের সহজ গুণটির উত্তর দিন (অভিভাবকদের জন্য):")
                    Text("৭ × ৪ = ?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = parentAnswer,
                        onValueChange = { parentAnswer = it },
                        label = { Text("উত্তর লিখুন") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = parentAnswer.trim()
                        if (clean == "28" || clean == "২৮") {
                            showParentGate = false
                            parentAnswer = ""
                            val digits = progress.whatsappNumber.filter { it.isDigit() }
                            val intl = if (digits.startsWith("0")) "88$digits" else digits
                            val msg = Uri.encode("আসসালামু আলাইকুম, শিখি প্রিমিয়াম লাইসেন্সের জন্য ${progress.licenseFeeTaka} টাকা বিকাশে সেন্ড মানি করেছি।")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$intl?text=$msg"))
                            runCatching { context.startActivity(intent) }.onFailure {
                                onShowMessage("WhatsApp খোলা যায়নি। নম্বর: ${progress.whatsappNumber}")
                            }
                        } else {
                            onShowMessage("সঠিক উত্তর দিন (৭ × ৪ = ২৮)")
                        }
                    }
                ) {
                    Text("WhatsApp এ যান")
                }
            },
            dismissButton = {
                TextButton(onClick = { showParentGate = false }) { Text("বাতিল") }
            }
        )
    }
}
