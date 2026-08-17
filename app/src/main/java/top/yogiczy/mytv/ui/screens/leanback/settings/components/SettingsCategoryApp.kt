package top.yogiczy.mytv.ui.screens.leanback.settings.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import top.yogiczy.mytv.R
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsViewModel
import top.yogiczy.mytv.ui.screens.leanback.update.LeanBackUpdateViewModel
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.SP

@Composable
fun LeanbackSettingsCategoryApp(
    modifier: Modifier = Modifier,
    settingsViewModel: LeanbackSettingsViewModel = viewModel(),
    updateViewModel: LeanBackUpdateViewModel = viewModel(),
) {

    TvLazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
    ) {
        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_app_boot),
                supportingContent = stringResource(R.string.settings_app_boot_desc),
                trailingContent = {
                    Switch(checked = settingsViewModel.appBootLaunch, onCheckedChange = null)
                },
                onSelected = {
                    settingsViewModel.appBootLaunch = !settingsViewModel.appBootLaunch
                },
            )
        }

        item {
            val context = LocalContext.current

            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_app_display_mode),
                supportingContent = stringResource(R.string.settings_app_display_mode_desc),
                trailingContent = when (settingsViewModel.appDeviceDisplayType) {
                    SP.AppDeviceDisplayType.LEANBACK -> stringResource(R.string.settings_app_display_mode_tv)
                    SP.AppDeviceDisplayType.PAD -> stringResource(R.string.settings_app_display_mode_pad)
                    SP.AppDeviceDisplayType.MOBILE -> stringResource(R.string.settings_app_display_mode_mobile)
                },
                onSelected = {
                    Toast.makeText(context, context.getString(R.string.settings_app_display_mode_not_open), Toast.LENGTH_SHORT).show()
//                    settingsViewModel.appDeviceDisplayType = SP.AppDeviceDisplayType.entries[
//                        (settingsViewModel.appDeviceDisplayType.ordinal + 1) % SP.AppDeviceDisplayType.entries.size
//                    ]
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_app_update),
                supportingContent = stringResource(R.string.settings_app_update_latest, updateViewModel.latestRelease.version),
                trailingContent = if (updateViewModel.isUpdateAvailable) stringResource(R.string.settings_app_update_found) else stringResource(R.string.settings_app_update_none),
                onSelected = {
                    if (updateViewModel.isUpdateAvailable)
                        updateViewModel.showDialog = true
                },
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackSettingsCategoryAppPreview() {
    SP.init(LocalContext.current)
    LeanbackTheme {
        LeanbackSettingsCategoryApp(
            modifier = Modifier.padding(20.dp),
            settingsViewModel = LeanbackSettingsViewModel(),
        )
    }
}
