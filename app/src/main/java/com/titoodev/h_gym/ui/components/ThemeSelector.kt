package com.titoodev.h_gym.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titoodev.h_gym.ui.theme.*

@Composable
fun ThemeSelectorCard(
    currentVariant: ThemeVariant,
    isDarkMode: Boolean,
    onVariantSelected: (ThemeVariant) -> Unit,
    onDarkModeToggled: (Boolean) -> Unit
) {
    val haptics = rememberHGymHaptics()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Tema y Apariencia Visual",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Estilo / Paleta:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Pink
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PinkPrimary)
                            .clickable {
                                haptics.performClick()
                                onVariantSelected(ThemeVariant.PINK)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentVariant == ThemeVariant.PINK) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                        }
                    }

                    // Blue
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BluePrimary)
                            .clickable {
                                haptics.performClick()
                                onVariantSelected(ThemeVariant.BLUE)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentVariant == ThemeVariant.BLUE) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                        }
                    }

                    // White / Monocromo
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(WhitePrimary)
                            .clickable {
                                haptics.performClick()
                                onVariantSelected(ThemeVariant.WHITE)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentVariant == ThemeVariant.WHITE) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                        }
                    }

                    // Verde Neón / OLED
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary)
                            .clickable {
                                haptics.performClick()
                                onVariantSelected(ThemeVariant.BLACK)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentVariant == ThemeVariant.BLACK) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Black))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Modo Oscuro:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = {
                        haptics.performClick()
                        onDarkModeToggled(it)
                    }
                )
            }
        }
    }
}
