package top.yogiczy.mytv.ui.screens.leanback.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import top.yogiczy.mytv.R
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsViewModel
import top.yogiczy.mytv.ui.theme.LeanbackTheme

@Composable
fun LeanbackSettingsCategoryDebug(
    modifier: Modifier = Modifier,
    settingsViewModel: LeanbackSettingsViewModel = viewModel(),
) {
    TvLazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
    ) {
        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_debug_show_fps),
                supportingContent = stringResource(R.string.settings_debug_show_fps_desc),
                trailingContent = {
                    Switch(checked = settingsViewModel.debugShowFps, onCheckedChange = null)
                },
                onSelected = {
                    settingsViewModel.debugShowFps = !settingsViewModel.debugShowFps
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_debug_show_player_info),
                supportingContent = stringResource(R.string.settings_debug_show_player_info_desc),
                trailingContent = {
                    Switch(
                        checked = settingsViewModel.debugShowVideoPlayerMetadata,
                        onCheckedChange = null
                    )
                },
                onSelected = {
                    settingsViewModel.debugShowVideoPlayerMetadata =
                        !settingsViewModel.debugShowVideoPlayerMetadata
                },
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackSettingsCategoryDebugPreview() {
    LeanbackTheme {
        LeanbackSettingsCategoryDebug(
            modifier = Modifier.padding(20.dp)
        )
    }
}
