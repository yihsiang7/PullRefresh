package com.yihsiang.pullrefresh

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class PullRefreshNestedScrollConnection(
    private val enabled: Boolean,
    private val state: PullRefreshState,
    private val coroutineScope: CoroutineScope,
    private val refreshingMaxOffsetPx: Float,
    private val refreshTriggerOffsetPx: Float,
    private val onRefresh: () -> Unit,
) : NestedScrollConnection {
    // callback 順序: onPreScroll -> onPostScroll -> onPreFling

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        Log.i("TAG", "onPreScroll: ")
        return when {
            !enabled -> Offset.Zero
            state.isRefreshing -> Offset(0f, available.y)
            // 上拉
            source == NestedScrollSource.Drag && available.y < 0 -> dragUp(available)
            else -> Offset.Zero
        }
    }

    private fun dragUp(available: Offset): Offset {
        state.isPullInProgress = true

        val newOffset = (available.y * DRAG_MULTIPLIER + state.contentOffset)
            .coerceAtLeast(0f)
        val dragConsumed = newOffset - state.contentOffset

        return if (dragConsumed.absoluteValue >= 0.5f) {
            coroutineScope.launch {
                state.dispatchScrollDelta(dragConsumed)
            }
            Offset(x = 0f, y = available.y)
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        Log.i("TAG", "onPostScroll: ")
        return  when {
            !enabled -> Offset.Zero
            state.isRefreshing -> Offset.Zero
            // 下拉
            source == NestedScrollSource.Drag && available.y > 0 -> dragDown(available)
            else -> Offset.Zero
        }
    }

    private fun dragDown(available: Offset): Offset {
        state.isPullInProgress = true

        val delta = available.y * DRAG_MULTIPLIER
        val newOffset = delta + state.contentOffset
        when {
            newOffset < refreshingMaxOffsetPx -> {
                coroutineScope.launch {
                    state.dispatchScrollDelta(delta)
                }
            }
            state.contentOffset != refreshingMaxOffsetPx -> {
                coroutineScope.launch {
                    state.dispatchScrollDelta(refreshingMaxOffsetPx - state.contentOffset)
                }
            }
        }
//        if (newOffset < refreshingMaxOffsetPx) {
//            coroutineScope.launch {
//                state.dispatchScrollDelta(delta)
//            }
//        }
        return Offset(x = 0f, y = available.y)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val triggerRefreshRange = refreshTriggerOffsetPx..refreshingMaxOffsetPx
        if (!state.isRefreshing &&
            state.contentOffset in triggerRefreshRange /*是否下拉到觸發刷新區域*/) {
            onRefresh()
        }

        // onPreFling 代表用戶已放開，所以重置狀態
        state.isPullInProgress = false

        return when {
            // 當在下拉中、刷新中時將此加速度值用掉
            state.contentOffset != 0f -> available
            else -> Velocity.Zero
        }
    }

    companion object {
        private const val DRAG_MULTIPLIER = 0.5f
    }
}