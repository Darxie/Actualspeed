package cz.feldis.actualspeed.ktx.routing

import com.sygic.sdk.context.CoreInitCallback
import com.sygic.sdk.context.CoreInitException
import com.sygic.sdk.route.Route
import com.sygic.sdk.route.simulator.RouteDemonstrateSimulator
import com.sygic.sdk.route.simulator.RouteDemonstrateSimulatorProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RouteSimulatorKtx {

    suspend fun provideSimulator(route: Route): RouteDemonstrateSimulator {
        return suspendCancellableCoroutine {
            RouteDemonstrateSimulatorProvider.getInstance(
                route,
                object : CoreInitCallback<RouteDemonstrateSimulator> {
                    override fun onError(error: CoreInitException) {
                        it.resumeWithException(Exception(error.message))
                    }

                    override fun onInstance(instance: RouteDemonstrateSimulator) {
                        it.resume(instance)
                    }
                })
        }
    }
}