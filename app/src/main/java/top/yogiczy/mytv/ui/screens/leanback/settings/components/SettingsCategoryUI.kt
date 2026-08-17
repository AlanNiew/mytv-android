package top.yogiczy.mytv.ui.screens.leanback.settings.components

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
import top.yogiczy.mytv.data.utils.Constants
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsViewModel
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.SP
import top.yogiczy.mytv.utils.humanizeMs
import java.text.DecimalFormat

@Composable
fun LeanbackSettingsCategoryUI(
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
                headlineContent = stringResource(R.string.settings_ui_programme_progress),
                supportingContent = stringResource(R.string.settings_ui_programme_progress_desc),
                trailingContent = {
                    Switch(
                        checked = settingsViewModel.uiShowEpgProgrammeProgress,
                        onCheckedChange = null
                    )
                },
                onSelected = {
                    settingsViewModel.uiShowEpgProgrammeProgress =
                        !settingsViewModel.uiShowEpgProgrammeProgress
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_ui_classic_panel),
                supportingContent = stringResource(R.string.settings_ui_classic_panel_desc),
                trailingContent = {
                    Switch(
                        checked = settingsViewModel.uiUseClassicPanelScreen,
                        onCheckedChange = null
                    )
                },
                onSelected = {
                    settingsViewModel.uiUseClassicPanelScreen =
                        !settingsViewModel.uiUseClassicPanelScreen
                },
            )
        }

        item {
            val timeShowRangeSeconds = Constants.UI_TIME_SHOW_RANGE / 1000

            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_ui_time_display),
                supportingContent = when (settingsViewModel.uiTimeShowMode) {
                    SP.UiTimeShowMode.HIDDEN -> stringResource(R.string.settings_ui_time_display_hidden)
                    SP.UiTimeShowMode.ALWAYS -> stringResource(R.string.settings_ui_time_display_always)
                    SP.UiTimeShowMode.EVERY_HOUR -> stringResource(
                        R.string.settings_ui_time_display_every_hour,
                        timeShowRangeSeconds,
                    )
                    SP.UiTimeShowMode.HALF_HOUR -> stringResource(
                        R.string.settings_ui_time_display_half_hour,
                        timeShowRangeSeconds,
                    )
                },
                trailingContent = when (settingsViewModel.uiTimeShowMode) {
                    SP.UiTimeShowMode.HIDDEN -> stringResource(R.string.settings_ui_time_display_hidden_value)
                    SP.UiTimeShowMode.ALWAYS -> stringResource(R.string.settings_ui_time_display_always_value)
                    SP.UiTimeShowMode.EVERY_HOUR -> stringResource(R.string.settings_ui_time_display_every_hour_value)
                    SP.UiTimeShowMode.HALF_HOUR -> stringResource(R.string.settings_ui_time_display_half_hour_value)
                },
                onSelected = {
                    settingsViewModel.uiTimeShowMode =
                        SP.UiTimeShowMode.entries.let { it[(it.indexOf(settingsViewModel.uiTimeShowMode) + 1) % it.size] }
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_ui_pip),
                trailingContent = {
                    Switch(
                        checked = settingsViewModel.uiPipMode,
                        onCheckedChange = null
                    )
                },
                onSelected = {
                    settingsViewModel.uiPipMode =
                        !settingsViewModel.uiPipMode
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_ui_auto_close),
                supportingContent = stringResource(R.string.settings_ui_auto_close_desc),
                trailingContent = Constants.UI_SCREEN_AUTO_CLOSE_DELAY.humanizeMs(),
                locK = true,
            )
        }

        item {
            val defaultScale = 1f
            val minScale = 1f
            val maxScale = 2f
            val stepScale = 0.1f

            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_ui_density_scale),
                supportingContent = stringResource(R.string.settings_ui_density_scale_desc),
                trailingContent = stringResource(
                    R.string.settings_ui_scale_value,
                    DecimalFormat("#.#").format(settingsViewModel.uiDensityScaleRatio)
                ),
                onSelected = {
                    if (settingsViewModel.uiDensityScaleRatio >= maxScale) {
                        settingsViewModel.uiDensityScaleRatio = minScale
                    } else {
                        settingsViewModel.uiDensityScaleRatio =
                            (settingsViewModel.uiDensityScaleRatio + stepScale).coerceIn(
                                minScale, maxScale
                            )
                    }
                },
                onLongSelected = {
                    settingsViewModel.uiDensityScaleRatio = defaultScale
                },
            )
        }

        item {
            val defaultScale = 1f
            val minScale = 1f
            val maxScale = 2f
            val stepScale = 0.1f

            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_ui_font_scale),
                supportingContent = stringResource(R.string.settings_ui_font_scale_desc),
                trailingContent = stringResource(
                    R.string.settings_ui_scale_value,
                    DecimalFormat("#.#").format(settingsViewModel.uiFontScaleRatio)
                ),
                onSelected = {
                    if (settingsViewModel.uiFontScaleRatio >= maxScale) {
                        settingsViewModel.uiFontScaleRatio = minScale
                    } else {
                        settingsViewModel.uiFontScaleRatio =
                            (settingsViewModel.uiFontScaleRatio + stepScale).coerceIn(
                                minScale, maxScale
                            )
                    }
                },
                onLongSelected = {
                    settingsViewModel.uiFontScaleRatio = defaultScale
                },
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackSettingsCategoryUIPreview() {
    SP.init(LocalContext.current)
    LeanbackTheme {
        LeanbackSettingsCategoryUI(
            modifier = Modifier.padding(20.dp),
            settingsViewModel = LeanbackSettingsViewModel(),
        )
    }
}