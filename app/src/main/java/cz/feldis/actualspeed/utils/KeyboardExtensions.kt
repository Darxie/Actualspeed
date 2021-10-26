package cz.feldis.actualspeed.utils

import android.app.Activity
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment

fun View.hideKb() {
    (context as? Activity)?.hideKb()
}

fun Fragment.hideKb() {
    activity?.hideKb()
}

fun Activity.hideKb() {
    WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.ime())
}