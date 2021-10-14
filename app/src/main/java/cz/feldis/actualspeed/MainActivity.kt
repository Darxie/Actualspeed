package cz.feldis.actualspeed

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.sygic.sdk.SygicEngine
import com.sygic.sdk.SygicEngine.initialize
import com.sygic.sdk.context.CoreInitException
import com.sygic.sdk.context.SygicContext
import com.sygic.sdk.context.SygicContextInitRequest
import com.sygic.sdk.diagnostics.LogConnector
import com.sygic.sdk.map.*
import com.sygic.sdk.map.`object`.MapMarker
import com.sygic.sdk.map.`object`.MapRoute
import com.sygic.sdk.map.`object`.MapSmartLabel
import com.sygic.sdk.map.`object`.StyledText
import com.sygic.sdk.map.listeners.OnMapInitListener
import com.sygic.sdk.navigation.NavigationManager
import com.sygic.sdk.navigation.NavigationManagerProvider
import com.sygic.sdk.navigation.OnStreetDetailListener
import com.sygic.sdk.navigation.StreetDetail
import com.sygic.sdk.navigation.routeeventnotifications.SpeedLimitInfo
import com.sygic.sdk.position.*
import com.sygic.sdk.route.*
import com.sygic.sdk.route.listeners.RouteComputeFinishedListener
import com.sygic.sdk.route.listeners.RouteComputeListener
import com.sygic.sdk.route.simulator.RouteDemonstrateSimulatorProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mPositionManager: PositionManager
    private lateinit var mCurrentStreet: String
    private lateinit var mJunctionText: String
    private lateinit var mSpeedLimitText: String
    private lateinit var mNavigationManager: NavigationManager
    private lateinit var mTrajectoryManager: TrajectoryManager
    private lateinit var mMapView: MapView
    var currentSpeedLimit = 0f

    override fun onCreate(savedInstanceState: Bundle?) {

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions, 0)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val speedTextView: TextView = findViewById(R.id.speed)
        val streetTextView: TextView = findViewById(R.id.street)
        val junctionTextView: TextView = findViewById(R.id.junction)
        val speedLimitTextView: TextView = findViewById(R.id.speedLimit)

        mCurrentStreet = ""
        mJunctionText = ""
        mSpeedLimitText = ""

        if (mSpeedLimitText == "") {
            findViewById<ImageView>(R.id.speedLimitImage)
        }

        val config = SygicEngine.JsonConfigBuilder()
        config.mapReaderSettings().startupOnlineMapsEnabled(true)
        val path = applicationContext.getExternalFilesDir(null).toString()
        config.storageFolders().rootPath(path)
        val clientID = ""
        config.authentication(clientID)

        initialize(
            SygicContextInitRequest(
                config.build(),
                applicationContext,
                null,
                object : LogConnector() {}),
            object : SygicEngine.OnInitCallback {
                override fun onInstance(instance: SygicContext) {
                    initManagers()
                    prepareMapView()

                    val streetListener = NavigationManager.StreetChangedListener {
                        mCurrentStreet = it.street + "\n" + it.city

                        streetTextView.text = mCurrentStreet

                        mNavigationManager.getCurrentStreetDetail(object : OnStreetDetailListener {
                            override fun onError(errorCode: OnStreetDetailListener.ErrorCode) {
                                println("FUCKED UP")
                            }

                            override fun onSuccess(streetDetail: StreetDetail) {
                                mJunctionText =
                                    "Next junction in " + streetDetail.distanceToNextJunction.toString() + "m"
                                junctionTextView.text = mJunctionText
                            }
                        })
                    }

                    speedTextView.setOnClickListener {
                        mMapView.cameraModel.movementMode =
                            Camera.MovementMode.FollowGpsPosition
                        mMapView.cameraModel.rotationMode = Camera.RotationMode.Vehicle
                        mMapView.cameraModel.tilt = 30f
                        mMapView.cameraModel.zoomLevel = 16f
                    }

                    val positionListener = object : PositionManager.PositionChangeListener {
                        override fun onCourseChanged(geoCourse: GeoCourse) {}

                        override fun onPositionChanged(geoPosition: GeoPosition) {
                            val actualSpeed = geoPosition.speed.toInt().toString()
                            speedTextView.text = actualSpeed

                            if (geoPosition.speed > currentSpeedLimit + 5f) {
                                findViewById<ImageView>(R.id.currentSpeedImage).setColorFilter(Color.RED)
                            } else {
                                findViewById<ImageView>(R.id.currentSpeedImage).setColorFilter(Color.GRAY)
                            }


                        }
                    }

                    val speedLimitListener = object : NavigationManager.OnSpeedLimitListener {
                        override fun onSpeedLimitInfoChanged(speedLimitInfo: SpeedLimitInfo) {
                            currentSpeedLimit = speedLimitInfo.speedLimit
                            speedLimitTextView.text = speedLimitInfo.speedLimit.toInt().toString()
                        }
                    }

                    mNavigationManager.addStreetChangedListener(streetListener)
                    mPositionManager.addPositionChangeListener(positionListener)
                    mNavigationManager.addOnSpeedLimitListener(speedLimitListener)

                }

                override fun onError(error: CoreInitException) {
                    // handle the error
                }
            })
    }

    private fun prepareMapView() {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction =
            fragmentManager.beginTransaction()
        val fragment = MapFragment()
        fragmentTransaction.add(R.id.mapFragment, fragment)
        fragmentTransaction.commit()
        fragment.getMapAsync(object : OnMapInitListener {
            override fun onMapInitializationInterrupted() {
            }

            override fun onMapReady(mapView: MapView) {
                mMapView = mapView
                mMapView.mapDataModel.setSkin(listOf("night"))
                mMapView.setMapLayerCategoryVisibility(MapView.MapLayerCategory.Sky, false)
                mMapView.setMapLayerCategoryVisibility(MapView.MapLayerCategory.Terrain, false)
                mMapView.setMapLayerCategoryVisibility(MapView.MapLayerCategory.Areas, false)
                mMapView.setMapLayerCategoryVisibility(MapView.MapLayerCategory.Pois, false)
                mMapView.setMapLayerCategoryVisibility(MapView.MapLayerCategory.CityMaps, false)
                mMapView.setMapLayerCategoryVisibility(MapView.MapLayerCategory.Landmarks, false)
                mMapView.setMapLayerCategoryVisibility(MapView.MapLayerCategory.LabelCityCenters, false)
                mMapView.setMapLayerCategoryVisibility(MapView.MapLayerCategory.LabelAddressPoints, false)
                mMapView.cameraModel.movementMode =
                    Camera.MovementMode.FollowGpsPosition
                mMapView.cameraModel.rotationMode = Camera.RotationMode.Vehicle
                mMapView.cameraModel.tilt = 30f
                mMapView.cameraModel.zoomLevel = 16f
            }
        })
    }

    fun initManagers() {
        mNavigationManager = NavigationManagerProvider.getInstance().get()
        mPositionManager = PositionManagerProvider.getInstance().get()
        mTrajectoryManager = TrajectoryManagerProvider.getInstance().get()
        mPositionManager.startPositionUpdating()
    }

}