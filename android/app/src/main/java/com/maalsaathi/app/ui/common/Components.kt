package com.maalsaathi.app.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.data.models.EntryType

// ─── Buttons ─────────────────────────────────────

@Composable
fun MsPrimaryButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    icon: String? = null, iconVector: ImageVector? = null,
    loading: Boolean = false, enabled: Boolean = true, textSize: TextUnit = 16.sp,
) {
    val ok = enabled && !loading
    Surface(
        onClick = onClick, enabled = ok,
        modifier = modifier.fillMaxWidth().heightIn(min = Sp.primaryCta)
            .shadow(if (ok) 6.dp else 0.dp, RoundedCornerShape(Sp.buttonRadius), ambientColor = Ms.colors.primary.copy(0.25f), spotColor = Ms.colors.primary.copy(0.15f)),
        shape = RoundedCornerShape(Sp.buttonRadius),
        color = if (ok) Ms.colors.primary else Ms.colors.primary.copy(alpha = 0.35f),
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), Arrangement.Center, Alignment.CenterVertically) {
            if (loading) { LoadingDots(Ms.colors.onPrimary) } else {
                if (icon != null) { Text(icon, fontSize = 18.sp); Spacer(Modifier.width(8.dp)) }
                if (iconVector != null) { Icon(iconVector, null, tint = Ms.colors.onPrimary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
                Text(text, style = MsType.button.copy(fontSize = textSize), color = Ms.colors.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun MsSecondaryButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    icon: String? = null, enabled: Boolean = true,
) {
    Surface(
        onClick = onClick, enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = Sp.secondaryCta)
            .shadow(3.dp, RoundedCornerShape(Sp.buttonRadius), ambientColor = Color.Black.copy(0.06f)),
        shape = RoundedCornerShape(Sp.buttonRadius), color = Ms.colors.card,
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), Arrangement.Center, Alignment.CenterVertically) {
            if (icon != null) { Text(icon, fontSize = 16.sp); Spacer(Modifier.width(8.dp)) }
            Text(text, style = MsType.button, color = Ms.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun MsDestructiveButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: String? = null,
) {
    Surface(
        onClick = onClick, modifier = modifier.fillMaxWidth().heightIn(min = Sp.secondaryCta)
            .shadow(3.dp, RoundedCornerShape(Sp.buttonRadius), ambientColor = Color.Black.copy(0.06f)),
        shape = RoundedCornerShape(Sp.buttonRadius), color = Ms.colors.card,
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 14.dp), Arrangement.Center, Alignment.CenterVertically) {
            if (icon != null) { Text(icon, fontSize = 16.sp); Spacer(Modifier.width(8.dp)) }
            Text(text, style = MsType.button, color = Ms.colors.loss, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun MsTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.heightIn(min = 44.dp)) {
        Text(text, style = MsType.bodyLarge, color = Ms.colors.textSecondary)
    }
}

// ─── Cards (shadow only, no borders) ─────────────

@Composable
fun MsCard(modifier: Modifier = Modifier, borderColor: Color = Color.Transparent, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(Sp.cardRadius), ambientColor = Color.Black.copy(0.08f)),
        shape = RoundedCornerShape(Sp.cardRadius), color = Ms.colors.card,
    ) { Column(Modifier.padding(Sp.cardPad)) { content() } }
}

@Composable
fun MsHighlightCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(Sp.cardRadius), ambientColor = Ms.colors.primary.copy(0.12f)),
        shape = RoundedCornerShape(Sp.cardRadius), color = Ms.colors.card,
    ) {
        Row {
            Box(Modifier.width(3.dp).height(1.dp).weight(0.001f, false)) // spacer for accent
            Column(Modifier.weight(1f).padding(Sp.cardPad)) { content() }
        }
    }
}

@Composable
fun MsWarningCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(Sp.cardRadius), ambientColor = Color.Black.copy(0.08f)),
        shape = RoundedCornerShape(Sp.cardRadius), color = Ms.colors.card,
    ) { Column(Modifier.padding(Sp.cardPad)) { content() } }
}

// ─── Stat / Status ───────────────────────────────

@Composable
fun StatBox(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Ms.colors.textPrimary) {
    Surface(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(Sp.cardRadius), ambientColor = Color.Black.copy(0.08f)),
        shape = RoundedCornerShape(Sp.cardRadius), color = Ms.colors.card,
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MsType.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MsType.headlineMedium, color = valueColor)
        }
    }
}

