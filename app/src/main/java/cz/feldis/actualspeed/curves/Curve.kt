package cz.feldis.actualspeed.curves

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.sygic.sdk.navigation.routeeventnotifications.SharpCurveInfo
import cz.feldis.actualspeed.R
import kotlin.math.abs

enum class Direction {
    LEFT,
    RIGHT
}

class Curve(
    val distance: Int,
    private val direction: Direction,
    angle: Float
) {
    private val angle = if (angle % 360 > 180F) abs(angle % 360 - 360F) else angle % 360
    companion object {
        fun fromSharpCurveInfo(sharpCurveInfo: SharpCurveInfo): Curve {
            val direction = if (sharpCurveInfo.direction == SharpCurveInfo.Direction.Left) Direction.LEFT else Direction.RIGHT
            return Curve(sharpCurveInfo.distance, direction, sharpCurveInfo.angle.toFloat())
        }
    }

    @StringRes
    fun description(): Int {
        return when {
            angle < 20 -> if (direction == Direction.LEFT) R.string.arrow_left_20 else R.string.arrow_right_20
            angle < 50 -> if (direction == Direction.LEFT) R.string.arrow_left_40 else R.string.arrow_right_40
            angle < 70 -> if (direction == Direction.LEFT) R.string.arrow_left_60 else R.string.arrow_right_60
            angle < 110 -> if (direction == Direction.LEFT) R.string.arrow_left_90 else R.string.arrow_right_90
            angle < 160 -> if (direction == Direction.LEFT) R.string.arrow_left_120 else R.string.arrow_right_120
            else -> if (direction == Direction.LEFT) R.string.arrow_left_180 else R.string.arrow_right_180
        }
    }

    @DrawableRes
    fun icon(): Int {
        return when {
            angle < 20 -> if (direction == Direction.LEFT) R.drawable.arrow_left_20 else R.drawable.arrow_right_20
            angle < 50 -> if (direction == Direction.LEFT) R.drawable.arrow_left_40 else R.drawable.arrow_right_40
            angle < 70 -> if (direction == Direction.LEFT) R.drawable.arrow_left_60 else R.drawable.arrow_right_60
            angle < 110 -> if (direction == Direction.LEFT) R.drawable.arrow_left_90 else R.drawable.arrow_right_90
            angle < 160 -> if (direction == Direction.LEFT) R.drawable.arrow_left_120 else R.drawable.arrow_right_120
            else -> if (direction == Direction.LEFT) R.drawable.arrow_left_180 else R.drawable.arrow_right_180
        }
    }
}

