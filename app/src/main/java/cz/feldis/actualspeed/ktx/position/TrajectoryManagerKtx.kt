package cz.feldis.actualspeed.ktx.position

import com.sygic.sdk.position.Trajectory
import com.sygic.sdk.position.TrajectoryManager
import com.sygic.sdk.position.TrajectoryManagerProvider
import com.sygic.sdk.position.listeners.OnTrajectoryCreated
import cz.feldis.actualspeed.ktx.SdkManagerKtx
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class CreateTrajectoryException(val errorCode: Trajectory.ResultCode) : Exception("CreateTrajectory failed with Trajectory.ResultCode: $errorCode")

class TrajectoryManagerKtx : SdkManagerKtx<TrajectoryManager>(TrajectoryManagerProvider::getInstance) {

    suspend fun createTrajectory(): Trajectory {
        val trajectoryManager = manager()
        return suspendCancellableCoroutine {
            trajectoryManager.createTrajectory(object : OnTrajectoryCreated {
                override fun onSuccess(trajectory: Trajectory) {
                    it.resume(trajectory)
                }

                override fun onError(errorCode: Trajectory.ResultCode) {
                    it.resumeWithException(CreateTrajectoryException(errorCode))
                }
            })
        }
    }

    suspend fun destroyTrajectory(trajectory: Trajectory) = manager().destroyTrajectory(trajectory)
}