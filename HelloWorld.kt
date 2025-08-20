package com.example.fpsoverlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import android.media.projection.MediaProjectionManager

class MainActivity : Activity() {

    private val REQ_OVERLAY = 1001
    private val REQ_CAPTURE = 1002
    private var projectionIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        val btn = Button(this).apply {
            text = "Start FPS Overlay"
            setOnClickListener { ensureOverlayThenCapture() }
        }
        root.addView(btn)
        setContentView(root)
    }

    private fun ensureOverlayThenCapture() {
        if (!Settings.canDrawOverlays(this)) {
            val i = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(i, REQ_OVERLAY)
        } else {
            requestScreenCapture()
        }
    }

    private fun requestScreenCapture() {
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_OVERLAY -> {
                if (Settings.canDrawOverlays(this)) requestScreenCapture()
                else Toast.makeText(this, "Overlay permission needed", Toast.LENGTH_SHORT).show()
            }
            REQ_CAPTURE -> {
                if (resultCode == RESULT_OK && data != null) {
                    projectionIntent = data
                    val svc = Intent(this, ScreenCaptureService::class.java).apply {
                        putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
                        putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, data)
                    }
                    if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc) else startService(svc)
                    Toast.makeText(this, "Overlay started. Open Free Fire!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}