// ============================================================
// FILE: app/src/main/java/com/example/ui/components/AmbientSoundPlayer.kt
// PURPOSE: Ambient focus sound audio synthesizer/player card for Pomodoro sessions.
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GlassTokens

data class AmbientSound(
    val id: String,
    val name: String,
    val iconName: String
)

@Composable
fun AmbientSoundPlayer(
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var selectedSoundId by remember { mutableStateOf("LOFI") }
    var volume by remember { mutableStateOf(0.7f) }

    val sounds = listOf(
        AmbientSound("LOFI", "Lo-Fi Beats", "Music"),
        AmbientSound("RAIN", "Rain & Thunder", "Rain"),
        AmbientSound("BINAURAL", "Binaural Focus", "Waves"),
        AmbientSound("FOREST", "Forest Stream", "Nature")
    )

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = GlassTokens.Info,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ambient Focus Sound",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassTokens.TextPrimary
                )
            }

            IconButton(
                onClick = { isPlaying = !isPlaying }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = "Play/Pause Sound",
                    tint = GlassTokens.Accent,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sounds.forEach { sound ->
                val isSelected = selectedSoundId == sound.id
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedSoundId = sound.id
                            isPlaying = true
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) GlassTokens.Accent.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sound.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GlassTokens.TextPrimary else GlassTokens.TextMuted
                        )
                    }
                }
            }
        }

        if (isPlaying) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VolumeDown,
                    contentDescription = null,
                    tint = GlassTokens.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = GlassTokens.Info,
                        activeTrackColor = GlassTokens.Info,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = GlassTokens.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
