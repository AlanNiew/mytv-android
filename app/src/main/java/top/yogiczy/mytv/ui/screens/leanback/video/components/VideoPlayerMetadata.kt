package top.yogiczy.mytv.ui.screens.leanback.video.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.yogiczy.mytv.R
import top.yogiczy.mytv.ui.screens.leanback.video.player.LeanbackVideoPlayer
import top.yogiczy.mytv.ui.theme.LeanbackTheme

@Composable
fun LeanbackVideoPlayerMetadata(
    modifier: Modifier = Modifier,
    metadata: LeanbackVideoPlayer.Metadata,
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.labelMedium,
        LocalContentColor provides MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = modifier
                .background(
                    MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                    MaterialTheme.shapes.extraSmall,
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column {
                Text(stringResource(R.string.video_metadata_video), style = MaterialTheme.typography.bodyMedium)
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(stringResource(R.string.video_metadata_codec, metadata.videoMimeType))
                    Text(stringResource(R.string.video_metadata_decoder, metadata.videoDecoder))
                    Text(stringResource(R.string.video_metadata_resolution, metadata.videoWidth, metadata.videoHeight))
                    Text(stringResource(R.string.video_metadata_color, metadata.videoColor))
                    Text(stringResource(R.string.video_metadata_frame_rate, metadata.videoFrameRate))
                    Text(stringResource(R.string.video_metadata_bitrate, metadata.videoBitrate / 1024))
                }
            }

            Column {
                Text(stringResource(R.string.video_metadata_audio), style = MaterialTheme.typography.bodyMedium)
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(stringResource(R.string.video_metadata_codec, metadata.audioMimeType))
                    Text(stringResource(R.string.video_metadata_decoder, metadata.audioDecoder))
                    Text(stringResource(R.string.video_metadata_channels, metadata.audioChannels))
                    Text(stringResource(R.string.video_metadata_sample_rate, metadata.audioSampleRate))
                }
            }
        }
    }
}

@Preview
@Composable
private fun LeanbackVideoMetadataPreview() {
    LeanbackTheme {
        LeanbackVideoPlayerMetadata(
            metadata = LeanbackVideoPlayer.Metadata(
                videoWidth = 1920,
                videoHeight = 1080,
                videoMimeType = "video/hevc",
                videoColor = "BT2020/Limited range/HLG/8/8",
                videoFrameRate = 25.0f,
                videoBitrate = 10605096,
                videoDecoder = "c2.goldfish.h264.decoder",

                audioMimeType = "audio/mp4a-latm",
                audioChannels = 2,
                audioSampleRate = 32000,
                audioDecoder = "c2.android.aac.decoder",
            )
        )
    }
}