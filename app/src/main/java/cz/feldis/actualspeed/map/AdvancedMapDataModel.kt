package cz.feldis.actualspeed.map

import com.sygic.sdk.map.`object`.MapRoute
import com.sygic.sdk.map.data.SimpleMapDataModel
import com.sygic.sdk.route.Route

class AdvancedMapDataModel : SimpleMapDataModel() {

    var primaryRoute: MapRoute? = null
    private set

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