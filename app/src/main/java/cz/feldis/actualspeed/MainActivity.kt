package cz.feldis.actualspeed

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
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
    private var testingEnv = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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

        if (testingEnv) {
            config.online().routingUrl("https://directions-testing.api.sygic.com")
            config.online().sSOServerUrl("https://auth-testing.api.sygic.com")
            config.online().productServer().licenceUrl("https://licensing-testing.api.sygic.com")
            config.online().productServer()
                .connectUrl("https://productserver-testing.api.sygic.com")
            config.online().productServer()
                .onlineMapsLinkUrl("https://licensing-testing.api.sygic.com")
            config.online().incidents().url("https://incidents-testing.api.sygic.com")
            config.online().searchUrl("https://search-testing.api.sygic.com")
            config.online().trafficUrl("https://traffic-testing.api.sygic.com")
            config.online().offlineMapsApiUrl("https://licensing-testing.api.sygic.com")
            config.online().voicesUrl("https://nonttsvoices-testing.api.sygic.com")
        }

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