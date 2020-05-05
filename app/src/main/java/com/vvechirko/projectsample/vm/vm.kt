package com.vvechirko.projectsample.vm

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vvechirko.projectsample.data.Error
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

typealias Data<T> = (data: T) -> Unit
typealias Error = (errors: Error) -> Unit
typealias Loading = (b: Boolean) -> Unit

const val DEFAULT_PAGE = 1

sealed class Response<T> {
    class Success<T>(val data: T) : Response<T>()
    class Error<T>(val error: Throwable) : Response<T>()
    class Loading<T>() : Response<T>()
}

class Paginated<T>(
    var page: Int = 0,
    val perPage: Int,
//    val total: Int,
    val data: List<T>
)


fun lifecycleOwner(owner: LifecycleOwner): LifecycleOwner {
    return if (owner is Fragment && owner.view != null) owner.viewLifecycleOwner else owner
}


class ActionData(isResettable: Boolean = true) : ResponseData<Unit>(isResettable)


@Suppress("FunctionName")
inline fun <T> SuccessObserver(crossinline onSuccess: (T) -> Unit) = Observer<Response<T>> {
    if (it is Response.Success) {
        onSuccess(it.data)
    }
}


inline fun <reified T : ViewModel> Fragment.targetViewModel() = lazy {
    ViewModelProvider(targetFragment!!).get(T::class.java)
}


/**
 * Base [ViewModel] with [CoroutineScope]
 */
open class BaseViewModel : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
        }

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancel()
    }
}