package com.example.safewoman.Hospital

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.safewoman.databinding.ActivityViewHospitalBinding
import kotlin.math.*

class ViewHospitalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewHospitalBinding
    private var hospitalLatitude = 0.0
    private var hospitalLongitude = 0.0
    private var userLatitude = 0.0
    private var userLongitude = 0.0
    private var hospitalName = "Unknown Hospital"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewHospitalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hospitalName = intent.getStringExtra("hospitalName") ?: "Unknown Hospital"
        val hospitalAddress = intent.getStringExtra("hospitalAddress") ?: "Unknown Address"
        hospitalLatitude = intent.getDoubleExtra("hospitalLatitude", 0.0)
        hospitalLongitude = intent.getDoubleExtra("hospitalLongitude", 0.0)
        userLatitude = intent.getDoubleExtra("userLatitude", 0.0)
        userLongitude = intent.getDoubleExtra("userLongitude", 0.0)

        binding.tvHospitalName.text = hospitalName
        binding.tvHospitalAddress.text = hospitalAddress

        Log.d("LocationDebug", "User Lat: $userLatitude, User Lon: $userLongitude")
        Log.d("LocationDebug", "Hospital Lat: $hospitalLatitude, Hospital Lon: $hospitalLongitude")

        val distance = calculateDistance(userLatitude, userLongitude, hospitalLatitude, hospitalLongitude)
        binding.tvHospitalDistance.text = "Distance: ${String.format("%.2f", distance)} km"

        // ✅ Load Google Maps in WebView
        loadGoogleMapsWebView()

        binding.btnViewOnMap.setOnClickListener {
            openGoogleMapsApp()
        }
    }

    // ✅ Load Google Maps instead of GoMap
    private fun loadGoogleMapsWebView() {
        val webView: WebView = binding.mapView  // Use View Binding
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        val googleMapsUrl = "https://www.google.com/maps?q=$hospitalLatitude,$hospitalLongitude"
        webView.loadUrl(googleMapsUrl)
    }

    // ✅ Open Google Maps app when the button is clicked
    private fun openGoogleMapsApp() {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$hospitalLatitude,$hospitalLongitude")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps") // Opens Google Maps app if installed
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // If Google Maps app is not installed, open in browser
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString())))
        }
    }

    private fun calculateDistance(userLat: Double, userLon: Double, hospitalLat: Double, hospitalLon: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(hospitalLat - userLat)
        val dLon = Math.toRadians(hospitalLon - userLon)

        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(userLat)) * cos(Math.toRadians(hospitalLat)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}
