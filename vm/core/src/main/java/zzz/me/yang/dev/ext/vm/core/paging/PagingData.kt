package zzz.me.yang.dev.ext.vm.core.paging

import androidx.compose.runtime.IntState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import zzz.me.yang.dev.ext.entity.paging.PagingItem
import zzz.me.yang.dev.ext.entity.paging.PagingParam
import zzz.me.yang.dev.ext.entity.paging.PagingResponse
import zzz.me.yang.dev.ext.vm.core.work.PagingAsync
import zzz.me.yang.dev.ext.vm.core.work.WorkAsync

public class PagingData<T : PagingItem>(
    private val pageSize: Int = 20,
    private val pageStart: Int = 1,
    itemList: List<T> = emptyList(),
    workAsync: WorkAsync = WorkAsync.Uninitialized,
) {
    private companion object {
        const val INIT_IS_EMPTY_PAGE = true
        const val INIT_HAS_MORE = true
        const val INIT_TOTAL_SIZE = 0
        const val INIT_TOTAL_PAGE = 0
    }

    private val curPageState by lazy { mutableIntStateOf(pageStart) }

    public val curPage: Int by curPageState

    private val isEmptyPageState by lazy { mutableStateOf(INIT_IS_EMPTY_PAGE) }

    public val isEmptyPage: Boolean by isEmptyPageState

    private val hasMoreState by lazy { mutableStateOf(INIT_HAS_MORE) }

    public val hasMore: Boolean by hasMoreState

    private val _itemListState by lazy { mutableStateOf(itemList) }

    public val itemListState: State<List<T>> by lazy { _itemListState }

    public val itemList: List<T> by _itemListState

    private val workAsyncState by lazy { mutableStateOf(workAsync) }

    public val workAsync: WorkAsync by workAsyncState

    private val _totalSizeState by lazy { mutableIntStateOf(INIT_TOTAL_SIZE) }

    public val totalSizeState: IntState by lazy { _totalSizeState }

    public val totalSize: Int by _totalSizeState

    private val totalPageState by lazy { mutableIntStateOf(INIT_TOTAL_PAGE) }

    public val totalPage: Int by totalPageState

    public val pagingAsyncState: State<PagingAsync> by lazy {
        derivedStateOf { createPagingAsync() }
    }

    public val pagingAsync: PagingAsync by pagingAsyncState

    private fun createPagingAsync(): PagingAsync {
        val isFullPage = itemList.isEmpty()

        val workAsync = workAsync

        return when {
            workAsync is WorkAsync.Loading -> PagingAsync.Loading(isFullPage)
            workAsync is WorkAsync.Fail -> PagingAsync.Fail(isFullPage, workAsync.error)
            workAsync is WorkAsync.Success && isFullPage -> PagingAsync.EmptyPage
            workAsync is WorkAsync.Success -> PagingAsync.Success(hasMore)
            else -> PagingAsync.Uninitialized
        }
    }

    /**
     * 初始化
     */
    public fun initialize() {
        curPageState.intValue = pageStart
        isEmptyPageState.value = INIT_IS_EMPTY_PAGE
        hasMoreState.value = INIT_HAS_MORE
        _itemListState.value = emptyList()
        workAsyncState.value = workAsync
        _totalSizeState.intValue = INIT_TOTAL_SIZE
        totalPageState.intValue = INIT_TOTAL_PAGE
    }

    public fun updateList(block: (List<T>) -> List<T>) {
        _itemListState.value = block(itemList)
    }

    internal fun createRefreshParam(): PagingParam<T> {
        return PagingParam(
            loadPage = pageStart,
            pageSize = pageSize,
            oldList = emptyList(),
        )
    }

    internal fun createNextParam(): PagingParam<T> {
        return PagingParam(
            loadPage = curPage + 1,
            pageSize = pageSize,
            oldList = itemList,
        )
    }

    internal fun updateWorkAsync(workAsync: WorkAsync) {
        workAsyncState.value = workAsync
    }

    internal fun updateByResponse(response: PagingResponse<T>) {
        curPageState.intValue = response.curPage
        isEmptyPageState.value = response.curPage == pageStart && response.list.isEmpty()
        hasMoreState.value = response.hasMore
        _itemListState.value = response.list
        workAsyncState.value = WorkAsync.Success
        _totalSizeState.intValue = response.totalSize
        totalPageState.intValue = response.totalPage
    }
}
