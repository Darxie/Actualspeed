package cz.feldis.actualspeed.utils

import com.sygic.sdk.map.MapView
import com.sygic.sdk.map.listeners.OnMapInitListener

open class OnMapInitListenerWrapper : OnMapInitListener {
    override fun onMapInitializationInterrupted() {}
    override fun onMapReady(mapView: MapView) {}
}