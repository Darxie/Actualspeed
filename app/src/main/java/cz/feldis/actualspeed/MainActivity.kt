package cz.feldis.actualspeed

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.sygic.sdk.SygicEngine
import com.sygic.sdk.context.CoreInitCallback
import com.sygic.sdk.context.CoreInitException
import com.sygic.sdk.context.SygicContext
import com.sygic.sdk.diagnostics.LogConnector
import com.sygic.sdk.map.Camera
import com.sygic.sdk.map.MapFragment
import com.sygic.sdk.map.MapView
import com.sygic.sdk.map.fps.FpsConfig
import com.sygic.sdk.map.listeners.OnMapInitListener
import com.sygic.sdk.position.GeoCourse
import com.sygic.sdk.position.GeoPosition
import com.sygic.sdk.position.PositionManager
import com.sygic.sdk.position.PositionManagerProvider

class MainActivity : AppCompatActivity() {

    private lateinit var mMapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {

        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val speedTextView: TextView = findViewById(R.id.speed)
        val streetTextView: TextView = findViewById(R.id.streetView)

        val path = applicationContext.getExternalFilesDir(null).toString()
        val jsonConfig = "{\n" +
                "  \"Authentication\": {\n" +
                "    \"app_key\": \"your-key\",\n" +
                "    \"app_secret\": \"\"\n" +
                "  },\n" +
                "  \"StorageFolders\": {\n" +
                "    \"root_path\": \""+path+"\"\n" +
                "  },\n" +
                "  \"Logging\": [\n" +
                "    {\n" +
                "      \"name\": \"global\",\n" +
                "      \"classpath\": \"\",\n" +
                "      \"appenders\": [\n" +
                "        {\n" +
                "          \"class\": \"CConsoleAppender\",\n" +
                "          \"format\": \"%levshort %datetime %msg\\n\",\n" +
                "          \"level\": \"debug\",\n" +
                "          \"time\": \"%y/%m/%d %H:%M:%S\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"class\": \"CScreenAppender\",\n" +
                "          \"format\": \"%levshort: %msg\\n\",\n" +
                "          \"level\": \"warn\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"class\": \"CFileAppender\",\n" +
                "          \"format\": \"%levshort %datetime %msg\\n\",\n" +
                "          \"time\": \"%FT%T%z\",\n" +
                "          \"level\": \"debug\",\n" +
                "          \"file\": \"global.log\",\n" +
                "          \"append\": false,\n" +
                "          \"flush\": 100\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n"

        SygicEngine.initialize(this, null, object : LogConnector() {},
            jsonConfig, object: SygicEngine.OnInitCallback {
            override fun onInstance(p0: SygicContext) {

                val fragmentManager: FragmentManager = supportFragmentManager
                val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
                val fragment = MapFragment()
                fragmentTransaction.add(R.id.fragment_container, fragment)
                fragmentTransaction.commit()
                fragment.getMapAsync(object: OnMapInitListener {
                    override fun onMapInitializationInterrupted() {
                    }
                    override fun onMapReady(mapView: MapView) {
                        mMapView = mapView
                        mMapView.cameraModel.zoomLevel = 16f
                        mMapView.cameraModel.tilt = 50f
                        val streetListener = PositionManager.StreetChangeListener {
                            streetTextView.text = it.streetName
                        }
                        PositionManagerProvider.getInstance(object: CoreInitCallback<PositionManager>{
                            override fun onInstance(p0: PositionManager) {
                                p0.startPositionUpdating()
                                p0.addStreetChangeListener(streetListener)
                                p0.addPositionChangeListener(object: PositionManager.PositionChangeListener{
                                    override fun onCourseChanged(p0: GeoCourse?) {
                                    }
                                    override fun onPositionChanged(p0: GeoPosition?) {
                                        speedTextView.text = p0?.speed?.toInt().toString()
                                        if (mMapView.cameraModel.movementMode != Camera.MovementMode.FollowGpsPosition) {
                                            mMapView.cameraModel.movementMode = Camera.MovementMode.FollowGpsPosition
                                        }
                                        if (mMapView.cameraModel.rotationMode != Camera.RotationMode.Vehicle){
                                            mMapView.cameraModel.rotationMode = Camera.RotationMode.Vehicle
                                        }
                                    }
                                })
                            }
                            override fun onError(p0: CoreInitException?) {
                            }
                        })
                    }
                })

            }
            override fun onError(p0: CoreInitException?) {
            }
        })
    }
}