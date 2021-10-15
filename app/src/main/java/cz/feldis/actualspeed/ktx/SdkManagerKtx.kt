package cz.feldis.actualspeed.ktx

import com.sygic.sdk.context.CoreInitCallback
import com.sygic.sdk.context.CoreInitException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

abstract class SdkManagerKtx<T>(private val factory: (CoreInitCallback<T>) -> Unit) {

    private var instance: T? = null

    suspend fun manager() = instance ?: suspendCancellableCoroutine<T> {
        factory(object : CoreInitCallback<T> {
            override fun onInstance(instance: T) {
                this@SdkManagerKtx.instance = instance
                it.resume(instance)
            }

            override fun onError(error: CoreInitException) {
                this@SdkManagerKtx.instance = null
                it.resumeWithException(error)
            }
        })
    }
}