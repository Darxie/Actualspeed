package cz.feldis.actualspeed

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sygic.sdk.SygicEngine
import com.sygic.sdk.context.CoreInitException
import com.sygic.sdk.context.SygicContext
import com.sygic.sdk.context.SygicContextInitRequest
import cz.feldis.actualspeed.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initSdk()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initSdk() {
        val configJson = getConfigurationJsonString(applicationContext)
        val sdkInitRequest = SygicContextInitRequest(configJson, applicationContext)
        SygicEngine.initialize(sdkInitRequest, object : SygicEngine.OnInitCallback {
            override fun onError(error: CoreInitException) {}
            override fun onInstance(instance: SygicContext) {
                checkLocationPermission()
            }
        })
    }

    private fun getConfigurationJsonString(context: Context): String {
        val config = SygicEngine.JsonConfigBuilder()
        val path = context.getExternalFilesDir(null).toString()
        config.storageFolders().rootPath(path)
        config.authentication(BuildConfig.SDK_CLIENT_ID)
        config.mapReaderSettings().startupOnlineMapsEnabled(true)
        return config.build()
    }


    private fun checkLocationPermission() {
        when (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> {
                SygicEngine.openGpsConnection()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    158 // volam zandare
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.first() == Manifest.permission.ACCESS_FINE_LOCATION && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
            SygicEngine.openGpsConnection()
        }
    }
}