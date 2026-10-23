package com.storyforge.nativerewrite

import com.storyforge.nativerewrite.data.BackupContract
import com.storyforge.nativerewrite.data.ProviderEndpoint
import com.storyforge.nativerewrite.data.countStoryWords
import com.storyforge.nativerewrite.ui.ExpressiveShapeScale
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeSkeletonTest {
    @Test
    fun backupVersionsMatchTheWebPackage() {
        assertEquals(15, BackupContract.CURRENT_BACKUP_VERSION)
        assertTrue(BackupContract.accepts(14))
        assertTrue(BackupContract.accepts(15))
        assertFalse(BackupContract.accepts(13))
        assertFalse(BackupContract.accepts(16))
    }

    @Test
    fun wordCountDropsWhitespaceLikeTheWebApp() {
        assertEquals(0, countStoryWords(""))
        assertEquals(2, countStoryWords("潮 声"))
        assertEquals(10, countStoryWords("hello world"))
        assertEquals(2, countStoryWords("a 潮"))
    }

    @Test
    fun modelEndpointsStayOnHttpsWithoutUserinfo() {
        assertEquals(
            "https://api.example.com/v1/chat/completions",
            ProviderEndpoint.chatCompletions("https://api.example.com/v1/"),
        )
        assertFails { ProviderEndpoint.chatCompletions("http://api.example.com/v1") }
        assertFails { ProviderEndpoint.chatCompletions("https://user:secret@api.example.com/v1") }
    }

    @Test
    fun expressiveShapeStopsGrowAndMaterial3StaysOnTheStablePin() {
        assertTrue(ExpressiveShapeScale.isIncreasing())
        assertTrue(ExpressiveShapeScale.EXTRA_LARGE_DP >= 28)
        val catalog = File("../gradle/libs.versions.toml").readText()
        assertTrue(catalog.contains("material3 = \"1.4.0\""))
        assertFalse(catalog.contains("1.5."))
    }

    private fun assertFails(block: () -> Unit) {
        try {
            block()
        } catch (_: IllegalArgumentException) {
            return
        }
        throw AssertionError("expected IllegalArgumentException")
    }
}
