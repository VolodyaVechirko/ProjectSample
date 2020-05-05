package com.vvechirko.projectsample.ui.address

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vvechirko.projectsample.*
import com.vvechirko.projectsample.data.api.PlaceComplete
import com.vvechirko.projectsample.data.api.PlaceDetails
import com.vvechirko.projectsample.data.model.Building
import com.vvechirko.projectsample.data.model.LocationPoint
import com.vvechirko.projectsample.utils.LocationManager
import kotlinx.android.synthetic.main.fragment_address.*
import org.koin.android.viewmodel.ext.android.viewModel

class AddressFragment : Fragment(), OnMapReadyCallback, LocationManager.Callback,
    AddressAdapter.Interaction {

    companion object {
        const val ARG_LOCATION = "ARG_LOCATION"

        fun newInstance(location: Building) = AddressFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_LOCATION, location)
            }
        }
    }

    private val viewModel: AddressViewModel by viewModel()
    private val location: Building by lazy {
        arguments!!.getParcelable(ARG_LOCATION)!!
    }

    private val adapter = AddressAdapter(this)
    private val locationManager = LocationManager()
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val behaviour = BottomSheetBehavior.from(addressSheet)
        addressSheet.doOnNextLayout {
            Log.d("doOnNextLayout", "peekHeight ${it.height}")
            // set peekHeight measured with addressContainer WRAP_CONTENT spec
            behaviour.peekHeight = it.height
            // set mapContainer height to fit above addressContainer
            mapContainer.layoutParams.height = view.height - it.height
            // adjust addressContainer height with MATCH_PARENT spec
            addressSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            view.post { view.requestLayout() }
        }

        handleSheetState(BottomSheetBehavior.STATE_COLLAPSED)
        behaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(view: View, offset: Float) {
                handleAnimation(offset)
            }

            override fun onStateChanged(view: View, state: Int) {
                handleSheetState(state)
            }
        })

        suggestList.adapter = adapter
        tvAddress.setOnClickListener {
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }

        etAddress.afterTextChanged {
            viewModel.searchPlaces(it.toString())
            tvAddress.text = it
        }

        etAddress.onEditorActionDone {
            behaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        btnDone.setOnClickListener {
            viewModel.checkAddressDistance(location)
        }

        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)

        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        viewModel.autocompleteData.observe(
            this,
            success = adapter::setItems,
            error = ::showError
        )

        viewModel.placeData.observe(
            this,
            success = this::selectPlace,
            error = ::showError
        )

        viewModel.myAddressData.observe(
            this,
            success = {
                adapter.setMyPlace(it)
                // set location to tvAddress and vm address
                tvAddress.text = it?.address
            },
            error = ::showError
        )

        viewModel.locationData.observe(
            this,
            success = {
//                navigate(CategoriesFragment.newInstance(it))
            },
            error = {
//                navigate(NoLocationFragment.newInstance(location.title))
            }
        )
    }

    override fun onCompletionSelected(p: PlaceComplete) {
        viewModel.fetchPlace(p.placeId)
    }

    override fun onMyPlaceSelected(p: PlaceDetails) {
        viewModel.placeData.updateValue(p)
    }

    private fun selectPlace(it: PlaceDetails) {
        etAddress.fillText(it.address)
        tvAddress.text = it.address // set vm address
        btnDone.isEnabled = it.hasStreetNumber // validate radius
        // move pin
        movePin(LatLng(it.lat.toDouble(), it.lng.toDouble()))
    }

    // LocationManager.Callback

    override val activity: Activity
        get() = requireActivity()

    override fun onLocationReceived(point: LocationPoint) {
        movePin(point.toLatLng())
        viewModel.fetchMyAddress(point)
    }

    override fun onDisplayWarning(w: LocationManager.Warning) {
        // Ignored
    }

    override fun onStart() {
        super.onStart()
        locationManager.tryRequestLocation(this)
    }

    override fun onStop() {
        super.onStop()
        locationManager.cancelRequest()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        locationManager.onActivityResult(requestCode, resultCode)
    }

    override fun onRequestPermissionsResult(code: Int, p: Array<String>, res: IntArray) {
        locationManager.onRequestPermissionsResult(code)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
//        map.isMyLocationEnabled = true
        drawLocation(map)

        val center = location.location.toLatLng()

        movePin(center)
        val myPin = map.addMarker(
            MarkerOptions()
                .position(center)
                .title("Your pin")
                .icon(mapIcon(R.drawable.ic_location_pin_black_24dp))
        )
        map.setOnCameraMoveListener {
            myPin.isVisible = false
            pinImage.visibility = View.VISIBLE
        }
        map.setOnCameraIdleListener {
            // TODO: fetch location to tvAddress and vm address
            myPin.position = map.cameraPosition.target
            myPin.isVisible = true
            pinImage.visibility = View.GONE
        }
    }

    private fun movePin(point: LatLng) {
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 11f))
    }

    private fun drawLocation(map: GoogleMap) {
        val position = location.location.toLatLng()
        map.addCircle(
            CircleOptions()
                .center(position)
                .radius(location.radius.toDouble())
                .fillColor(Color.parseColor("#448bc34a"))
                .strokeColor(Color.parseColor("#8bc34a"))
                .strokeWidth(8f)
        )

        map.addMarker(
            MarkerOptions()
                .position(position)
                .title(location.title)
                .icon(mapIcon(R.drawable.ic_location_pin_black_24dp))
        )
    }

    // TODO:
//    fun onBackPressed() {
//
//    }

    private fun handleSheetState(state: Int) {
        Log.d("handleSheetState", "state $state")
        when (state) {
            BottomSheetBehavior.STATE_COLLAPSED -> {
                adapter.setItems(listOf())
            }
            BottomSheetBehavior.STATE_EXPANDED -> {
                suggestList.visibility = View.VISIBLE
//                adapter.setItems(mock())
                etAddress.requestFocus()
                showKeyboard(etAddress)
                etAddress.animate().translationZ(16f).setDuration(100).start()
            }
            BottomSheetBehavior.STATE_SETTLING -> {
                hideKeyboard()
                etAddress.clearFocus()
                etAddress.translationZ = 0f
                suggestList.visibility = View.GONE
            }
            BottomSheetBehavior.STATE_DRAGGING -> {
                etAddress.translationZ = 0f
                suggestList.visibility = View.GONE
            }
        }
    }

    private fun handleAnimation(offset: Float) {
        Log.d("handleAnimation", "offset $offset")
        val translateY = -offset * tvAddress.top
        tvTitle.translationY = translateY
        tvTitle.alpha = 1f - offset

        tvAddress.translationY = translateY
        tvAddress.alpha = 1f - offset
        tvAddress.updateVisibility()

        etAddress.alpha = offset
        etAddress.updateVisibility()

        btnDone.translationY = -2 * translateY
        btnDone.alpha = 1f - offset
        btnDone.updateVisibility()
    }

    private fun View.updateVisibility() {
        visibility = if (alpha == 0f) View.GONE else View.VISIBLE
    }

    private fun mapIcon(resId: Int) = BitmapDescriptorFactory.fromBitmap(
        AppCompatResources.getDrawable(context!!, resId)!!.toBitmap()
    )

    private fun showError(s: Any?) = context?.toast(s)
}