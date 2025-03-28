package com.example.safewoman.volumesafety

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.example.safewoman.R

class VolumeService : Service() {
    private var volumePressCount = 0
    private var lastPressTime: Long = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("SafeWomanPrefs", MODE_PRIVATE)
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SafeWoman", "🔄 Volume Service Started")
        return START_STICKY // Ensures service restarts if killed
    }

    private fun startForegroundService() {
        val channelId = "safewoman_channel"
        val channelName = "SafeWoman Emergency Service"
        val notificationManager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SafeWoman Active")
            .setContentText("Listening for emergency volume button presses...")
            .setSmallIcon(R.drawable.ic_map)
            .build()
        startForeground(1, notification)
    }

    fun onVolumeButtonPressed() {
        val currentTime = System.currentTimeMillis()

        // Reset counter if last press was more than 5 seconds ago
        if (currentTime - lastPressTime > 5000) {
            volumePressCount = 0
        }

        volumePressCount++
        lastPressTime = currentTime

        Log.d("SafeWoman", "📢 Volume button pressed $volumePressCount times")

        if (volumePressCount >= 5) {
            sendSOSMessage()
            volumePressCount = 0 // Reset after sending
        }
    }

    private fun sendSOSMessage() {
        getLastKnownLocation { location ->
            val emergencyNumber = sharedPreferences.getString("emergency_contact", "9373368067") ?: "9373368067"
            val message = if (location != null) {
                "🚨 Emergency! Need help. My location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "🚨 Emergency! Need help. Unable to fetch location."
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(emergencyNumber, null, message, null, null)
                Toast.makeText(this, "🚨 SOS Message Sent!", Toast.LENGTH_LONG).show()
                Log.d("SafeWoman", "📩 SMS sent to $emergencyNumber: $message")
            } else {
                Log.e("SafeWoman", "❌ SMS permission not granted!")
                Toast.makeText(this, "❌ Permission required to send SMS!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getLastKnownLocation(callback: (Location?) -> Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback(location)
            } else {
                Log.e("SafeWoman", "⚠️ Unable to retrieve location.")
                callback(null)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
