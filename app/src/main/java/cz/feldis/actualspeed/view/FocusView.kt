package cz.feldis.actualspeed.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import cz.feldis.actualspeed.R

class FocusView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var borderPaint: Paint? = null
    private val pathPaint = Paint().apply {
        color = Color.TRANSPARENT
        strokeWidth = 10F
    }
    private val path = Path()
    private var gradientColor: Int = Color.BLACK

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.FocusView, 0, 0).apply {
            try {
                gradientColor = getColor(R.styleable.FocusView_gradientColor, Color.BLACK)
            } finally {
                recycle()
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if(borderPaint == null) {
            borderPaint = Paint().apply {
                color = Color.WHITE
                strokeWidth = 1F
                style = Paint.Style.FILL_AND_STROKE
                shader = RadialGradient(width / 2F, height / 2F, height / 1.5F, Color.TRANSPARENT, gradientColor, Shader.TileMode.MIRROR)
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.apply {
            path.reset()
            path.addCircle(width / 2F, height / 2F, height / 2F, Path.Direction.CW)
            path.fillType = Path.FillType.INVERSE_EVEN_ODD

            borderPaint?.let {
                drawCircle(width / 2F, height / 2F, height / 2F, it)
            }

            drawPath(path, pathPaint)
            clipPath(path)

            drawColor(gradientColor)
        }
    }
}