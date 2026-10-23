package com.storyforge.nativerewrite.data

import androidx.work.Constraints

/**
 * Name reserved for the later WorkManager import/export task.
 * The skeleton does not enqueue it: a killed model call must not be retried automatically.
 */
object BackupTransfer {
    const val UNIQUE_WORK_NAME = "storyforge-native-backup"

    fun constraints(): Constraints = Constraints.Builder()
        .setRequiresStorageNotLow(true)
        .build()
}