enum class PillType { LIVE, DONE, CANCELLED, SCHEDULED }

@Composable
fun StatusPill(text: String, type: PillType, modifier: Modifier = Modifier) {
    val (dotColor, textColor) = when (type) {
        PillType.LIVE -> Ms.colors.primary to Ms.colors.primary
        PillType.DONE -> Ms.colors.profit to Ms.colors.profit
        PillType.CANCELLED -> Ms.colors.loss to Ms.colors.loss
        PillType.SCHEDULED -> Ms.colors.warning to Ms.colors.warning
    }
    val pulseAlpha = if (type == PillType.LIVE) {
        val anim = rememberInfiniteTransition(label = "pulse")
        val a by anim.animateFloat(1f, 0.3f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pa")
        a
    } else 1f

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(7.dp).graphicsLayer { alpha = if (type == PillType.LIVE) pulseAlpha else 1f }.clip(CircleShape).background(dotColor))
        Text(text, style = MsType.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = textColor)
    }
}

// ─── Section / Empty / Skeleton ──────────────────

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(modifier.fillMaxWidth().padding(vertical = 10.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(title, style = MsType.titleMedium.copy(fontSize = 14.sp, color = Ms.colors.textPrimary))
        trailing()
    }
}

@Composable
fun EmptyState(emoji: String, title: String, subtitle: String, buttonText: String? = null, onButton: (() -> Unit)? = null) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(title, style = MsType.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MsType.bodyMedium, textAlign = TextAlign.Center)
            if (buttonText != null && onButton != null) {
                Spacer(Modifier.height(24.dp))
                MsPrimaryButton(buttonText, onButton, Modifier.padding(horizontal = 40.dp))
            }
        }
    }
}

@Composable
fun SkeletonCard(modifier: Modifier = Modifier, height: Dp = 80.dp) {
    val anim = rememberInfiniteTransition(label = "skel")
    val alpha by anim.animateFloat(0.2f, 0.5f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "a")
    Box(modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(Sp.cardRadius)).graphicsLayer { this.alpha = alpha }.background(Ms.colors.border))
}

// ─── Journal Entry Row (clean, no card) ──────────

@Composable
fun JournalEntryRow(
    time: String, emoji: String, text: String, amount: Long = 0,
    entryType: EntryType = EntryType.EXPENSE, isFirst: Boolean = false, isLast: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            if (!isFirst) Box(Modifier.width(1.dp).height(8.dp).background(Ms.colors.divider))
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (isFirst) Ms.colors.primary else Ms.colors.divider))
            if (!isLast) Box(Modifier.width(1.dp).height(28.dp).background(Ms.colors.divider))
        }
        Column(Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 4.dp)) {
            Text(time, style = MsType.labelSmall)
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("$emoji $text", style = MsType.bodyLarge.copy(fontSize = 14.sp), maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (amount > 0) {
                    val c = if (entryType == EntryType.EXPENSE) Ms.colors.loss else Ms.colors.primary
                    val prefix = if (entryType == EntryType.EXPENSE) "−" else "+"
                    Text("$prefix${formatRupees(amount)}", style = MsType.amountMedium.copy(fontSize = 15.sp), color = c)
                }
            }
        }
    }
}

// ─── Thin Divider ────────────────────────────────

@Composable
fun ThinDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(vertical = 2.dp), thickness = 0.5.dp, color = Ms.colors.divider)
}

// ─── Loading dots ────────────────────────────────

@Composable
fun LoadingDots(color: Color) {
    val anim = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { i ->
            val a by anim.animateFloat(0.3f, 1f, infiniteRepeatable(tween(500, delayMillis = i * 150), RepeatMode.Reverse), label = "d$i")
            Box(Modifier.size(8.dp).graphicsLayer { alpha = a }.clip(CircleShape).background(color))
        }
    }
}

// ─── Profile Avatar ──────────────────────────────

@Composable
fun ProfileAvatar(name: String, onClick: () -> Unit, size: Dp = 36.dp) {
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    Box(
        Modifier.size(size).clip(CircleShape).background(Ms.colors.primary).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, fontSize = (size.value * 0.44f).sp, fontFamily = Poppins, fontWeight = FontWeight.Bold, color = Ms.colors.onPrimary)
    }
}
