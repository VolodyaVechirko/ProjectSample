package com.vvechirko.projectsample.data

import com.vvechirko.projectsample.App
import com.vvechirko.projectsample.isNetworkAvailable
import org.json.JSONObject
import retrofit2.HttpException
import java.net.SocketTimeoutException

// ERROR CODES
const val LIQPAY_NOT_SUCCESS = "error.liqpay.not-success"
const val CARD_NEED_3DS = "error.portmone.card-needs-3ds"
const val APP_UPDATE = "error.app.app-version"

object ErrorParser {
    fun parse(t: Throwable): Error {
        return when {
            t is Error -> t
            !App.appContext().isNetworkAvailable() -> Error.General("No internet connection")
            t is SocketTimeoutException -> Error.General("Server error")
            t is HttpException -> if (t.code() >= 500) Error.General("Server error") else try {
                val s = t.response()!!.errorBody()!!.string()
                with(JSONObject(s)) {
                    Error.Api(
                        getString("title"),
                        getInt("status"),
                        getString("code"),
                        optString("payload")
                    )
                }
            } catch (ignored: Throwable) {
                Error.General(t.message())
            }
            else -> Error.General(t.message ?: t.toString())
        }
    }
}

sealed class Error(val title: String): Exception(title) {
    class General(title: String) : Error(title)
    class Api(
        title: String,
        val status: Int,
        val code: String,
        // payload object as json string
        val payload: String? = null
    ) : Error(title)

    override fun toString(): String {
        return title
    }
}