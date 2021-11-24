package cz.feldis.actualspeed.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapRectangle
import com.sygic.sdk.map.MapView
import com.sygic.sdk.map.`object`.MapMarker
import com.sygic.sdk.map.data.SimpleCameraDataModel
import com.sygic.sdk.route.PrimaryRouteRequest
import com.sygic.sdk.route.RouteRequest
import com.sygic.sdk.route.RoutingOptions
import com.sygic.sdk.search.GeocodingResult
import cz.feldis.actualspeed.map.AdvancedMapDataModel
import cz.feldis.actualspeed.ktx.navigation.NavigationManagerKtx
import cz.feldis.actualspeed.ktx.position.PositionManagerKtx
import cz.feldis.actualspeed.ktx.routing.RouterKtx
import cz.feldis.actualspeed.utils.RouteComputeListenerWrapper
import cz.feldis.actualspeed.utils.SignalingLiveData
import kotlinx.coroutines.launch

private const val RouteMargin = 0.2F

class SearchResultFragmentViewModel : ViewModel() {
    private val positionManagerKtx = PositionManagerKtx()
    private val navigationManagerKtx = NavigationManagerKtx()

    private lateinit var geocodingResult: GeocodingResult
    val mapDataModel = AdvancedMapDataModel()
    val cameraDataModel = SimpleCameraDataModel()

    private val resultTitleSignal = SignalingLiveData<String>()
    val resultTitle: LiveData<String> = resultTitleSignal

    private val resultSubtitleSignal = SignalingLiveData<String>()
    val resultSubtitle: LiveData<String> = resultSubtitleSignal

    private val navigateToSignal = SignalingLiveData<Unit>()
    val navigateTo: LiveData<Unit> = navigateToSignal

    private val fastestRouteSignal = MutableLiveData<Boolean>()
    val fastestRoute: LiveData<Boolean> = fastestRouteSignal

    private val avoidTollRoadsSignal = MutableLiveData<Boolean>()
    val avoidTollRoads: LiveData<Boolean> = avoidTollRoadsSignal

    private val useUnpavedRoadsSignal = MutableLiveData<Boolean>()
    val useUnpavedRoads: LiveData<Boolean> = useUnpavedRoadsSignal

    private val calculateButtonVisibleSignal = MutableLiveData<Boolean>()
    val calculateButtonVisible: LiveData<Boolean> = calculateButtonVisibleSignal

    private val navigateButtonVisibleSignal = MutableLiveData<Boolean>()
    val navigateButtonVisible: LiveData<Boolean> = navigateButtonVisibleSignal

    private val progressBarVisibleSignal = MutableLiveData<Boolean>()
    val progressBarVisible: LiveData<Boolean> = progressBarVisibleSignal

    init {
        initMapDataModel()
        resetCamera()
        initRoutingOptions()
        calculateButtonVisibleSignal.postValue(false)
        navigateButtonVisibleSignal.postValue(false)
        progressBarVisibleSignal.postValue(false)
    }

    private fun initRoutingOptions() {
        fastestRouteSignal.postValue(true)
        avoidTollRoadsSignal.postValue(true)
        useUnpavedRoadsSignal.postValue(false)
    }

    fun setFastestRoute(useFastestRoute: Boolean) {
        fastestRouteSignal.postValue(useFastestRoute)
    }

    fun setAvoidTollRoads(avoidTollRoads: Boolean) {
        avoidTollRoadsSignal.postValue(avoidTollRoads)
    }

    fun setUseUnpavedRoads(useUnpavedRoads: Boolean) {
        useUnpavedRoadsSignal.postValue(useUnpavedRoads)
    }

    fun showResult(geocodingResult: GeocodingResult) {
        calculateButtonVisibleSignal.postValue(true)
        navigateButtonVisibleSignal.postValue(false)
        progressBarVisibleSignal.postValue(false)

        this.geocodingResult = geocodingResult
        viewModelScope.launch {
            with(geocodingResult) {
                cameraDataModel.position = location
                mapDataModel.addMapObject(MapMarker.at(location).build())
                resultTitleSignal.postValue(title)
                resultSubtitleSignal.postValue(subtitle)
            }
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

    private fun resetCamera() {
        viewModelScope.launch {
            with(cameraDataModel) {
                movementMode = Camera.MovementMode.Free
                rotationMode = Camera.RotationMode.NorthUp
                tilt = 0F
                zoomLevel = 14F
            }
        }
    }

    fun calculateRoute() {
        viewModelScope.launch {
            calculateButtonVisibleSignal.postValue(false)
            progressBarVisibleSignal.postValue(true)
            val routingOptions = RoutingOptions().apply {
                isTollRoadAvoided = avoidTollRoadsSignal.value ?: false
                isUnpavedRoadAvoided = useUnpavedRoadsSignal.value ?: false
                routingType =
                    if (fastestRouteSignal.value != false) RoutingOptions.RoutingType.Fastest else RoutingOptions.RoutingType.Shortest
            }
            val routeRequest = RouteRequest().apply {
                setStart(positionManagerKtx.lastKnownPosition().coordinates)
                setDestination(geocodingResult.location)
                this.routingOptions = routingOptions
            }

            val route = RouterKtx().calculateRouteWithAlternatives(
                PrimaryRouteRequest(
                    routeRequest,
                    RouteComputeListenerWrapper()
                )
            )
            progressBarVisibleSignal.postValue(false)
            navigateButtonVisibleSignal.postValue(true)
            route?.let {
                mapDataModel.setPrimaryRoute(it)
                cameraDataModel.mapRectangle = MapRectangle(it.boundingBox, RouteMargin, RouteMargin, RouteMargin, RouteMargin)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mapDataModel.clearPrimaryRoute()
    }

    fun startNavigation() {
        viewModelScope.launch {
            mapDataModel.primaryRoute?.data?.route?.let {
                navigationManagerKtx.setRouteForNavigation(it)
            }

            navigateToSignal.postValue(Unit)
        }
    }
}