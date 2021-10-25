package cz.feldis.actualspeed.ktx.routing

import com.sygic.sdk.route.*
import com.sygic.sdk.route.listeners.RouteComputeFinishedListener
import cz.feldis.actualspeed.ktx.SdkManagerKtx
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class RouterKtx : SdkManagerKtx<Router>(RouterProvider::getInstance) {

    suspend fun calculateRouteWithAlternatives(routeRequest: PrimaryRouteRequest): Route? {
        val router = manager()
        return suspendCancellableCoroutine {
            val listener = object : RouteComputeFinishedListener {
                override fun onComputeFinished(
                    route: Route?,
                    alternatives: List<AlternativeRouteResult>
                ) {
                    it.resume(route)
                }
            }
            router.computeRouteWithAlternatives(routeRequest, listener = listener)
        }
    }
}