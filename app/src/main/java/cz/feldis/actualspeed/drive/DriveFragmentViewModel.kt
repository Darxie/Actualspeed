package cz.feldis.actualspeed.drive

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapView
import com.sygic.sdk.map.data.SimpleCameraDataModel
import com.sygic.sdk.navigation.StreetDetail
import com.sygic.sdk.navigation.StreetInfo
import com.sygic.sdk.navigation.routeeventnotifications.DirectionInfo
import com.sygic.sdk.navigation.routeeventnotifications.SpeedLimitInfo
import com.sygic.sdk.position.GeoCoordinates
import com.sygic.sdk.position.GeoPosition
import com.sygic.sdk.position.Trajectory
import com.sygic.sdk.route.PrimaryRouteRequest
import com.sygic.sdk.route.Route
import com.sygic.sdk.route.RouteRequest
import com.sygic.sdk.route.simulator.PositionSimulator
import com.sygic.sdk.route.simulator.RouteDemonstrateSimulator
import cz.feldis.actualspeed.R
import cz.feldis.actualspeed.ktx.navigation.CurrentStreetDetailException
import cz.feldis.actualspeed.ktx.navigation.NavigationManagerKtx
import cz.feldis.actualspeed.ktx.position.PositionManagerKtx
import cz.feldis.actualspeed.ktx.position.TrajectoryManagerKtx
import cz.feldis.actualspeed.ktx.routing.RouteSimulatorKtx
import cz.feldis.actualspeed.ktx.routing.RouterKtx
import cz.feldis.actualspeed.utils.RouteComputeListenerWrapper
import cz.feldis.actualspeed.utils.SignalingLiveData
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TrajectorySearchDistance = 1000 //meters

class DriveFragmentViewModel : ViewModel() {

    private enum class Mode {
        FreeDrive,
        Navigation
    }

    private val positionManagerKtx = PositionManagerKtx()
    private val navigationManagerKtx = NavigationManagerKtx()
    private val trajectoryManagerKtx = TrajectoryManagerKtx()
    private val routerKtx = RouterKtx()
    private var currentSpeedLimit = 0f
    private var simulator: RouteDemonstrateSimulator? = null
    private var simulatorState = PositionSimulator.SimulatorState.Closed

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

    private val simulateButtonIconSignal = SignalingLiveData<Int>()
    val simulateButtonIcon: LiveData<Int> = simulateButtonIconSignal

    private val stopNavigationButtonVisibleSignal = SignalingLiveData<Boolean>()
    val stopNavigationButtonVisible: LiveData<Boolean> = stopNavigationButtonVisibleSignal

    val mapDataModel = DriveMapDataModel()
    val cameraDataModel = SimpleCameraDataModel()

    init {
        initManagers()
        initMapDataModel()
        resetCamera()
        setMode(Mode.FreeDrive)
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
                navigationManagerKtx.direction().collect { handleDirectionInfo(it) }
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
                setMode(Mode.Navigation)
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

    private fun handlePosition(geoPosition: GeoPosition) {
        viewModelScope.launch {
            handleTrajectory(trajectoryManagerKtx.createTrajectory())
        }

        currentSpeedTextMutable.postValue(geoPosition.speed.toInt().toString())
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

    private suspend fun handleStreetInfo(streetInfo: StreetInfo) {
        currentStreetInfoMutable.postValue(streetInfo)
        try {
            val streetDetail = navigationManagerKtx.currentStreetDetail()
            currentStreetDetailMutable.postValue(streetDetail)
        } catch (exception: CurrentStreetDetailException) {
            println(exception.message)
        }
    }

    private fun handleDirectionInfo(directionInfo: DirectionInfo) {
        //ToDO
    }

    private fun handleTrajectory(trajectory: Trajectory?) {
        do {
            trajectory?.advance()?.apply {
                Log.d("TRAJECTORY", "Point distanceFromStart:$distanceFromStart angle:$angle")
                if (distanceFromStart > TrajectorySearchDistance) {
                    return
                }
            } ?: return
        } while (true)
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
                simulateButtonVisibleSignal.postValue(true)
                stopNavigationButtonVisibleSignal.postValue(true)
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