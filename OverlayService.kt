package com.skills.fps

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var fpsMonitor: FpsMonitor
    private lateinit var fpsText: TextView

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.END
        params.x = 20
        params.y = 100

        // Create overlay layout
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_fps, null)
        fpsText = overlayView.findViewById(R.id.fpsLabel)

        // Allow drag
        overlayView.setOnTouchListener(object : View.OnTouchListener {
            private var lastX = 0f
            private var lastY = 0f
            private var initX = 0
            private var initY = 0

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX
                        lastY = event.rawY
                        initX = params.x
                        initY = params.y
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - lastX).toInt()
                        val dy = (event.rawY - lastY).toInt()
                        params.x = initX - dx
                        params.y = initY + dy
                        windowManager.updateViewLayout(overlayView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(overlayView, params)

        val refreshRate = resources.displayMetrics.refreshRate
        fpsMonitor = FpsMonitor(refreshRate) { fps ->
            runOnUiThread { fpsText.text = "FPS $fps" }
        }
        fpsMonitor.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        fpsMonitor.stop()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun runOnUiThread(action: () -> Unit) {
        android.os.Handler(mainLooper).post(action)
    }
}
