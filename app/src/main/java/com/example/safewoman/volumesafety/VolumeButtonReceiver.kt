package com.example.safewoman.volumesafety

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class VolumeButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
            Log.d("SafeWoman", "🎛 Volume Button Press Detected")

            val serviceIntent = Intent(context, VolumeService::class.java)
            ContextCompat.startForegroundService(context!!, serviceIntent)

            val service = VolumeService()
            service.onVolumeButtonPressed()
        }
    }
}
