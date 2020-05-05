package com.vvechirko.projectsample.gpay

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.vvechirko.projectsample.data.CARD_NEED_3DS
import com.vvechirko.projectsample.data.Error
import com.vvechirko.projectsample.data.ErrorParser
import com.vvechirko.projectsample.data.model.Need3DS
import com.vvechirko.projectsample.vm.Response
import com.vvechirko.projectsample.vm.ResponseData
import org.json.JSONObject

class GPayData : ResponseData<Unit>(true) {

    fun observe(
        owner: LifecycleOwner,
        success: () -> Unit = { },
        need3ds: (n3ds: Need3DS) -> Unit = { },
        error: (e: Error) -> Unit = { },
        loading: (b: Boolean) -> Unit = { }
    ) {
        val viewLifecycleOwner = (owner as? Fragment)?.viewLifecycleOwner ?: owner
        super.observe(viewLifecycleOwner, Observer { t ->
            when (t) {
                is Response.Success -> {
                    loading.invoke(false)
                    success.invoke()
                    resetIfNeed()
                }
                is Response.Error -> {
                    loading.invoke(false)
                    val e = ErrorParser.parse(t.error)
                    error.invoke(e)

                    // need 3d secure
                    if (e is Error.Api && e.code == CARD_NEED_3DS) {
                        val it = with(JSONObject(e.payload!!)) {
                            Need3DS(
                                getString("shop_bill_id"),
                                getString("action"),
                                getString("pareq"),
                                getString("md")
                            )
                        }
                        need3ds.invoke(it)
                    }
                    resetIfNeed()
                }
                is Response.Loading -> loading.invoke(true)
            }
        })
    }
}