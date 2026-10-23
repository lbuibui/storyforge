package com.storyforge.nativerewrite.di

import android.content.Context
import com.storyforge.nativerewrite.data.LibraryRepository
import com.storyforge.nativerewrite.data.SettingsRepository
import com.storyforge.nativerewrite.data.db.StoryForgeDatabase

/** Hand-written graph. Hilt waits until import jobs and the model client settle. */
class AppGraph(context: Context) {
    private val database = StoryForgeDatabase.create(context)
    val library = LibraryRepository(database)
    val settings = SettingsRepository(context)
}
