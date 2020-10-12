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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RSCNode(var i : Int, val state : State = State()) {

        private var prev : RSCNode? = null
        private var next : RSCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = RSCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawRSCNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RSCNode {
            var curr : RSCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class RectSemiCircle(var i : Int) {

        private var curr : RSCNode = RSCNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RectSemiCircleView) {

        private val animator : Animator = Animator(view)
        private val rsc : RectSemiCircle = RectSemiCircle(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            rsc.draw(canvas, paint)
            animator.animate {
                rsc.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            rsc.startUpdating {
                animator.start()
            }
        }
    }
}