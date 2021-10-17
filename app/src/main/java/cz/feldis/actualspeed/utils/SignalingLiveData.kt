package cz.feldis.actualspeed.utils

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SignalingLiveData<T> : MutableLiveData<T> {

    constructor() : super()
    constructor(value: T) : super(value)

    private val pending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            throw IllegalAccessException("For register multiple observers use MultipleSignalingLiveData.")
        }
        super.observe(owner, { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    @AnyThread
    override fun postValue(t: T?) {
        pending.set(true)
        super.postValue(t)
    }

    fun clear() {
        value = null
        pending.set(false)
    }
}