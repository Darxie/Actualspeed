package cz.feldis.actualspeed.drive

import android.graphics.Color
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapAnimation
import com.sygic.sdk.map.MapView
import com.sygic.sdk.map.`object`.MapRoute
import com.sygic.sdk.map.data.SimpleCameraDataModel
import com.sygic.sdk.navigation.StreetDetail
import com.sygic.sdk.navigation.StreetInfo
import com.sygic.sdk.navigation.routeeventnotifications.SpeedLimitInfo
import com.sygic.sdk.position.GeoCoordinates
import com.sygic.sdk.position.GeoPosition
import com.sygic.sdk.route.PrimaryRouteRequest
import com.sygic.sdk.route.RouteRequest
import cz.feldis.actualspeed.ktx.navigation.CurrentStreetDetailException
import cz.feldis.actualspeed.ktx.navigation.NavigationManagerKtx
import cz.feldis.actualspeed.ktx.position.PositionManagerKtx
import cz.feldis.actualspeed.ktx.routing.RouteSimulatorKtx
import cz.feldis.actualspeed.ktx.routing.RouterKtx
import cz.feldis.actualspeed.utils.RouteComputeListenerWrapper
import cz.feldis.actualspeed.utils.SignalingLiveData
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DriveFragmentViewModel : ViewModel() {

    private val positionManagerKtx = PositionManagerKtx()
    private val navigationManagerKtx = NavigationManagerKtx()
    private val routerKtx = RouterKtx()
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

    private val simulateButtonVisibleSignal = SignalingLiveData<Boolean>()
    val simulateButtonVisible: LiveData<Boolean> = simulateButtonVisibleSignal

    val mapDataModel = DriveMapDataModel()
    val cameraDataModel = SimpleCameraDataModel()

    init {
        initManagers()
        initMapDataModel()
        resetCamera()
    }

    private fun initManagers() {
        viewModelScope.launch {
            launch {
                positionManagerKtx.positions().collect { handlePosition(it) }
            }
            launch {
                navigationManagerKtx.speedLimits().collect { handleSpeedLimitInfo(it) }
            }
            launch {
                navigationManagerKtx.street().collect { handleStreetInfo(it) }
            }
            positionManagerKtx.startPositionUpdating()
        }
    }

    private fun initMapDataModel() {
        viewModelScope.launch {
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
    }

    fun resetCamera() {
        viewModelScope.launch {
            with(cameraDataModel) {
                movementMode = Camera.MovementMode.FollowGpsPosition
                rotationMode = Camera.RotationMode.Vehicle
                tilt = 90F
                zoomLevel = 14F
                position = positionManagerKtx.lastKnownPosition().coordinates
            }
        }
    }

    fun navigateTo(geoCoordinates: GeoCoordinates) {
        viewModelScope.launch {
            val routeRequest = RouteRequest().apply {
                setStart(positionManagerKtx.lastKnownPosition().coordinates)
                setDestination(geoCoordinates)
            }

            val route = routerKtx.calculateRouteWithAlternatives(
                PrimaryRouteRequest(
                    routeRequest,
                    RouteComputeListenerWrapper()
                )
            )
            route?.let {
                navigationManagerKtx.setRouteForNavigation(it)
                mapDataModel.setPrimaryRoute(it)
                simulateButtonVisibleSignal.postValue(true)
            }
        }
    }

    fun simulate() {
        viewModelScope.launch {
            navigationManagerKtx.getCurrentRoute()?.let {
                RouteSimulatorKtx().provideSimulator(it).start()
            }
        }
    }

    private fun handlePosition(geoPosition: GeoPosition) {
        currentSpeedTextMutable.postValue(geoPosition.speed.toInt().toString())
        if (geoPosition.speed > currentSpeedLimit + 5f) {
            currentSpeedColorMutable.postValue(Color.RED)
        } else {
            currentSpeedColorMutable.postValue(Color.GRAY)
        }
    }

    private fun handleSpeedLimitInfo(speedLimitInfo: SpeedLimitInfo) {
        currentSpeedLimit = speedLimitInfo.speedLimit
        cameraDataModel.setZoomLevel(
            if (speedLimitInfo.isInMunicipality()) {
                17F
            } else {
                15.5F
            },
            MapAnimation(500L, MapAnimation.InterpolationCurve.AccelerateDecelerate)
        )
        speedLimitTextMutable.postValue(currentSpeedLimit.toInt().toString())
    }

    private suspend fun handleStreetInfo(streetInfo: StreetInfo) {
        currentStreetInfoMutable.postValue(streetInfo)
        try {
            val streetDetail = navigationManagerKtx.currentStreetDetail()
            currentStreetDetailMutable.postValue(streetDetail)
        } catch (exception: CurrentStreetDetailException) {
            println(exception.message)
        }
    }
}