package com.storyforge.nativerewrite.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * LocalWorkspace shell. Titles, genres and word counts stay on [WorkEntity],
 * matching `Project` in `src/lib/types/project.ts`.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workspaceUid: String,
    val workspacePurpose: String,
    val name: String,
    val enableMultiWorld: Boolean = false,
    val activeWorldId: Long? = null,
    val activeWorkId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "worlds",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId")],
)
data class WorldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val identityKind: String,
    val code: String,
    val name: String,
    val description: String,
    val currentVersion: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "works",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId"), Index("worldId")],
)
data class WorkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val worldId: Long,
    val code: String,
    val kind: String,
    val novelProfile: String?,
    val title: String,
    val description: String,
    /** JSON array of genre ids. Web backups store `genres` as a string array. */
    val genresJson: String,
    val status: String,
    val targetWordCount: Int,
    val currentWordCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "outline_nodes",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId")],
)
data class OutlineNodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val workId: Long?,
    val parentId: Long?,
    val type: String,
    val title: String,
    val summary: String,
    /** Web field name is `order`. */
    @ColumnInfo(name = "sortOrder") val sortOrder: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId"), Index("outlineNodeId")],
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val outlineNodeId: Long,
    val title: String,
    val content: String,
    val wordCount: Int,
    val status: String,
    @ColumnInfo(name = "sortOrder") val sortOrder: Int,
    val notes: String,
    val summary: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
