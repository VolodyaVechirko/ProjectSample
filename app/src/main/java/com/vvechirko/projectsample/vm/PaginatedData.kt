package com.vvechirko.projectsample.vm

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.vvechirko.projectsample.data.ErrorParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaginatedData<T> : LiveData<Response<List<T>>>() {

    private val allItems: MutableList<T> = mutableListOf()
    var page: Int = 0
        private set

    val data: List<T>
        get() = allItems

    var hasNextPage: Boolean = true
        private set

    val nextPage
        get() = page + 1

    fun observe(
        owner: LifecycleOwner,
        success: Data<List<T>> = { },
        error: Error = { },
        loading: Loading = { }
    ) {
        super.observe(lifecycleOwner(owner), Observer { t ->
            when (t) {
                is Response.Success -> {
                    loading.invoke(false)
                    success.invoke(t.data)
                }
                is Response.Error -> {
                    loading.invoke(false)
                    error.invoke(ErrorParser.parse(t.error))
                }
                is Response.Loading -> loading.invoke(true)
            }
        })
    }

    suspend fun from(action: suspend () -> Paginated<T>) = withContext(Dispatchers.Main) {
        value = Response.Loading()

        try {
            val result = withContext(Dispatchers.IO) { action() }
            if (result.page == DEFAULT_PAGE) allItems.clear()
            page = result.page
            allItems.addAll(result.data)

            hasNextPage = result.data.size == result.perPage

            value = Response.Success(allItems)
        } catch (canceled: CancellationException) {
            // Canceled by user
        } catch (error: Throwable) {
            value = Response.Error(error)
        }
    }
}