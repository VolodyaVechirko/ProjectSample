package com.vvechirko.projectsample.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vvechirko.projectsample.R
import com.vvechirko.projectsample.toast
import kotlinx.android.synthetic.main.activity_widget_sample.*

class WidgetSampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_sample)

        switchView.selectInternal(0)
        switchView.onSelected = {
            toast("Selected $it")
        }
    }
}
