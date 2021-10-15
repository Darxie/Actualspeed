package cz.feldis.actualspeed.ktx.position

import com.sygic.sdk.position.GeoCourse
import com.sygic.sdk.position.GeoPosition
import com.sygic.sdk.position.PositionManager
import com.sygic.sdk.position.PositionManagerProvider
import cz.feldis.actualspeed.ktx.SdkManagerKtx
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class PositionManagerKtx : SdkManagerKtx<PositionManager>(PositionManagerProvider::getInstance) {

    fun positions(): Flow<GeoPosition> = callbackFlow {
        val positionManager = manager()
        val listener = object : PositionManager.PositionChangeListener {
            override fun onCourseChanged(geoCourse: GeoCourse) {}

            override fun onPositionChanged(geoPosition: GeoPosition) {
                launch { send(geoPosition) }
            }
        }
        positionManager.addPositionChangeListener(listener)
        awaitClose { positionManager.removePositionChangeListener(listener) }
    }

    suspend fun lastKnownPosition() = manager().lastKnownPosition

    suspend fun startPositionUpdating() = manager().startPositionUpdating()

    suspend fun stopPositionUpdating() = manager().stopPositionUpdating()
}