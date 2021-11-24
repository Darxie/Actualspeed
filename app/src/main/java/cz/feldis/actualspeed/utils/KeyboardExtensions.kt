package cz.feldis.actualspeed.utils

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment

fun View.focusAndShowKeyboard() {
    fun View.showKeyboard() {
        if (isFocused) {
            post {
                showKb()
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        showKeyboard()
    } else {
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showKeyboard()
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

fun View.hideKb() {
    (context as? Activity)?.hideKb()
}

fun Fragment.hideKb() {
    activity?.hideKb()
}

fun Activity.hideKb() {
    WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
}

fun View.showKb() {
    (context as? Activity)?.showKb()
}

fun Fragment.showKb() {
    activity?.showKb()
}

fun Activity.showKb() {
    WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.ime())
}