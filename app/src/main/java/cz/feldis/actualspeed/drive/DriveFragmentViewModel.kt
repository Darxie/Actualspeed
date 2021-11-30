package cz.feldis.actualspeed.drive

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.sdk.audio.AudioManager
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapView
import com.sygic.sdk.map.data.SimpleCameraDataModel
import com.sygic.sdk.navigation.StreetInfo
import com.sygic.sdk.navigation.routeeventnotifications.SharpCurveInfo
import com.sygic.sdk.navigation.routeeventnotifications.SpeedLimitInfo
import com.sygic.sdk.position.GeoCoordinates
import com.sygic.sdk.position.GeoPosition
import com.sygic.sdk.route.PrimaryRouteRequest
import com.sygic.sdk.route.Route
import com.sygic.sdk.route.RouteRequest
import com.sygic.sdk.route.RoutingOptions
import com.sygic.sdk.route.simulator.PositionSimulator
import com.sygic.sdk.route.simulator.RouteDemonstrateSimulator
import cz.feldis.actualspeed.R
import cz.feldis.actualspeed.curves.Curve
import cz.feldis.actualspeed.ktx.audio.AudioManagerKtx
import cz.feldis.actualspeed.ktx.navigation.NavigationManagerKtx
import cz.feldis.actualspeed.ktx.position.PositionManagerKtx
import cz.feldis.actualspeed.ktx.routing.RouteSimulatorKtx
import cz.feldis.actualspeed.ktx.routing.RouterKtx
import cz.feldis.actualspeed.map.AdvancedMapDataModel
import cz.feldis.actualspeed.utils.RouteComputeListenerWrapper
import cz.feldis.actualspeed.utils.Units
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val BabaDemo = false

class DriveFragmentViewModel : ViewModel() {

    private enum class Mode {
        FreeDrive,
        Navigation
    }

    val mapDataModel = AdvancedMapDataModel()
    val cameraDataModel = SimpleCameraDataModel()

    private val positionManagerKtx = PositionManagerKtx()
    private val navigationManagerKtx = NavigationManagerKtx()

    private var currentSpeedLimit = 0f
    private var simulator: RouteDemonstrateSimulator? = null
    private var simulatorState = PositionSimulator.SimulatorState.Closed

    private val nextCurveDistanceMutable = MutableLiveData<String>()
    val nextCurveDistance: LiveData<String> = nextCurveDistanceMutable

    private val nextCurveTextMutable = MutableLiveData<Int>()
    val nextCurveText: LiveData<Int> = nextCurveTextMutable

    private val nextCurveImageMutable = MutableLiveData<Int>()
    val nextCurveImage: LiveData<Int> = nextCurveImageMutable

    private val currentStreetInfoMutable = MutableLiveData<StreetInfo>()
    val currentStreetInfo: LiveData<StreetInfo> = currentStreetInfoMutable

    private val currentSpeedColorMutable = MutableLiveData<Int>()
    val currentSpeedColor: LiveData<Int> = currentSpeedColorMutable

    private val currentSpeedTextMutable = MutableLiveData<String>()
    val currentSpeedText: LiveData<String> = currentSpeedTextMutable

    private val speedLimitTextMutable = MutableLiveData<String>()
    val speedLimitText: LiveData<String> = speedLimitTextMutable

    private val simulateButtonVisibleSignal = MutableLiveData<Boolean>()
    val simulateButtonVisible: LiveData<Boolean> = simulateButtonVisibleSignal

    private val simulateButtonIconSignal = MutableLiveData<Int>()
    val simulateButtonIcon: LiveData<Int> = simulateButtonIconSignal

    private val stopNavigationButtonVisibleSignal = MutableLiveData<Boolean>()
    val stopNavigationButtonVisible: LiveData<Boolean> = stopNavigationButtonVisibleSignal

    init {
        initManagers()
        initMapDataModel()
        resetCamera()
        viewModelScope.launch {
            if (BabaDemo) {
                babaRoute()
            }
            else {
                navigationManagerKtx.getCurrentRoute()?.let {
                    setMode(Mode.Navigation)
                } ?: setMode(Mode.FreeDrive)
            }
        }
    }

    private suspend fun babaRoute() {
        setMode(Mode.Navigation)

        val routeRequest = RouteRequest().apply {
            setStart(GeoCoordinates(48.342614017202735, 17.221804079842677))
            setDestination(GeoCoordinates(48.362013613994165, 17.171891625370957))
        }

        val route = RouterKtx().calculateRouteWithAlternatives(
            PrimaryRouteRequest(
                routeRequest,
                RouteComputeListenerWrapper()
            )
        )
        route?.let{
            navigationManagerKtx.setRouteForNavigation(it)
            startSimulation(it)
        }
    }

