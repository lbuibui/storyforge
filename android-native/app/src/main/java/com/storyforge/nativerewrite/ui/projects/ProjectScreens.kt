package com.storyforge.nativerewrite.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.storyforge.nativerewrite.R
import com.storyforge.nativerewrite.data.LibraryRepository
import com.storyforge.nativerewrite.data.db.ProjectCard
import com.storyforge.nativerewrite.data.db.ProjectDetail
import com.storyforge.nativerewrite.ui.StoryForgeBottomBar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectListViewModel(repository: LibraryRepository) : ViewModel() {
    val cards: StateFlow<List<ProjectCard>> = repository.observeCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repository.ensurePlaceholder() }
    }
}

class ProjectDetailViewModel(
    repository: LibraryRepository,
    projectId: Long,
) : ViewModel() {
    val detail: StateFlow<ProjectDetail?> = repository.observeDetail(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    model: ProjectListViewModel,
    onOpenProject: (Long) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val cards by model.cards.collectAsStateWithLifecycle()
    ProjectListContent(
        cards = cards,
        onOpenProject = onOpenProject,
        onOpenSettings = onOpenSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectListContent(
    cards: List<ProjectCard>,
    onOpenProject: (Long) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pending = stringResource(R.string.projects_create_pending)
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.projects_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings))
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { scope.launch { snackbar.showSnackbar(pending) } },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.projects_create)) },
                shape = MaterialTheme.shapes.large,
            )
        },
        bottomBar = {
            StoryForgeBottomBar(onProjects = true, openProjects = {}, openSettings = onOpenSettings)
        },
    ) { padding ->
        LazyColumn(
            // Scaffold padding clears the bars. The extended FAB floats inside
            // that area, so the list itself stops above the button.
            modifier = Modifier.fillMaxSize().padding(padding).padding(bottom = 80.dp),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(cards, key = { it.project.id }) { card ->
                ProjectCardView(card = card, onClick = { onOpenProject(card.project.id) })
            }
        }
    }
}

@Composable
private fun ProjectCardView(card: ProjectCard, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AssistChip(
                onClick = onClick,
                label = { Text(stringResource(R.string.project_sample)) },
            )
            Text(
                text = card.workTitle ?: card.project.name,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = card.project.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.project_meta, card.chapterCount, card.wordCount),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    model: ProjectDetailViewModel,
    onBack: () -> Unit,
    onOpenChapter: (Long) -> Unit,
) {
    val detail by model.detail.collectAsStateWithLifecycle()
    val current = detail
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(current?.works?.firstOrNull()?.title ?: stringResource(R.string.chapters_heading))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        if (current == null) {
            Text(
                text = stringResource(R.string.missing_project),
                modifier = Modifier.padding(padding).padding(24.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.chapters_heading),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                items(current.chapters, key = { it.id }) { chapter ->
                    Card(
                        onClick = { onOpenChapter(chapter.id) },
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ListItem(
                            headlineContent = { Text(chapter.title) },
                            supportingContent = {
                                Text(chapterStatusLabel(chapter.status) + " · " + stringResource(R.string.editor_words, chapter.wordCount))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun chapterStatusLabel(status: String): String = when (status) {
    "outline" -> stringResource(R.string.chapter_status_outline)
    "revised" -> stringResource(R.string.chapter_status_revised)
    "polished" -> stringResource(R.string.chapter_status_polished)
    "final" -> stringResource(R.string.chapter_status_final)
    else -> stringResource(R.string.chapter_status_draft)
}
