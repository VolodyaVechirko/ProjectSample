package com.vvechirko.projectsample.ui.building

import com.vvechirko.projectsample.data.model.Building
import com.vvechirko.projectsample.data.model.LocationPoint
import com.vvechirko.projectsample.domain.BuildingsInteractor
import com.vvechirko.projectsample.vm.BaseViewModel
import com.vvechirko.projectsample.vm.ResponseData
import kotlinx.coroutines.launch

class BuildingsViewModel(
    private val interactor: BuildingsInteractor
) : BaseViewModel() {

    val locations = ResponseData<List<Building>>()
    var myPoint: LocationPoint? = null

    fun refresh() {
        launch {
            locations.from {
                calculateAndSort(interactor.get())
            }
        }

//        launch {
//            AppPrefs.deliveryMinPrice = interactor.getMinDeliveryPrice()
//        }
    }

    fun onLocationReceived(point: LocationPoint) {
        myPoint = point
        val current = locations.data ?: return
        launch {
            locations.from {
                calculateAndSort(current)
            }
        }
    }

    private fun calculateAndSort(l: List<Building>) = l.map {
        it.distance = myPoint?.distanceTo(it.location)
        it
    }.sortedBy { it.distance }
}