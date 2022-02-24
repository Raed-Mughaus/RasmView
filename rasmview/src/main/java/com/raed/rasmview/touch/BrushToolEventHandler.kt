package com.raed.rasmview.touch

import android.graphics.Canvas
import android.graphics.Rect
import android.view.MotionEvent
import com.raed.rasmview.brushtool.BrushToolFactory
import com.raed.rasmview.brushtool.model.TouchEvent
import com.raed.rasmview.RasmContext
import com.raed.rasmview.actions.DrawBitmapAction
import kotlin.math.max
import kotlin.math.min

class BrushToolEventHandler(
    private val rasmContext: RasmContext,
): MotionEventHandler {

    private val brushTool = BrushToolFactory(rasmContext.brushToolBitmaps)
        .create(rasmContext.brushColor, rasmContext.brushConfig)
    private val touchEvent = TouchEvent()
    private var pointerId = 0
    private var ignoreEvents = false
    
    override fun handleFirstTouch(event: MotionEvent) {
        val pointerIdx = 0
        pointerId = event.getPointerId(pointerIdx)

        touchEvent.set(event, pointerIdx, 0)
        startDrawing(touchEvent)

        for (historyPosition in 1..event.historySize) {
            touchEvent.set(event, pointerIdx, historyPosition)
            brushTool.continueDrawing(touchEvent)
        }
    }

    override fun handleTouch(event: MotionEvent) {
        if (ignoreEvents) {
            return
        }
        val pointerIdx = event.findPointerIndex(pointerId)

        for (i in 0 until event.historySize) {
            touchEvent.set(event, pointerIdx, i)
            brushTool.continueDrawing(touchEvent)
        }

        touchEvent.set(event, pointerIdx, event.historySize)
        if (event.isActionUp(pointerIdx)) {
            ignoreEvents = true
            endDrawing(touchEvent)
        } else {
            brushTool.continueDrawing(touchEvent)
        }
    }

    override fun handleLastTouch(event: MotionEvent) {
        if (ignoreEvents) {
            return
        }
        val pointerIdx = event.findPointerIndex(pointerId)

        for (i in 0 until event.historySize) {
            touchEvent.set(event, pointerIdx, i)
            brushTool.continueDrawing(touchEvent)
        }

        touchEvent.set(event, pointerIdx, event.historySize)
        endDrawing(touchEvent)
    }

    private fun startDrawing(event: TouchEvent) {
        rasmContext.isBrushToolActive = true
        rasmContext.brushToolBitmaps.resultBitmap.eraseColor(0)
        rasmContext.brushToolBitmaps.strokeBitmap.eraseColor(0)
        if (rasmContext.brushConfig.isEraser) {
            Canvas(rasmContext.brushToolBitmaps.strokeBitmap)
                .drawBitmap(
                    rasmContext.brushToolBitmaps.layerBitmap,
                    0f, 0f, null,
                )
        }
        brushTool.startDrawing(event)
    }

    private fun endDrawing(event: TouchEvent) {
        brushTool.endDrawing(event)
        rasmContext.isBrushToolActive = false

        val strokeBoundary = Rect(brushTool.strokeBoundary)
        val resultBitmap = rasmContext.brushToolBitmaps.resultBitmap
        strokeBoundary.left = max(strokeBoundary.left, 0)
        strokeBoundary.top = max(strokeBoundary.top, 0)
        strokeBoundary.right = min(strokeBoundary.right, resultBitmap.width)
        strokeBoundary.bottom = min(strokeBoundary.bottom, resultBitmap.height)
        if (strokeBoundary.width() != 0 && strokeBoundary.height() != 0) {
            rasmContext.state.update(
                DrawBitmapAction(
                    resultBitmap,
                    strokeBoundary,
                    strokeBoundary,
                ),
            )
        }
    }

    override fun cancel() {
        if (!ignoreEvents) {
            brushTool.cancel()
        }
    }

}

private fun TouchEvent.set(event: MotionEvent, pointerIdx: Int, historyPosition: Int) {
    require(historyPosition <= event.historySize)
    if (historyPosition == event.historySize) {
        x = event.getX(pointerIdx)
        y = event.getY(pointerIdx)
    } else {
        x = event.getHistoricalX(pointerIdx, historyPosition)
        y = event.getHistoricalY(pointerIdx, historyPosition)
    }
}


private fun MotionEvent.isActionUp(pointerIdx: Int): Boolean {
    return actionMasked == MotionEvent.ACTION_UP ||
            (actionMasked == MotionEvent.ACTION_POINTER_UP && pointerIdx == actionIndex)
}
