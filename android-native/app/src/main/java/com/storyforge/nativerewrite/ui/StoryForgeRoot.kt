package com.storyforge.nativerewrite.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.storyforge.nativerewrite.R
import com.storyforge.nativerewrite.di.AppGraph
import com.storyforge.nativerewrite.ui.editor.EditorScreen
import com.storyforge.nativerewrite.ui.editor.EditorViewModel
import com.storyforge.nativerewrite.ui.projects.ProjectDetailScreen
import com.storyforge.nativerewrite.ui.projects.ProjectDetailViewModel
import com.storyforge.nativerewrite.ui.projects.ProjectListScreen
import com.storyforge.nativerewrite.ui.projects.ProjectListViewModel
import com.storyforge.nativerewrite.ui.settings.SettingsScreen
import com.storyforge.nativerewrite.ui.settings.SettingsViewModel

object Routes {
    const val Projects = "projects"
    const val Settings = "settings"
    const val Project = "projects/{projectId}"
    const val Editor = "projects/{projectId}/chapters/{chapterId}"

    fun project(id: Long) = "projects/$id"
    fun editor(projectId: Long, chapterId: Long) = "projects/$projectId/chapters/$chapterId"
}

@Composable
fun StoryForgeBottomBar(onProjects: Boolean, openProjects: () -> Unit, openSettings: () -> Unit) {
    ShortNavigationBar {
        ShortNavigationBarItem(
            selected = onProjects,
            onClick = openProjects,
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_projects)) },
        )
        ShortNavigationBarItem(
            selected = !onProjects,
            onClick = openSettings,
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_settings)) },
        )
    }
}

@Composable
fun StoryForgeRoot(graph: AppGraph) {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Routes.Projects,
        enterTransition = {
            fadeIn(spring(dampingRatio = ExpressiveMotion.DAMPING, stiffness = Spring.StiffnessMediumLow)) +
                slideInHorizontally(spring(dampingRatio = ExpressiveMotion.DAMPING, stiffness = Spring.StiffnessMediumLow)) { it / 6 }
        },
        exitTransition = { fadeOut(tween(120)) },
        popEnterTransition = { fadeIn(tween(150)) },
        popExitTransition = {
            fadeOut(tween(120)) +
                slideOutHorizontally(spring(dampingRatio = ExpressiveMotion.DAMPING, stiffness = Spring.StiffnessMediumLow)) { it / 6 }
        },
    ) {
        composable(Routes.Projects) {
            val model = viewModel<ProjectListViewModel>(
                factory = AppViewModelFactory { ProjectListViewModel(graph.library) },
            )
            ProjectListScreen(
                model = model,
                onOpenProject = { nav.navigate(Routes.project(it)) },
                onOpenSettings = {
                    nav.navigate(Routes.Settings) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.Settings) {
            val model = viewModel<SettingsViewModel>(
                factory = AppViewModelFactory { SettingsViewModel(graph.settings) },
            )
            SettingsScreen(
                model = model,
                onOpenProjects = { nav.popBackStack(Routes.Projects, inclusive = false) },
            )
        }
        composable(
            route = Routes.Project,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType }),
        ) { entry ->
            val projectId = entry.arguments?.getLong("projectId") ?: return@composable
            val model = viewModel<ProjectDetailViewModel>(
                factory = AppViewModelFactory { ProjectDetailViewModel(graph.library, projectId) },
            )
            ProjectDetailScreen(
                model = model,
                onBack = { nav.popBackStack() },
                onOpenChapter = { chapterId -> nav.navigate(Routes.editor(projectId, chapterId)) },
            )
        }
        composable(
            route = Routes.Editor,
            arguments = listOf(
                navArgument("projectId") { type = NavType.LongType },
                navArgument("chapterId") { type = NavType.LongType },
            ),
        ) { entry ->
            val chapterId = entry.arguments?.getLong("chapterId") ?: return@composable
            val model = viewModel<EditorViewModel>(
                factory = AppViewModelFactory { EditorViewModel(graph.library, chapterId) },
            )
            EditorScreen(model = model, onBack = { nav.popBackStack() })
        }
    }
}

class AppViewModelFactory<T : androidx.lifecycle.ViewModel>(
    private val create: () -> T,
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <VM : androidx.lifecycle.ViewModel> create(modelClass: Class<VM>): VM {
        @Suppress("UNCHECKED_CAST")
        return create() as VM
    }
}
