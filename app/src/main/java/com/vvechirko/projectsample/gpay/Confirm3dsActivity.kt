package com.vvechirko.projectsample.gpay

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.vvechirko.projectsample.BuildConfig
import com.vvechirko.projectsample.data.model.Need3DS
import com.vvechirko.projectsample.lollipopFixWebView
import com.vvechirko.projectsample.urlEncoded

class Confirm3dsActivity : AppCompatActivity() {

    companion object {
        const val RESULT_ERROR = 1

        fun show(activity: Activity, id: String, data: Need3DS, code: Int) {
            val intent = Intent(activity, Confirm3dsActivity::class.java)
                .putExtra("orderId", id)
                .putExtra("data", data)
            activity.startActivityForResult(intent, code)
        }

        fun payRequestResult(resultCode: Int, data: Intent?): Result {
            when (resultCode) {
                RESULT_OK -> data?.let {
                    return Result.Success(
                        it.getStringExtra("order_id")
                    )
                }
                RESULT_ERROR -> data?.let {
                    return Result.Error(
                        it.getStringExtra("order_id"),
                        it.getStringExtra("errorMessage")
                    )
                }
            }
            return Result.Empty
        }
    }

    private fun termUrl(id: String, billId: String): String {
        return BuildConfig.BASE_API + "orders/$id/payment/3dsecure?shopBillId=$billId"
    }

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        webView = WebView(lollipopFixWebView())
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        setContentView(webView)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return if (url.contains("custom://gpay")) {
                    handleResult(Uri.parse(url))
                    true
                } else {
                    super.shouldOverrideUrlLoading(view, url)
                }
            }
        }

        val orderId = intent.getStringExtra("orderId")!!
        val data = intent.getParcelableExtra<Need3DS>("data")!!

        val params = "MD=${data.md.urlEncoded}&PaReq=${data.pareq.urlEncoded}" +
                "&TermUrl=${termUrl(orderId, data.shopBillId).urlEncoded}"
        webView.postUrl(data.action, params.toByteArray())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Handle 3ds confirm result to custom://gpay
     *
     * success sample
     * custom://gpay/success/?orderId=157f0acd-149e-470a-af38-0ef5423f45ad
     *
     * error sample
     * custom://gpay/error/?orderId=157f0acd-149e-470a-af38-0ef5423f45ad&errorCode=409&errorMessage=ErrorMsg
     */
    private fun handleResult(uri: Uri) {
        val path = uri.path ?: return
        if (path.contains("success")) {
            val data = Intent()
                .putExtra("order_id", uri.getQueryParameter("orderId"))
            setResult(RESULT_OK, data)
            finish()
        } else if (path.contains("error")) {
            val data = Intent()
                .putExtra("order_id", uri.getQueryParameter("orderId"))
                .putExtra("errorCode", uri.getQueryParameter("errorCode"))
                .putExtra("errorMessage", uri.getQueryParameter("errorMessage"))
            setResult(RESULT_ERROR, data)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }

    sealed class Result {
        class Success(val orderId: String) : Result()
        class Error(val orderId: String, val message: String) : Result()
        object Empty : Result()
    }
}