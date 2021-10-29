package cz.feldis.actualspeed.ktx.position

import com.sygic.sdk.position.Trajectory
import com.sygic.sdk.position.TrajectoryManager
import com.sygic.sdk.position.TrajectoryManagerProvider
import com.sygic.sdk.position.listeners.OnTrajectoryCreated
import cz.feldis.actualspeed.ktx.SdkManagerKtx
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class CreateTrajectoryException(val errorCode: Trajectory.ResultCode) : Exception("CreateTrajectory failed with Trajectory.ResultCode: $errorCode")

sealed class TrajectoryResult {
    data class Success(val trajectory: Trajectory) : TrajectoryResult()
    data class Error(val error: CreateTrajectoryException) : TrajectoryResult()
}

class TrajectoryManagerKtx : SdkManagerKtx<TrajectoryManager>(TrajectoryManagerProvider::getInstance) {

    suspend fun createTrajectory(): TrajectoryResult {
        val trajectoryManager = manager()
        return suspendCancellableCoroutine {
            trajectoryManager.createTrajectory(object : OnTrajectoryCreated {
                override fun onSuccess(trajectory: Trajectory) {
                    it.resume(TrajectoryResult.Success(trajectory))
                }

                override fun onError(errorCode: Trajectory.ResultCode) {
                    it.resume(TrajectoryResult.Error(CreateTrajectoryException(errorCode)))
                }
            })
        }
    }

    suspend fun destroyTrajectory(trajectory: Trajectory) = manager().destroyTrajectory(trajectory)
}