package cz.feldis.actualspeed.drive

import com.sygic.sdk.map.`object`.MapRoute
import com.sygic.sdk.map.data.SimpleMapDataModel
import com.sygic.sdk.route.Route

class DriveMapDataModel : SimpleMapDataModel() {

    private var primaryRoute: MapRoute? = null

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
}