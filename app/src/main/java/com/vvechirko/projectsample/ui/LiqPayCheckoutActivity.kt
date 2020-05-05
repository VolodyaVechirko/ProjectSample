package com.vvechirko.projectsample.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.vvechirko.projectsample.lollipopFixWebView
import com.vvechirko.projectsample.urlEncoded

class LiqPayCheckoutActivity : AppCompatActivity() {

    companion object {

        fun show(activity: Activity, data: String, signature: String, requestCode: Int) {
            val intent = Intent(activity, LiqPayCheckoutActivity::class.java).putExtra(
                "postData", "data=${data.urlEncoded}&signature=${signature.urlEncoded}"
            )
            activity.startActivityForResult(intent, requestCode)
        }
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
            override fun onLoadResource(view: WebView, url: String) {
                super.onLoadResource(view, url)
                if (url.contains("/api/mob/callback")) {
                    handleResult(Uri.parse(url))
                } else if (url.contains("/apiweb/checkout/cancel") || url.contains("/apiweb/checkout/home_redirect")) {
                    finish()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                val cn = intent.resolveActivity(packageManager)
                // redirect to privat24 application
                return if (cn != null && cn.packageName.contains("ua.privatbank.ap24")) {
                    startActivity(intent)
                    true
                } else {
                    // block app redirect when no p24 app installed
                    if (url.startsWith("privat24://rd")) true
                    else super.shouldOverrideUrlLoading(view, url)
                }
            }
        }

        CookieManager.getInstance().removeAllCookies {
            val postData = intent.getStringExtra("postData")!!
            webView.postUrl("https://www.liqpay.ua/api/3/checkout", postData.toByteArray())
        }
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
     * Handle liqpay success callback
     *
     * success sample
     * https://www.liqpay.ua/api/mob/callback?status=success&payment_id=1225360473&_order_id=3ce54bff-8feb-401c-a0de-4a83569a0c82
     */
    private fun handleResult(uri: Uri) {
        if (uri.getQueryParameter("status") == "success") {
            val data = Intent()
                .putExtra("payment_id", uri.getQueryParameter("payment_id"))
                .putExtra("order_id", uri.getQueryParameter("_order_id"))
            setResult(RESULT_OK, data)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}