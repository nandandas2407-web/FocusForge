// ============================================================
// FILE: app/src/main/java/com/example/ui/screens/WebsiteBlockerScreen.kt
// PURPOSE: Website & Domain level blocker management screen.
// CREATED: 2026-08-09
// UPDATED: 2026-08-09 — tablet-responsive layout with wider domain list
// ============================================================

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.WebsiteBlockEntity
import com.example.ui.theme.*

@Composable
fun WebsiteBlockerScreen(
    websiteBlocks: List<WebsiteBlockEntity>,
    onAddDomain: (domain: String, category: String) -> Unit,
    onRemoveDomain: (item: WebsiteBlockEntity) -> Unit
) {
    var newDomainInput by remember { mutableStateOf("") }
    val isTablet = Responsive.isTablet()

    WallpaperBackground(preset = "COSMIC_NEON") {
        ResponsiveScaffold {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Responsive.sectionSpacing())
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = GlassTokens.Info,
                            modifier = Modifier.size(if (isTablet) 36.dp else 32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Website Blocker",
                                fontSize = if (isTablet) 30.sp else 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Text(
                                text = "Block distracting web domains across all mobile browsers",
                                fontSize = if (isTablet) 14.sp else 12.sp,
                                color = GlassTokens.TextSecondary
                            )
                        }
                    }
                }

                if (isTablet) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Responsive.sectionSpacing())
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Block New Domain",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTokens.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    GlassTextField(
                                        value = newDomainInput,
                                        onValueChange = { newDomainInput = it },
                                        placeholder = "e.g. reddit.com or shopping.com...",
                                        testTagStr = "input_website_domain"
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    GlassButton(
                                        text = "Add Domain to Blocklist",
                                        onClick = {
                                            if (newDomainInput.isNotBlank()) {
                                                onAddDomain(newDomainInput.lowercase().trim(), "Distracting")
                                                newDomainInput = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        icon = Icons.Default.Add,
                                        accentColor = GlassTokens.Info,
                                        testTagStr = "btn_add_domain"
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Blocked Domains",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassTokens.TextPrimary
                                )
                                Spacer(modifier = Modifier.height(Responsive.sectionSpacing()))
                                DomainList(
                                    websiteBlocks = websiteBlocks,
                                    onRemoveDomain = onRemoveDomain
                                )
                            }
                        }
                    }
                } else {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Block New Domain",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlassTokens.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            GlassTextField(
                                value = newDomainInput,
                                onValueChange = { newDomainInput = it },
                                placeholder = "e.g. reddit.com or shopping.com...",
                                testTagStr = "input_website_domain"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            GlassButton(
                                text = "Add Domain to Blocklist",
                                onClick = {
                                    if (newDomainInput.isNotBlank()) {
                                        onAddDomain(newDomainInput.lowercase().trim(), "Distracting")
                                        newDomainInput = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Add,
                                accentColor = GlassTokens.Info,
                                testTagStr = "btn_add_domain"
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Blocked Domains",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassTokens.TextPrimary
                        )
                    }

                    items(websiteBlocks.size) { idx ->
                        val item = websiteBlocks[idx]
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.domain,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GlassTokens.TextPrimary
                                    )
                                    Text(
                                        text = "Category: ${item.category}",
                                        fontSize = 12.sp,
                                        color = GlassTokens.Info
                                    )
                                }

                                IconButton(onClick = { onRemoveDomain(item) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Domain",
                                        tint = GlassTokens.TextSecondary
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
private fun DomainList(
    websiteBlocks: List<WebsiteBlockEntity>,
    onRemoveDomain: (WebsiteBlockEntity) -> Unit
) {
    val isTablet = Responsive.isTablet()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        websiteBlocks.forEach { item ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.domain,
                            fontSize = if (isTablet) 18.sp else 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassTokens.TextPrimary
                        )
                        Text(
                            text = "Category: ${item.category}",
                            fontSize = if (isTablet) 14.sp else 12.sp,
                            color = GlassTokens.Info
                        )
                    }

                    IconButton(onClick = { onRemoveDomain(item) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Domain",
                            tint = GlassTokens.TextSecondary
                        )
                    }
                }
            }
        }
    }
}
