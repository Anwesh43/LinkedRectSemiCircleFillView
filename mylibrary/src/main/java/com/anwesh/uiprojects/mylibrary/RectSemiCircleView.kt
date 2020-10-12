package com.anwesh.uiprojects.mylibrary

/**
 * Created by anweshmishra on 13/10/20.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Path
import android.graphics.Color
import android.content.Context

val parts : Int = 3
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 2.9f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val colors : Array<Int> = arrayOf(
        "#F44336",
        "#2196F3",
        "#FFC107",
        "#3F51B5",
        "#FF5722"
).map {
    Color.parseColor(it)
}.toTypedArray()
val deg : Float = 180f


fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawRectSemiCircleFillPath(scale : Float, w : Float, h : Float, r : Float, paint : Paint) {
    save()
    val path : Path = Path()
    path.moveTo(- r, h / 2)
    path.lineTo(-r, 0f)
    path.arcTo(RectF(-r, -r, r, r), deg, deg, false)
    path.lineTo(r, h / 2)
    path.lineTo(-r, h / 2)
    clipPath(path)
    drawRect(RectF(-r, h / 2 * (1 - scale), r, h / 2), paint)
    restore()
}

fun Canvas.drawRectSemiCircle(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / sizeFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    save()
    translate(w / 2, h / 2)
    for (j in 0..1) {
        save()
        translate(w / 2 - size / 2  + size * j, h)
        drawLine(0f, 0f, 0f, h / 2 * sf1, paint)
        restore()
    }
    drawRectSemiCircleFillPath(sf2, w, h, size / 2, paint)
    restore()
}

fun Canvas.drawRSCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawRectSemiCircle(scale, w, h, paint)
}

class RectSemiCircleView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}