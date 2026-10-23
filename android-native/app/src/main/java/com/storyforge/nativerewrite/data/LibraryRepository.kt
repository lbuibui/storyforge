package com.storyforge.nativerewrite.data

import androidx.room.withTransaction
import com.storyforge.nativerewrite.data.db.ChapterEntity
import com.storyforge.nativerewrite.data.db.OutlineNodeEntity
import com.storyforge.nativerewrite.data.db.ProjectCard
import com.storyforge.nativerewrite.data.db.ProjectDetail
import com.storyforge.nativerewrite.data.db.ProjectEntity
import com.storyforge.nativerewrite.data.db.StoryForgeDatabase
import com.storyforge.nativerewrite.data.db.WorkEntity
import com.storyforge.nativerewrite.data.db.WorldEntity
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LibraryRepository(private val database: StoryForgeDatabase) {
    fun observeCards(): Flow<List<ProjectCard>> = database.projectDao().observeCards()

    fun observeDetail(projectId: Long): Flow<ProjectDetail?> = combine(
        database.projectDao().observe(projectId),
        database.workDao().observeByProject(projectId),
        database.chapterDao().observeByProject(projectId),
    ) { project, works, chapters ->
        project?.let { ProjectDetail(it, works, chapters) }
    }

    fun observeChapter(chapterId: Long): Flow<ChapterEntity?> = database.chapterDao().observe(chapterId)

    suspend fun ensurePlaceholder() {
        database.withTransaction {
            if (database.projectDao().count() > 0) return@withTransaction
            val now = System.currentTimeMillis()
            val content = "灯塔还亮着。潮水退到旧栈桥下面，木头上留着一层盐。" +
                "这是原生骨架里的示例章节，用来查看编辑器，不是从网页导入的作品。"
            val words = countStoryWords(content)
            val projectId = database.projectDao().insert(
                ProjectEntity(
                    workspaceUid = UUID.randomUUID().toString(),
                    workspacePurpose = "independent-work",
                    name = "本地工作区",
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            val worldId = database.worldDao().insert(
                WorldEntity(
                    projectId = projectId,
                    identityKind = "workspace-scope",
                    code = "scope-${UUID.randomUUID()}",
                    name = "工作区范围",
                    description = "",
                    currentVersion = 1,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            val workId = database.workDao().insert(
                WorkEntity(
                    projectId = projectId,
                    worldId = worldId,
                    code = "work-${UUID.randomUUID()}",
                    kind = "novel",
                    novelProfile = "long",
                    title = "雾港手记",
                    description = "原生骨架示例",
                    genresJson = """["xuanyi"]""",
                    status = "drafting",
                    targetWordCount = 20000,
                    currentWordCount = words,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            val outlineId = database.outlineNodeDao().insert(
                OutlineNodeEntity(
                    projectId = projectId,
                    workId = workId,
                    parentId = null,
                    type = "chapter",
                    title = "第一章 潮声",
                    summary = "灯塔未灭，潮水退到栈桥下。",
                    sortOrder = 1,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            database.chapterDao().insert(
                ChapterEntity(
                    projectId = projectId,
                    outlineNodeId = outlineId,
                    title = "第一章 潮声",
                    content = content,
                    wordCount = words,
                    status = "draft",
                    sortOrder = 1,
                    notes = "",
                    summary = "灯塔未灭，潮水退到栈桥下。",
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            database.projectDao().setActive(projectId, worldId, workId, now)
        }
    }
}
