package com.storyforge.nativerewrite.data

/**
 * Web backups currently write version 15 and still accept version 14.
 * See `CURRENT_BACKUP_VERSION` in `src/lib/export/backup-trust.ts`.
 */
object BackupContract {
    const val CURRENT_BACKUP_VERSION = 15
    val acceptedVersions: Set<Int> = setOf(14, 15)

    fun accepts(version: Int): Boolean = version in acceptedVersions
}
