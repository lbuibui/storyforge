package com.storyforge.nativerewrite.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class ProjectCard(
    @Embedded val project: ProjectEntity,
    @ColumnInfo(name = "workTitle") val workTitle: String?,
    @ColumnInfo(name = "wordCount") val wordCount: Int,
    @ColumnInfo(name = "chapterCount") val chapterCount: Int,
)

data class ProjectDetail(
    val project: ProjectEntity,
    val works: List<WorkEntity>,
    val chapters: List<ChapterEntity>,
)

@Dao
interface ProjectDao {
    @Query("SELECT COUNT(*) FROM projects")
    suspend fun count(): Int

    @Query(
        """
        SELECT projects.*,
               works.title AS workTitle,
               COALESCE(works.currentWordCount, 0) AS wordCount,
               (SELECT COUNT(*) FROM chapters WHERE chapters.projectId = projects.id) AS chapterCount
        FROM projects
        LEFT JOIN works ON works.id = projects.activeWorkId
        ORDER BY projects.updatedAt DESC
        """,
    )
    fun observeCards(): Flow<List<ProjectCard>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observe(id: Long): Flow<ProjectEntity?>

    @Insert
    suspend fun insert(project: ProjectEntity): Long

    @Query(
        """
        UPDATE projects
        SET activeWorldId = :worldId, activeWorkId = :workId, updatedAt = :updatedAt
        WHERE id = :projectId
        """,
    )
    suspend fun setActive(projectId: Long, worldId: Long, workId: Long, updatedAt: Long)
}

@Dao
interface WorldDao {
    @Insert
    suspend fun insert(world: WorldEntity): Long
}

@Dao
interface WorkDao {
    @Query("SELECT * FROM works WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun observeByProject(projectId: Long): Flow<List<WorkEntity>>

    @Insert
    suspend fun insert(work: WorkEntity): Long
}

@Dao
interface OutlineNodeDao {
    @Insert
    suspend fun insert(node: OutlineNodeEntity): Long
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE projectId = :projectId ORDER BY sortOrder ASC")
    fun observeByProject(projectId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    fun observe(id: Long): Flow<ChapterEntity?>

    @Insert
    suspend fun insert(chapter: ChapterEntity): Long
}
