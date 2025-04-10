package com.aralhub.network.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.aralhub.network.api.DriverNetworkApi
import com.aralhub.network.models.NetworkResult
import com.aralhub.network.models.ride.RideHistoryNetwork
import com.aralhub.network.utils.ex.NetworkEx.safeRequestServerResponse

class HistoryPagingSource(private val api: DriverNetworkApi) :
    PagingSource<Int, RideHistoryNetwork>() {

    companion object {
        private const val STARTING_PAGE_INDEX = 1
    }

    override fun getRefreshKey(state: PagingState<Int, RideHistoryNetwork>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RideHistoryNetwork> {
        val position = params.key ?: STARTING_PAGE_INDEX
        return when (val result = api.getRideHistory(position, params.loadSize).safeRequestServerResponse()) {
            is NetworkResult.Error -> LoadResult.Error(Exception(result.toString()))
            is NetworkResult.Success -> {
                LoadResult.Page(
                    data = result.data,
                    prevKey = if (position == STARTING_PAGE_INDEX) null else -1,
                    nextKey = if (result.data.isEmpty()) null else position + 1
                )
            }

        }
    }
}