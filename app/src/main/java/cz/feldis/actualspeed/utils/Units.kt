package cz.feldis.actualspeed.utils

object Units {

    fun formatMeters(meters: Int): String {
        return when {
            meters < 1000 -> "${meters}m"
            else -> "${meters/1000}km"
        }
    }
}