package cz.feldis.actualspeed.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapView
import com.sygic.sdk.map.`object`.MapMarker
import com.sygic.sdk.map.data.SimpleCameraDataModel
import com.sygic.sdk.map.data.SimpleMapDataModel
import com.sygic.sdk.search.GeocodingResult
import cz.feldis.actualspeed.drive.NavigateOptions
import cz.feldis.actualspeed.utils.SignalingLiveData
import kotlinx.coroutines.launch

class SearchResultFragmentViewModel : ViewModel() {
    private lateinit var geocodingResult: GeocodingResult
    val mapDataModel = SimpleMapDataModel()
    val cameraDataModel = SimpleCameraDataModel()

    private val resultTitleSignal = SignalingLiveData<String>()
    val resultTitle: LiveData<String> = resultTitleSignal

    private val resultSubtitleSignal = SignalingLiveData<String>()
    val resultSubtitle: LiveData<String> = resultSubtitleSignal

    private val navigateToSignal = SignalingLiveData<NavigateOptions>()
    val navigateTo: LiveData<NavigateOptions> = navigateToSignal

    private val fastestRouteSignal = MutableLiveData<Boolean>()
    val fastestRoute: LiveData<Boolean> = fastestRouteSignal

    private val avoidTollRoadsSignal = MutableLiveData<Boolean>()
    val avoidTollRoads: LiveData<Boolean> = avoidTollRoadsSignal

    private val useUnpavedRoadsSignal = MutableLiveData<Boolean>()
    val useUnpavedRoads: LiveData<Boolean> = useUnpavedRoadsSignal

    init {
        initMapDataModel()
        resetCamera()
        initRoutingOptions()
    }

    private fun initRoutingOptions() {
        fastestRouteSignal.postValue(true)
        avoidTollRoadsSignal.postValue(false)
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

    fun onNavigateToResultClick() {
        navigateToSignal.postValue(
            NavigateOptions(
                geocodingResult.location,
                fastestRouteSignal.value ?: true,
                avoidTollRoadsSignal.value ?: true,
                useUnpavedRoadsSignal.value ?: true
            )
        )
    }
}