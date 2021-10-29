package cz.feldis.actualspeed.map

import com.sygic.sdk.map.`object`.MapMarker
import com.sygic.sdk.map.`object`.MapRoute
import com.sygic.sdk.map.data.SimpleMapDataModel
import com.sygic.sdk.position.GeoCoordinates
import com.sygic.sdk.position.Trajectory
import com.sygic.sdk.position.data.TrajectoryPoint
import com.sygic.sdk.route.Route
import cz.feldis.actualspeed.R

private const val MaxTrajectoryPoints = 20

class AdvancedMapDataModel : SimpleMapDataModel() {

    var primaryRoute: MapRoute? = null
        private set

    private val trajectoryPoints = mutableMapOf<GeoCoordinates, MapMarker>()

    fun setPrimaryRoute(route: Route) {
        clearPrimaryRoute()

        primaryRoute = MapRoute.from(route).setType(MapRoute.RouteType.Primary).build().apply {
            addMapObject(this)
        }
    }

    fun clearPrimaryRoute() {
        primaryRoute?.let {
            removeMapObject(it)
        }
    }

    fun setTrajectory(trajectory: Trajectory) {
        val newTrajectory = mutableMapOf<GeoCoordinates, MapMarker>()
        var point: TrajectoryPoint?
        do {
            point = trajectory.advance()

            point?.apply {
                if (trajectoryPoints.containsKey(position)) {
                    // marker na tejto polohe uz je, len ho prehodim do noveho zoznamu
                    newTrajectory[position] = requireNotNull(trajectoryPoints[position])
                    trajectoryPoints.remove(position)
                } else {
                    // marker je novy
                    val marker = MapMarker.at(position)
                        .withIcon(R.drawable.circle)
                        .build()
                    newTrajectory[position] = marker
                    addMapObject(marker)
                }
            }

            if (newTrajectory.size >= MaxTrajectoryPoints) {
                break
            }
        } while(point != null)

        // odstranit markery, ktory boli v starej a uz nie su v novej trajektorii
        trajectoryPoints.forEach { removeMapObject(it.value) }
        trajectoryPoints.clear()
        trajectoryPoints.putAll(newTrajectory)
    }

    fun clearTrajectory() {
        trajectoryPoints.forEach { removeMapObject(it.value) }
        trajectoryPoints.clear()
    }
}