package com.vvechirko.projectsample.ui.building

import androidx.fragment.app.Fragment
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vvechirko.projectsample.R
import com.vvechirko.projectsample.data.model.Building
import com.vvechirko.projectsample.data.model.LocationPoint
import com.vvechirko.projectsample.toast
import com.vvechirko.projectsample.utils.LocationManager
import kotlinx.android.synthetic.main.fragment_buildings.*
import org.koin.android.viewmodel.ext.android.viewModel

class BuildingsFragment : Fragment(), BuildingsAdapter.Interaction, LocationManager.Callback {
    private val viewModel: BuildingsViewModel by viewModel()
    private val adapter = BuildingsAdapter(this)
    private val locationManager = LocationManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_buildings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = adapter
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun subscribeToViewModel() {
        viewModel.locations.observe(
            this,
            success = adapter::setItems,
            error = ::showError,
            loading = swipeRefreshLayout::setRefreshing
        )
    }

    override fun onStart() {
        super.onStart()
        viewModel.refresh()
//        locationManager.tryRequestLocation(this)
    }

    override fun onStop() {
        super.onStop()
//        locationManager.cancelRequest()
    }

    // LocationsAdapter.Interaction

    override fun onDeliveryClicked(item: Building) {
//        AppPrefs.orderType = OrderPlace.DELIVERY
//        navigate(AddressFragment.newInstance(item))
    }

    override fun onRestaurantClicked(item: Building) {
//        AppPrefs.orderType = OrderPlace.IN_RESTAURANT
//        navigate(CategoriesFragment.newInstance(item))
    }

    // LocationManager.Callback

    override val activity: Activity
        get() = requireActivity()

    override fun onLocationReceived(point: LocationPoint) {
        viewModel.onLocationReceived(point)
    }

    override fun onDisplayWarning(w: LocationManager.Warning) {
        // Ignored
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        locationManager.onActivityResult(requestCode, resultCode)
    }

    override fun onRequestPermissionsResult(code: Int, p: Array<String>, res: IntArray) {
        locationManager.onRequestPermissionsResult(code)
    }

    private fun showError(s: Any?) = context?.toast(s)
}