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
fun LeanbackSettingsCategoryFavorite(
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
                headlineContent = stringResource(R.string.settings_favorite_enable),
                trailingContent = {
                    Switch(
                        checked = settingsViewModel.iptvChannelFavoriteEnable,
                        onCheckedChange = null
                    )
                },
                onSelected = {
                    settingsViewModel.iptvChannelFavoriteEnable =
                        !settingsViewModel.iptvChannelFavoriteEnable
                    if (!settingsViewModel.iptvChannelFavoriteEnable) {
                        settingsViewModel.iptvChannelFavoriteListVisible = false
                    }
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_favorite_current),
                supportingContent = stringResource(R.string.settings_favorite_current_desc),
                trailingContent = stringResource(R.string.panel_channels_count, settingsViewModel.iptvChannelFavoriteList.size),
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_favorite_clear),
                supportingContent = stringResource(R.string.settings_favorite_clear_desc),
                onSelected = {
                    settingsViewModel.iptvChannelFavoriteList = emptySet()
                    settingsViewModel.iptvChannelFavoriteListVisible = false
                }
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackSettingsCategoryFavoritePreview() {
    LeanbackTheme {
        LeanbackSettingsCategoryFavorite(
            modifier = Modifier.padding(20.dp),
            settingsViewModel = LeanbackSettingsViewModel().apply {
                iptvChannelFavoriteList = setOf("CCTV-1", "CCTV-2", "CCTV-3")
            }
        )
    }
}
