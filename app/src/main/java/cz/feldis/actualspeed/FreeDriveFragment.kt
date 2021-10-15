package cz.feldis.actualspeed

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapFragment
import com.sygic.sdk.map.MapView
import com.sygic.sdk.navigation.NavigationManager
import com.sygic.sdk.navigation.NavigationManagerProvider
import com.sygic.sdk.navigation.OnStreetDetailListener
import com.sygic.sdk.navigation.StreetDetail
import com.sygic.sdk.navigation.routeeventnotifications.SpeedLimitInfo
import com.sygic.sdk.position.*
import cz.feldis.actualspeed.databinding.FragmentFreeDriveBinding
import cz.feldis.actualspeed.utils.OnMapInitListenerWrapper

class FreeDriveFragment : MapFragment() {
    private lateinit var binding: FragmentFreeDriveBinding

    private lateinit var positionManager: PositionManager
    private lateinit var navigationManager: NavigationManager
    private lateinit var trajectoryManager: TrajectoryManager

    var currentSpeedLimit = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFreeDriveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.speed.setOnClickListener { setCamera() }

        getMapAsync(object : OnMapInitListenerWrapper() {
            override fun onMapReady(mapView: MapView) {
                initManagers()
                prepareMapView()

                val streetListener = NavigationManager.StreetChangedListener {
                    binding.street.text = it.street + "\n" + it.city

                    navigationManager.getCurrentStreetDetail(object : OnStreetDetailListener {
                        override fun onError(errorCode: OnStreetDetailListener.ErrorCode) {
                            println("FUCKED UP")
                        }

                        override fun onSuccess(streetDetail: StreetDetail) {
                            binding.junction.text = "Next junction in ${streetDetail.distanceToNextJunction}m"
                        }
                    })
                }



                val positionListener = object : PositionManager.PositionChangeListener {
                    override fun onCourseChanged(geoCourse: GeoCourse) {}

                    override fun onPositionChanged(geoPosition: GeoPosition) {
                        binding.speed.text = geoPosition.speed.toInt().toString()

                        if (geoPosition.speed > currentSpeedLimit + 5f) {
                            binding.currentSpeedImage.setColorFilter(Color.RED)
                        } else {
                            binding.currentSpeedImage.setColorFilter(Color.GRAY)
                        }


                    }
                }

                val speedLimitListener = object : NavigationManager.OnSpeedLimitListener {
                    override fun onSpeedLimitInfoChanged(speedLimitInfo: SpeedLimitInfo) {
                        currentSpeedLimit = speedLimitInfo.speedLimit
                        binding.speedLimit.text = speedLimitInfo.speedLimit.toInt().toString()
                    }
                }

                navigationManager.addStreetChangedListener(streetListener)
                positionManager.addPositionChangeListener(positionListener)
                navigationManager.addOnSpeedLimitListener(speedLimitListener)
            }
        })
    }

    fun initManagers() {
        navigationManager = NavigationManagerProvider.getInstance().get()
        positionManager = PositionManagerProvider.getInstance().get()
        trajectoryManager = TrajectoryManagerProvider.getInstance().get()
        positionManager.startPositionUpdating()
    }

    private fun prepareMapView() {
        setSkin()
        setCategories()
        setCamera()
    }

    private fun setSkin() {
        mapView?.mapDataModel?.setSkin(listOf("night"))
    }

    private fun setCategories() {
        mapView?.apply {
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

    private fun setCamera() {
        mapView?.cameraModel?.apply {
            movementMode = Camera.MovementMode.FollowGpsPosition
            rotationMode = Camera.RotationMode.Vehicle
            tilt = 30f
            zoomLevel = 16f
        }
    }
}