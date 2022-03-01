package com.raed.rasmview

import android.graphics.*
import android.graphics.Bitmap.Config.ARGB_8888
import com.raed.rasmview.actions.ChangeBackgroundAction
import com.raed.rasmview.actions.ClearAction
import com.raed.rasmview.brushtool.BrushToolBitmaps
import com.raed.rasmview.brushtool.model.BrushConfig
import com.raed.rasmview.renderer.RasmOnScreenRendererFactory
import com.raed.rasmview.state.RasmState


class RasmContext {

    private var nullableBrushToolBitmaps: BrushToolBitmaps? = null
        set(value) {
            require(field == null)
            require(value != null)
            field = value
        }
    internal val brushToolBitmaps get() = nullableBrushToolBitmaps!!
    internal var isBrushToolActive = false
    val isInitialized get() = nullableBrushToolBitmaps != null
    val rasmWidth get() = brushToolBitmaps.layerBitmap.width
    val rasmHeight get() = brushToolBitmaps.layerBitmap.height
    val state = RasmState(this)
    val transformation = Matrix()
    var brushConfig = BrushConfig()
    var brushColor = 0xff2187bb.toInt()
    var rotationEnabled = false
    internal var backgroundColor = -1

    fun init(
        drawingWidth: Int,
        drawingHeight: Int,
    ) = init(Bitmap.createBitmap(drawingWidth, drawingHeight, ARGB_8888))

    fun init(drawing: Bitmap) {
        nullableBrushToolBitmaps = BrushToolBitmaps.createFromDrawing(drawing)
    }

    fun exportRasm(): Bitmap {
        val rasm = Bitmap.createBitmap(rasmWidth, rasmHeight, ARGB_8888)
        val rasmRenderer = RasmOnScreenRendererFactory().createOffscreenRenderer(this)
        rasmRenderer.render(Canvas(rasm))
        return rasm
    }

    fun clear() {
        state.update(
            ClearAction(),
        )
    }

    fun setBackgroundColor(color: Int) {
        state.update(
            ChangeBackgroundAction(color),
        )
    }

    internal fun resetTransformation(containerWidth: Int, containerHeight: Int) {
        transformation.setRectToRect(
            RectF(0F, 0F, rasmWidth.toFloat(), rasmHeight.toFloat()),
            RectF(0f, 0f, containerWidth.toFloat(), containerHeight.toFloat()),
            Matrix.ScaleToFit.CENTER,
        )
    }

}
