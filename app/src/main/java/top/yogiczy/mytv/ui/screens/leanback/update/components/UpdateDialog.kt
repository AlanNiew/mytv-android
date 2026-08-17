package top.yogiczy.mytv.ui.screens.leanback.update.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import top.yogiczy.mytv.R
import top.yogiczy.mytv.data.entities.GitRelease
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.handleLeanbackKeyEvents

@Composable
fun LeanbackUpdateDialog(
    modifier: Modifier = Modifier,
    showDialogProvider: () -> Boolean = { false },
    onDismissRequest: () -> Unit = {},
    releaseProvider: () -> GitRelease = { GitRelease() },
    onUpdateAndInstall: () -> Unit = {},
) {
    if (showDialogProvider()) {
        val release = releaseProvider()
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            confirmButton = {
                androidx.tv.material3.Button(
                    onClick = {},
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .handleLeanbackKeyEvents(
                            onSelect = onUpdateAndInstall,
                        ),
                ) {
                    androidx.tv.material3.Text(text = stringResource(R.string.update_now))
                }
            },
            dismissButton = {
                androidx.tv.material3.Button(
                    onClick = {},
                    modifier = Modifier.handleLeanbackKeyEvents(
                        onSelect = onDismissRequest,
                    ),
                ) {
                    androidx.tv.material3.Text(text = stringResource(R.string.update_ignore))
                }
            },
            title = {
                Text(text = stringResource(R.string.update_new_version_title, release.version))
            },
            text = {
                LazyColumn {
                    item {
                        Text(text = release.description)
                    }
                }
            }
        )
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun LeanbackUpdateDialogPreview() {
    LeanbackTheme {
        LeanbackUpdateDialog(
            showDialogProvider = { true },
            releaseProvider = {
                GitRelease(
                    version = "1.0.0",
                    description = "版本更新日志".repeat(100),
                )
            }
        )
    }
}