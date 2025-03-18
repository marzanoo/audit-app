package com.example.auditapp.signature

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileOutputStream

class SignatureView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    private val path = Path()
    private var signatureBitmap: Bitmap? = null
    private var lastX = 0f
    private var lastY = 0f
    private var hasSignature = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                lastX = x
                lastY = y
                hasSignature = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                lastX = x
                lastY = y
                invalidate()
                return true
            }
            else -> return false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    fun clearSignature() {
        path.reset()
        hasSignature = false
        invalidate()
    }

    fun hasSignature(): Boolean {
        return hasSignature
    }

    fun getSignatureBitmap(): Bitmap {
        if (signatureBitmap == null ||
            signatureBitmap?.width != width ||
            signatureBitmap?.height != height) {
            signatureBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(signatureBitmap!!)
        canvas.drawColor(Color.WHITE)
        draw(canvas)
        return signatureBitmap!!
    }

    fun saveSignature(context: Context, fileName: String): File {
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { output ->
            getSignatureBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
            output.flush()
        }
        return file
    }
}