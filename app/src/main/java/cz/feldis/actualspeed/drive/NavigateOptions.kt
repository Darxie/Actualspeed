package cz.feldis.actualspeed.drive

import android.os.Parcelable
import com.sygic.sdk.position.GeoCoordinates
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NavigateOptions(val destination: GeoCoordinates, val fastestRoute: Boolean, val avoidTollRoads: Boolean, val useUnpavedRoads: Boolean) : Parcelable