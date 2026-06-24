package com.maalsaathi.app.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Tab(val label: String, val icon: ImageVector) {
    Trip("Trip", Icons.Default.LocalShipping),
    Hisaab("Hisaab", Icons.Default.Wallet),
    Calendar("Calendar", Icons.Default.CalendarMonth),
    Madad("Madad", Icons.Default.Headset),
    Profile("Profile", Icons.Default.Person),
}

@Composable
fun BottomNavBar(currentTab: Tab, onTabSelected: (Tab) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Ms.colors.card)) {
        HorizontalDivider(thickness = 0.5.dp, color = Ms.colors.divider)
        Row(
            modifier = Modifier.fillMaxWidth().height(Sp.bottomNavH),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Tab.entries.forEach { tab ->
                val sel = currentTab == tab
                val color = if (sel) Ms.colors.primary else Ms.colors.textSecondary
                val iconScale by animateFloatAsState(
                    targetValue = if (sel) 1.1f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "icon_${tab.name}",
                )
                Box(
                    modifier = Modifier.weight(1f).height(Sp.bottomNavH)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onTabSelected(tab) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Icon(tab.icon, tab.label, tint = color, modifier = Modifier.size(20.dp).scale(iconScale))
                        Text(tab.label, fontSize = 10.sp, color = color, style = if (sel) MsType.labelLarge.copy(fontSize = 10.sp, color = color) else MsType.labelSmall.copy(color = color))
                    }
                }
            }
        }
    }
}
