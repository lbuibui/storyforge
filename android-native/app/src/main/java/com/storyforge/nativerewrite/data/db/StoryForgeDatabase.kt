package com.storyforge.nativerewrite.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        WorldEntity::class,
        WorkEntity::class,
        OutlineNodeEntity::class,
        ChapterEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class StoryForgeDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun worldDao(): WorldDao
    abstract fun workDao(): WorkDao
    abstract fun outlineNodeDao(): OutlineNodeDao
    abstract fun chapterDao(): ChapterDao

    companion object {
        fun create(context: Context): StoryForgeDatabase =
            Room.databaseBuilder(context, StoryForgeDatabase::class.java, "storyforge-native.db")
                .build()
    }
}
