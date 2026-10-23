package com.storyforge.nativerewrite.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.storyforge.nativerewrite.R
import com.storyforge.nativerewrite.data.BackupContract
import com.storyforge.nativerewrite.data.SettingsRepository
import com.storyforge.nativerewrite.data.SettingsSnapshot
import com.storyforge.nativerewrite.ui.StoryForgeBottomBar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val snapshot = repository.snapshot.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsSnapshot(),
    )

    fun setTheme(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { repository.setDynamicColor(enabled) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(model: SettingsViewModel, onOpenProjects: () -> Unit) {
    val settings by model.snapshot.collectAsStateWithLifecycle()
    val apiKey = rememberTextFieldState()
    val modelName = remember { androidx.compose.runtime.mutableStateOf("") }
    Scaffold(
        topBar = { LargeTopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
        bottomBar = {
            StoryForgeBottomBar(onProjects = false, openProjects = onOpenProjects, openSettings = {})
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.settings_appearance), style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip(stringResource(R.string.settings_theme_system), settings.themeMode == "system") {
                    model.setTheme("system")
                }
                ThemeChip(stringResource(R.string.settings_theme_light), settings.themeMode == "light") {
                    model.setTheme("light")
                }
                ThemeChip(stringResource(R.string.settings_theme_dark), settings.themeMode == "dark") {
                    model.setTheme("dark")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_dynamic_color), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.settings_dynamic_color_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = settings.dynamicColor, onCheckedChange = model::setDynamicColor)
            }
            Text(stringResource(R.string.settings_language_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.settings_language_body),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(stringResource(R.string.settings_provider_title), style = MaterialTheme.typography.titleLarge)
            OutlinedSecureTextField(
                state = apiKey,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.settings_api_key)) },
                supportingText = { Text(stringResource(R.string.settings_api_key_support)) },
                shape = MaterialTheme.shapes.large,
            )
            OutlinedTextField(
                value = modelName.value,
                onValueChange = { modelName.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.settings_model)) },
                shape = MaterialTheme.shapes.large,
            )
            Text(stringResource(R.string.settings_backup_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.settings_backup_body) +
                    " (${BackupContract.CURRENT_BACKUP_VERSION})",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
