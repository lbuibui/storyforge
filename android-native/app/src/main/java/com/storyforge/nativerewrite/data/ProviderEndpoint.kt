package com.storyforge.nativerewrite.data

/**
 * OpenAI-compatible chat endpoint. Mirrors the Capacitor HTTP plugin:
 * HTTPS only, no userinfo, callers must disable redirect following.
 */
object ProviderEndpoint {
    fun chatCompletions(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        require(trimmed.startsWith("https://")) { "StoryForge only calls HTTPS model endpoints." }
        require(!trimmed.substringAfter("://").contains('@')) { "Model URLs cannot carry userinfo." }
        return "$trimmed/chat/completions"
    }
}
