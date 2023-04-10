package com.yihsiang.pullrefresh

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*

@Stable
class PullRefreshState(isRefreshing: Boolean) {
    private val _contentOffset = Animatable(0f)

    var isRefreshing by mutableStateOf(isRefreshing)

    var isPullInProgress: Boolean by mutableStateOf(false)
        internal set

    val contentOffset: Float get() = _contentOffset.value

    suspend fun animateOffsetTo(offset: Float) {
        _contentOffset.animateTo(offset)
    }

    suspend fun dispatchScrollDelta(delta: Float) {
        _contentOffset.snapTo(_contentOffset.value + delta)
    }
}

@Composable
fun rememberPullRefreshState(
    isRefreshing: Boolean
): PullRefreshState = remember {
    PullRefreshState(isRefreshing)
}.apply {
    this.isRefreshing = isRefreshing
}