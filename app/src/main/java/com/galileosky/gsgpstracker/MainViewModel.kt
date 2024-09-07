package com.galileosky.gsgpstracker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.galileosky.gsgpstracker.location.LocationModel

class MainViewModel: ViewModel() {
    //
    val locationUpdates = MutableLiveData<LocationModel>()
    val timeData = MutableLiveData<String>()

}