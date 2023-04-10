package com.yihsiang.pullrefresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun PullRefresh(
    state: PullRefreshState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    refreshTriggerOffset: Dp = 60.dp,
    refreshingMaxOffset: Dp = 120.dp,
    indicator: @Composable (
        state: PullRefreshState,
        refreshTriggerOffset: Dp,
        refreshingMaxOffset: Dp
    ) -> Unit = { s, trigger, max ->
        PullRefreshLottieIndicator(state = s, refreshTriggerOffset = trigger, refreshingMaxOffset = max)
    },
    clipIndicatorToPadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val refreshingMaxOffsetPx = with(LocalDensity.current) { refreshingMaxOffset.toPx() }
    val refreshTriggerOffsetPx = with(LocalDensity.current) { refreshTriggerOffset.toPx() }

    LaunchedEffect(Unit) {
        snapshotFlow { !state.isPullInProgress to state.isRefreshing }
            .distinctUntilChanged()
            .filter { it.first }
            .map { it.second }
            .collectLatest { isRefreshing ->
                val offset = if (isRefreshing) {
                    refreshingMaxOffsetPx
                } else {
                    // 刷新完成回彈
                    0f
                }
                state.animateOffsetTo(offset)
            }
    }

    val nestedScrollConnection = remember(state) {
        PullRefreshNestedScrollConnection(
            enabled = enabled,
            state = state,
            coroutineScope = coroutineScope,
            refreshingMaxOffsetPx = refreshingMaxOffsetPx,
            refreshTriggerOffsetPx = refreshTriggerOffsetPx
        ) {
            onRefresh()
        }
    }

    Box(modifier.nestedScroll(connection = nestedScrollConnection)) {
        Box(modifier = Modifier
            .offset {
                IntOffset(0, state.contentOffset.toInt())
            }
        ) {
            content()
        }
        Box(
            Modifier
                // If we're not clipping to the padding, we use clipToBounds() before the padding()
                // modifier.
                .let { if (!clipIndicatorToPadding) it.clipToBounds() else it }
                .matchParentSize()
                // Else, if we're are clipping to the padding, we use clipToBounds() after
                // the padding() modifier.
                .let { if (clipIndicatorToPadding) it.clipToBounds() else it }
        ) {
            Box(Modifier.align(Alignment.TopCenter)) {
                indicator(state, refreshTriggerOffset, refreshingMaxOffset)
            }
        }
    }
}