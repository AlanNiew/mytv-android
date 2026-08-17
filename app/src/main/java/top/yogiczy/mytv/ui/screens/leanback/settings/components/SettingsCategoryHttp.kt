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

@Composable
fun LeanbackSettingsCategoryHttp(
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
                headlineContent = stringResource(R.string.settings_http_retry_count),
                supportingContent = stringResource(R.string.settings_http_retry_desc),
                trailingContent = Constants.HTTP_RETRY_COUNT.toString(),
                locK = true,
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_http_retry_interval),
                supportingContent = stringResource(R.string.settings_http_retry_desc),
                trailingContent = Constants.HTTP_RETRY_INTERVAL.humanizeMs(),
                locK = true,
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_http_token),
                supportingContent = stringResource(R.string.settings_http_token_desc),
                trailingContent = SP.httpToken,
                locK = true,
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = stringResource(R.string.settings_http_trust_all_certificates),
                supportingContent = stringResource(R.string.settings_http_trust_all_certificates_desc),
                trailingContent = {
                    Switch(
                        checked = settingsViewModel.httpTrustAllCertificates,
                        onCheckedChange = null
                    )
                },
                onSelected = {
                    settingsViewModel.httpTrustAllCertificates =
                        !settingsViewModel.httpTrustAllCertificates
                },
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackSettingsCategoryHttpPreview() {
    SP.init(LocalContext.current)
    LeanbackTheme {
        LeanbackSettingsCategoryHttp(
            modifier = Modifier.padding(20.dp),
        )
    }
}
