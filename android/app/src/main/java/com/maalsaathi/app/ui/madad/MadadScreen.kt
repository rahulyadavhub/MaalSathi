package com.maalsaathi.app.ui.madad

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.ui.common.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class Msg(val id: String, val text: String, val isBot: Boolean, val time: Long = System.currentTimeMillis())

@Composable
fun MadadScreen() {
    val c = Ms.colors; val ctx = LocalContext.current
    val msgs = remember { mutableStateListOf(Msg("w", "Namaste! Main MaalSaathi hun. 🙏\nSupport jaldi aa raha hai.\nTab tak Trip tab se apna kaam karo.", true)) }
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val timeFmt = remember { SimpleDateFormat("h:mm a", Locale("en", "IN")) }

    val send = {
        val t = input.trim()
        if (t.isNotEmpty()) {
            msgs.add(Msg("u_${System.currentTimeMillis()}", t, false))
            msgs.add(Msg("b_${System.currentTimeMillis()}", "Abhi live support available nahi hai. Jaldi aa raha hai!\nTab tak Trip tab se trip log karo, kharcha daalo, aur Hisaab tab se apna P&L dekho.", true))
            input = ""
            scope.launch { listState.animateScrollToItem(msgs.lastIndex) }
        }
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Spacer(Modifier.height(Sp.sectionGap))
        Column(Modifier.padding(horizontal = Sp.screenHPad)) {
            Text("Madad", style = MsType.headlineLarge, color = c.textPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Box(Modifier.size(8.dp).background(c.textSecondary, CircleShape))
                Text("Jaldi aa raha hai", style = MsType.bodyMedium)
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = c.border)

        LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(horizontal = Sp.screenHPad, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(msgs, key = { it.id }) { msg ->
                Column(Modifier.fillMaxWidth(), horizontalAlignment = if (msg.isBot) Alignment.Start else Alignment.End) {
                    Surface(Modifier.widthIn(max = 280.dp),
                        shape = RoundedCornerShape(topStart = Sp.cardRadius, topEnd = Sp.cardRadius,
                            bottomStart = if (msg.isBot) 4.dp else Sp.cardRadius, bottomEnd = if (msg.isBot) Sp.cardRadius else 4.dp),
                        color = if (msg.isBot) c.statBg else c.card,
                        border = if (!msg.isBot) androidx.compose.foundation.BorderStroke(1.dp, c.border) else null) {
                        Text(msg.text, style = MsType.bodyLarge, color = c.textPrimary, modifier = Modifier.padding(14.dp))
                    }
                    Text(timeFmt.format(Date(msg.time)), fontSize = 11.sp, fontFamily = NotoSans, color = c.textSecondary, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        // WhatsApp
        MsSecondaryButton("💬 WhatsApp Pe Baat Karo", onClick = {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918879688678?text=MaalSaathi%20se%20madad%20chahiye")))
        }, modifier = Modifier.padding(horizontal = Sp.screenHPad, vertical = 4.dp))

        HorizontalDivider(color = c.border)
        Row(modifier = Modifier.fillMaxWidth().background(c.card).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("Message likho...", color = c.textSecondary.copy(0.5f)) },
                maxLines = 3, shape = RoundedCornerShape(Sp.inputRadius),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = c.border, unfocusedBorderColor = c.border, focusedTextColor = c.textPrimary, unfocusedTextColor = c.textPrimary, cursorColor = c.primary, focusedContainerColor = c.statBg, unfocusedContainerColor = c.statBg),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send() }),
            )
            IconButton(onClick = { send() }, modifier = Modifier.size(Sp.iconButton), enabled = input.trim().isNotEmpty(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (input.trim().isNotEmpty()) c.primary else c.statBg,
                    contentColor = if (input.trim().isNotEmpty()) c.onPrimary else c.textSecondary)) {
                Icon(Icons.AutoMirrored.Filled.Send, "Bhejo", Modifier.size(20.dp))
            }
        }
    }
}
