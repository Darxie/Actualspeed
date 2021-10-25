package cz.feldis.actualspeed.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapView
import com.sygic.sdk.map.`object`.MapMarker
import com.sygic.sdk.map.data.SimpleCameraDataModel
import com.sygic.sdk.map.data.SimpleMapDataModel
import com.sygic.sdk.position.GeoCoordinates
import com.sygic.sdk.search.GeocodingResult
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

    private val navigateToSignal = SignalingLiveData<GeoCoordinates>()
    val navigateTo: LiveData<GeoCoordinates> = navigateToSignal

    init {
        initMapDataModel()
        resetCamera()
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
        navigateToSignal.postValue(geocodingResult.location)
    }
}