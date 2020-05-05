package com.vvechirko.projectsample.ui.address

import android.util.Log
import com.vvechirko.projectsample.data.Error
import com.vvechirko.projectsample.data.api.PlaceComplete
import com.vvechirko.projectsample.data.api.PlaceDetails
import com.vvechirko.projectsample.data.model.Building
import com.vvechirko.projectsample.data.model.DeliveryAddress
import com.vvechirko.projectsample.data.model.LocationPoint
import com.vvechirko.projectsample.domain.PlacesInteractor
import com.vvechirko.projectsample.vm.BaseViewModel
import com.vvechirko.projectsample.vm.ResponseData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddressViewModel(
    private val places: PlacesInteractor
) : BaseViewModel() {

    val autocompleteData = ResponseData<List<PlaceComplete>>()
    val placeData = ResponseData<PlaceDetails>()
    val myAddressData = ResponseData<PlaceDetails?>()
    private var autocompleteJob: Job? = null

    val locationData = ResponseData<Building>(true)

    fun checkAddressDistance(l: Building) {
        val p = placeData.data!!
        val address = DeliveryAddress(p.address, p.lat, p.lng)
//        AppPrefs.deliveryAddress = address

        launch {
            locationData.from {
                val distance = l.location.distanceTo(address.toLocationPoint())
                if (distance > l.radius) {
                    throw Error.General("Address is out of radius")
                } else {
                    l
                }
            }
        }
    }

    fun fetchPlace(placeId: String) {
        launch {
            placeData.from {
                places.placeDetails(placeId)
            }
        }
    }

    fun searchPlaces(input: String) {
        autocompleteJob?.cancel()
        autocompleteJob = launch {
            val isNotEmpty = input.isNotEmpty()
            if (isNotEmpty) delay(300)
            autocompleteData.from {
                places.autoComplete(input)
//                if (isNotEmpty) places.autoComplete(input) else places.getRecent()
            }
        }
    }

    fun fetchMyAddress(it: LocationPoint) {
        Log.i("myLocation", it.toString())
        launch {
            myAddressData.from {
                places.geocodeLocation(it)
            }
        }
    }
}