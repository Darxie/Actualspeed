package cz.feldis.actualspeed.ktx.navigation

import com.sygic.sdk.navigation.*
import com.sygic.sdk.navigation.routeeventnotifications.SpeedLimitInfo
import com.sygic.sdk.route.Route
import cz.feldis.actualspeed.ktx.SdkManagerKtx
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class CurrentStreetDetailException(val errorCode: OnStreetDetailListener.ErrorCode) :
    Exception("GetCurrentStreetDetail failed with OnStreetDetailListener.ErrorCode: $errorCode")

class NavigationManagerKtx :
    SdkManagerKtx<NavigationManager>(NavigationManagerProvider::getInstance) {

    fun speedLimits(): Flow<SpeedLimitInfo> = callbackFlow {
        val manager = manager()
        val listener = NavigationManager.OnSpeedLimitListener { launch { send(it) } }

        manager.addOnSpeedLimitListener(listener)
        awaitClose { manager.removeOnSpeedLimitListener(listener) }
    }

    fun street(): Flow<StreetInfo> = callbackFlow {
        val manager = manager()
        val listener = NavigationManager.StreetChangedListener { launch { send(it) } }

        manager.addStreetChangedListener(listener)
        awaitClose { manager.removeStreetChangedListener(listener) }
    }

    suspend fun currentStreetDetail(): StreetDetail {
        val manager = manager()
        return suspendCancellableCoroutine {
            val listener = object : OnStreetDetailListener {
                override fun onError(errorCode: OnStreetDetailListener.ErrorCode) {
                    it.resumeWithException(CurrentStreetDetailException(errorCode))
                }

                override fun onSuccess(streetDetail: StreetDetail) {
                    it.resume(streetDetail)
                }
            }
            manager.getCurrentStreetDetail(listener)
        }
    }

    suspend fun setRouteForNavigation(route: Route) = manager().setRouteForNavigation(route)
    suspend fun getCurrentRoute(): Route? = manager().currentRoute
}