    override fun onCleared() {
        mapDataModel.clearPrimaryRoute()
        mapDataModel.clearTrajectory()
        super.onCleared()
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
            launch {
                navigationManagerKtx.routeChanged().collect { handleRouteChanged(it) }
            }
            launch {
                navigationManagerKtx.curves().collect {
                    handleCurve(it)
                }
            }
            positionManagerKtx.startPositionUpdating()
        }
    }

    private fun initMapDataModel() {
        viewModelScope.launch {
            with(mapDataModel) {
                setMapLayerCategoryVisibility(MapView.MapLayerCategory.Sky, false)
                setMapLayerCategoryVisibility(MapView.MapLayerCategory.Terrain, false)
                setMapLayerCategoryVisibility(MapView.MapLayerCategory.Areas, false)
                setMapLayerCategoryVisibility(MapView.MapLayerCategory.Pois, false)
                setMapLayerCategoryVisibility(MapView.MapLayerCategory.CityMaps, false)
                setMapLayerCategoryVisibility(MapView.MapLayerCategory.Landmarks, false)
                setMapLayerCategoryVisibility(MapView.MapLayerCategory.LabelCityCenters, false)
                setMapLayerCategoryVisibility(MapView.MapLayerCategory.LabelAddressPoints, false)
                setMapLayerCategoryVisibility(MapView.MapLayerCategory.SmartLabels, false)
                setMapLayerCategoryVisibility(MapView.MapLayerCategory.RouteJunctions, false)
            }
        }
    }

    fun resetCamera() {
        viewModelScope.launch {
            with(cameraDataModel) {
                movementMode = Camera.MovementMode.FollowGpsPositionWithAutozoom
                rotationMode = Camera.RotationMode.Vehicle
                tilt = 90F
                zoomLevel = 14F
                position = positionManagerKtx.lastKnownPosition().coordinates
            }
        }
    }

    fun enableDebugLayer(): Boolean {
        return mapDataModel.setMapLayerCategoryVisibility(
            MapView.MapLayerCategory.Debug,
            true
        ) // toto nefunguje
    }

    fun simulate() {
        viewModelScope.launch {
            navigationManagerKtx.getCurrentRoute()?.let {
                if (simulatorState != PositionSimulator.SimulatorState.Closed) {
                    stopSimulation()
                } else {
                    startSimulation(it)
                }
            }
        }
    }

    private fun handleRouteChanged(route : Route?) {
        route?.let {
            mapDataModel.clearPrimaryRoute()
            mapDataModel.setPrimaryRoute(route)
        }
    }

    private fun handlePosition(geoPosition: GeoPosition) {
        currentSpeedTextMutable.postValue(geoPosition.speed.toInt().toString())
        println(geoPosition.speed.toInt().toString())
        if (geoPosition.speed > currentSpeedLimit + 5f) {
            currentSpeedColorMutable.postValue(Color.RED)
        } else {
            currentSpeedColorMutable.postValue(Color.GRAY)
        }
    }

    private fun handleSpeedLimitInfo(speedLimitInfo: SpeedLimitInfo) {
        currentSpeedLimit = speedLimitInfo.speedLimit
        speedLimitTextMutable.postValue(currentSpeedLimit.toInt().toString())
    }

    private fun handleStreetInfo(streetInfo: StreetInfo) {
        currentStreetInfoMutable.postValue(streetInfo)
    }

    private fun handleCurve(sharpCurveInfo: SharpCurveInfo) {
        Log.e("XXX", "SharpCurve angle: ${sharpCurveInfo.angle} direction: ${sharpCurveInfo.direction}")
        val curve = Curve.fromSharpCurveInfo(sharpCurveInfo)
        nextCurveTextMutable.postValue(curve.description())
        nextCurveImageMutable.postValue(curve.icon())
        nextCurveDistanceMutable.postValue(Units.formatMeters(curve.distance))
    }

    private fun setMode(mode: Mode) {
        viewModelScope.launch {
            if (mode == Mode.FreeDrive) {
                stopSimulation()
                navigationManagerKtx.stopNavigation()
                mapDataModel.clearPrimaryRoute()
                mapDataModel.skin = listOf("night")
                mapDataModel.setMapLayerCategoryVisibility(
                    MapView.MapLayerCategory.Collections,
                    true
                )
                simulateButtonVisibleSignal.postValue(false)
                stopNavigationButtonVisibleSignal.postValue(false)
            } else { // Mode.Navigation
                mapDataModel.skin = listOf("car", "night")
                mapDataModel.setMapLayerCategoryVisibility(
                    MapView.MapLayerCategory.Collections,
                    false
                )
                simulateButtonVisibleSignal.postValue(BabaDemo.not())
                stopNavigationButtonVisibleSignal.postValue(BabaDemo.not())
            }
        }
    }

    fun stopNavigation() {
        setMode(Mode.FreeDrive)
    }

    private fun stopSimulation() {
        simulator?.stop()
        simulatorState = PositionSimulator.SimulatorState.Closed
        simulateButtonIconSignal.postValue(R.drawable.ic_play_arrow_24)
    }

    private fun startSimulation(route: Route) {
        viewModelScope.launch {
            simulator = RouteSimulatorKtx().provideSimulator(route).apply {
                setSpeedMultiplier(2F)
                addPositionSimulatorListener(object :
                    PositionSimulator.PositionSimulatorListener {
                    override fun onSimulatedStateChanged(state: Int) {
                        simulatorState = state
                        if (simulatorState == PositionSimulator.SimulatorState.End) {
                            simulator?.start()
                        }
                    }

                    override fun onSimulatedPositionChanged(position: GeoPosition, p1: Float) {
                        handlePosition(position)
                    }
                })
                start()
            }
            simulateButtonIconSignal.postValue(R.drawable.ic_stop_24)
        }
    }
}