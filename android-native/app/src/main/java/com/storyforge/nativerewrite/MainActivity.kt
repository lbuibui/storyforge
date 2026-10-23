package com.storyforge.nativerewrite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.storyforge.nativerewrite.data.SettingsSnapshot
import com.storyforge.nativerewrite.ui.StoryForgeRoot
import com.storyforge.nativerewrite.ui.theme.StoryForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val graph = (application as StoryForgeApp).graph
        setContent {
            val settings by graph.settings.snapshot.collectAsStateWithLifecycle(
                initialValue = SettingsSnapshot(),
            )
            val dark = when (settings.themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            StoryForgeTheme(darkTheme = dark, dynamicColor = settings.dynamicColor) {
                StoryForgeRoot(graph)
            }
        }
    }
}
