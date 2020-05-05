package com.vvechirko.projectsample

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import java.net.URLEncoder

fun Context.toast(s: Any?) {
    Toast.makeText(this, s.toString(), Toast.LENGTH_SHORT).show()
}

fun Paint.setTextAppearance(context: Context, @StyleRes textAppearance: Int) {
    val ta = context.obtainStyledAttributes(textAppearance, R.styleable.TextAppearance)
    color = ta.getColor(R.styleable.TextAppearance_android_textColor, Color.GRAY)
    textSize = ta.getDimension(R.styleable.TextAppearance_android_textSize, 0.0f)

    val fontRes = ta.getResourceId(R.styleable.TextAppearance_fontFamily, 0)
    val fontFamily = ta.getString(R.styleable.TextAppearance_fontFamily)

    try {
        val font = ResourcesCompat.getFont(context, fontRes)
        if (font != null) {
            typeface = Typeface.create(font, Typeface.NORMAL)
        }
    } catch (e: Resources.NotFoundException) {
    } catch (e: UnsupportedOperationException) {
    } catch (e: Exception) {
        Log.d("TextAppearance", "Error loading font $fontFamily", e)
    }

    ta.recycle()
}

fun Context.isNetworkAvailable(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val cp = manager.getNetworkCapabilities(manager.activeNetwork)
        cp != null && cp.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && cp.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } else {
        manager.activeNetworkInfo?.isConnected ?: false
    }
}


inline val String.urlEncoded: String
    get() = URLEncoder.encode(this, "UTF-8")

/**
 * Fix for WebView Crash on Android 5-5.1 (API 21-22)
 * Resources$NotFoundException: String resource ID #0x2040002
 */
fun Context.lollipopFixWebView(): Context {
    return if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) {
        createConfigurationContext(Configuration())
    } else this
}




inline fun EditText.fillText(text: String?) {
    setText(text)
    if (text != null) {
        setSelection(text.length)
    }
}


inline fun EditText.onEditorActionDone(crossinline action: (tv: TextView) -> Unit) {
    setOnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            action(v)
        }
        // return false to hide keyboard
        false
    }
}

inline fun EditText.afterTextChanged(crossinline action: (s: Editable) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            action.invoke(s)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}


fun Fragment.hideKeyboard() {
    val imm = activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Find the currently focused view, so we can grab the correct window token from it
    var view = activity?.currentFocus
    // If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.showKeyboard(view: View) {
    val imm = activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, 0)
}



inline fun ImageView.load(url: String?) {
    Glide.with(this)
        .load(url)
        .centerCrop()
        .into(this)
}

inline fun ImageView.round(@DimenRes dimen: Int) {
    val radius = resources.getDimension(dimen)
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, radius)
        }
    }
    clipToOutline = true
}

inline fun ImageView.saturation(sat: Float) {
    colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
        setSaturation(sat)
    })
}


inline fun ViewGroup.inflate(@LayoutRes resId: Int, attach: Boolean = false) =
    LayoutInflater.from(this.context).inflate(resId, this, attach)