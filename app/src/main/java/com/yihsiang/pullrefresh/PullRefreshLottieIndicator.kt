package com.yihsiang.pullrefresh

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PullRefreshLottieIndicator(
    state: PullRefreshState,
    refreshTriggerOffset: Dp,
    refreshingMaxOffset: Dp,
) {
    val refreshTriggerOffsetPx = with(LocalDensity.current) { refreshTriggerOffset.toPx() }
    val refreshingMaxOffsetPx = with(LocalDensity.current) { refreshingMaxOffset.toPx() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(refreshingMaxOffset)
            .graphicsLayer {
                translationY = -refreshingMaxOffsetPx + state.contentOffset
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(if (state.isRefreshing) Color.Red else Color.Green)
        )
        BasicText(
            text = when {
                state.isPullInProgress && state.contentOffset < refreshTriggerOffsetPx -> "下拉更新"
                state.isPullInProgress && state.contentOffset < refreshingMaxOffsetPx -> "放開手立即更新"
                state.isRefreshing -> "更新中..."
                else -> "下拉更新"
            },
        )
    }
}