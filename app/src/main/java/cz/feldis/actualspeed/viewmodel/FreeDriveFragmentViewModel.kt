package cz.feldis.actualspeed.viewmodel

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapView
import com.sygic.sdk.map.data.SimpleCameraDataModel
import com.sygic.sdk.map.data.SimpleMapDataModel
import com.sygic.sdk.navigation.StreetDetail
import com.sygic.sdk.navigation.StreetInfo
import cz.feldis.actualspeed.ktx.navigation.CurrentStreetDetailException
import cz.feldis.actualspeed.ktx.navigation.NavigationManagerKtx
import cz.feldis.actualspeed.ktx.position.PositionManagerKtx
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FreeDriveFragmentViewModel : ViewModel() {

    private val positionManagerKtx = PositionManagerKtx()
    private val navigationManagerKtx = NavigationManagerKtx()
    private var currentSpeedLimit = 0f

    private val currentStreetDetailMutable = MutableLiveData<StreetDetail>()
    val currentStreetDetail: LiveData<StreetDetail> = currentStreetDetailMutable

    private val currentStreetInfoMutable = MutableLiveData<StreetInfo>()
    val currentStreetInfo: LiveData<StreetInfo> = currentStreetInfoMutable

    private val currentSpeedColorMutable = MutableLiveData<Int>()
    val currentSpeedColor: LiveData<Int> = currentSpeedColorMutable

    private val currentSpeedTextMutable = MutableLiveData<String>()
    val currentSpeedText: LiveData<String> = currentSpeedTextMutable

    private val speedLimitTextMutable = MutableLiveData<String>()
    val speedLimitText: LiveData<String> = speedLimitTextMutable

    val mapDataModel = SimpleMapDataModel()
    val cameraDataModel = SimpleCameraDataModel()

    init {
        initManagers()
        initMapDataModel()
        resetCamera()
    }

    private fun initManagers() {
        viewModelScope.launch {
            launch {
                positionManagerKtx.positions().collect { geoPosition ->
                    currentSpeedTextMutable.postValue(geoPosition.speed.toInt().toString())
                    if (geoPosition.speed > currentSpeedLimit + 5f) {
                        currentSpeedColorMutable.postValue(Color.RED)
                    } else {
                        currentSpeedColorMutable.postValue(Color.GRAY)
                    }
                }
            }
            launch {
                navigationManagerKtx.speedLimits().collect { speedLimitInfo ->
                    currentSpeedLimit = speedLimitInfo.speedLimit
                    speedLimitTextMutable.postValue(currentSpeedLimit.toInt().toString())
                }
            }
            launch {
                navigationManagerKtx.street().collect { streetInfo ->
                    currentStreetInfoMutable.postValue(streetInfo)
                    try {
                        val streetDetail = navigationManagerKtx.currentStreetDetail()
                        currentStreetDetailMutable.postValue(streetDetail)
                    } catch (exception: CurrentStreetDetailException) {
                        println(exception.message)
                    }
                }
            }
            positionManagerKtx.startPositionUpdating()
        }
    }

    private fun initMapDataModel() {
        with(mapDataModel) {
            skin = listOf("night")
            setMapLayerCategoryVisibility(MapView.MapLayerCategory.Sky, false)
            setMapLayerCategoryVisibility(MapView.MapLayerCategory.Terrain, false)
            setMapLayerCategoryVisibility(MapView.MapLayerCategory.Areas, false)
            setMapLayerCategoryVisibility(MapView.MapLayerCategory.Pois, false)
            setMapLayerCategoryVisibility(MapView.MapLayerCategory.CityMaps, false)
            setMapLayerCategoryVisibility(MapView.MapLayerCategory.Landmarks, false)
            setMapLayerCategoryVisibility(MapView.MapLayerCategory.LabelCityCenters, false)
            setMapLayerCategoryVisibility(MapView.MapLayerCategory.LabelAddressPoints, false)
        }
    }

    fun resetCamera() {
        with(cameraDataModel) {
            movementMode = Camera.MovementMode.FollowGpsPosition
            rotationMode = Camera.RotationMode.Vehicle
            tilt = 30f
            zoomLevel = 16f
        }
    }
}