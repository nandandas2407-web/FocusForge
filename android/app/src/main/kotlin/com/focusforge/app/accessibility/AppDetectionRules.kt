// ============================================================
// FILE: android/.../accessibility/AppDetectionRules.kt
// PURPOSE: Per-app detection rules for Shorts/Reels sub-screen
//          blocking. Loads from bundled JSON with remote update support.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
package com.focusforge.app.accessibility

import android.content.Context
import org.json.JSONObject

class AppDetectionRules {

    data class SubScreenRule(
        val name: String,
        val resourceIdContains: List<String>,
        val contentDescContains: List<String>,
        val classNameContains: List<String>
    )

    data class AppRule(
        val packageName: String,
        val blockAll: Boolean = false,
        val subScreenBlocks: List<SubScreenRule> = emptyList()
    )

    private var rules: Map<String, AppRule> = emptyMap()

    init {
        rules = loadBundledRules()
    }

    private fun loadBundledRules(): Map<String, AppRule> {
        val rulesJson = """
        {
            "com.instagram.android": {
                "subscreen_blocks": [
                    {
                        "name": "reels",
                        "resource_id_contains": ["clips_viewer", "reel_", "clips_tab", "clips_grid"],
                        "content_desc_contains": ["Reels", "reels"],
                        "class_name_contains": ["ReelsFragment", "ClipsViewer"]
                    }
                ]
            },
            "com.google.android.youtube": {
                "subscreen_blocks": [
                    {
                        "name": "shorts",
                        "resource_id_contains": ["reel_player", "shorts_", "reel_recycler", "shorts_player"],
                        "content_desc_contains": ["Shorts", "shorts"],
                        "class_name_contains": ["ShortsPlayer", "ReelWatch", "ShortsFragment"]
                    }
                ]
            },
            "com.zhiliaoapp.musically": {
                "block_all": false,
                "subscreen_blocks": []
            },
            "com.snapchat.android": {
                "subscreen_blocks": [
                    {
                        "name": "spotlight",
                        "resource_id_contains": ["spotlight_", "snap_spotlight"],
                        "content_desc_contains": ["Spotlight"],
                        "class_name_contains": ["SpotlightFragment"]
                    }
                ]
            },
            "com.facebook.katana": {
                "subscreen_blocks": [
                    {
                        "name": "reels",
                        "resource_id_contains": ["reels_", "video_reels"],
                        "content_desc_contains": ["Reels"],
                        "class_name_contains": ["ReelsFragment", "VideoReelsActivity"]
                    }
                ]
            }
        }
        """.trimIndent()

        return parseRules(rulesJson)
    }

    fun loadRemoteRules(json: String) {
        try {
            rules = parseRules(json)
        } catch (e: Exception) {
            // Keep bundled rules on parse failure
        }
    }

    private fun parseRules(json: String): Map<String, AppRule> {
        val root = JSONObject(json)
        val result = mutableMapOf<String, AppRule>()

        for (packageName in root.keys()) {
            val appJson = root.getJSONObject(packageName)
            val blockAll = appJson.optBoolean("block_all", false)

            val subScreens = mutableListOf<SubScreenRule>()
            val subscreenArray = appJson.optJSONArray("subscreen_blocks")

            subscreenArray?.let { arr ->
                for (i in 0 until arr.length()) {
                    val ruleJson = arr.getJSONObject(i)
                    subScreens.add(
                        SubScreenRule(
                            name = ruleJson.getString("name"),
                            resourceIdContains = jsonArrayToList(ruleJson.optJSONArray("resource_id_contains")),
                            contentDescContains = jsonArrayToList(ruleJson.optJSONArray("content_desc_contains")),
                            classNameContains = jsonArrayToList(ruleJson.optJSONArray("class_name_contains"))
                        )
                    )
                }
            }

            result[packageName] = AppRule(
                packageName = packageName,
                blockAll = blockAll,
                subScreenBlocks = subScreens
            )
        }

        return result
    }

    private fun jsonArrayToList(arr: org.json.JSONArray?): List<String> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { arr.getString(it) }
    }

    fun getRule(packageName: String): AppRule? = rules[packageName]

    fun getAllRules(): Map<String, AppRule> = rules.toMap()
}
