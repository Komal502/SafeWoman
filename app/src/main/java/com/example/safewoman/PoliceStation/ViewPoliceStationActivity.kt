package com.example.safewoman.PoliceStation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.safewoman.databinding.ActivityViewPoliceStationBinding
import kotlin.math.*

class ViewPoliceStationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewPoliceStationBinding
    private var policeStationLatitude = 0.0
    private var policeStationLongitude = 0.0
    private var userLatitude = 0.0
    private var userLongitude = 0.0
    private var policeStationName = "Unknown Police Station"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewPoliceStationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Fetch Data from Intent
        policeStationName = intent.getStringExtra("policeStationName") ?: "Unknown Police Station"
        val policeStationAddress = intent.getStringExtra("policeStationAddress") ?: "Unknown Address"
        policeStationLatitude = intent.getDoubleExtra("policeStationLatitude", 0.0)
        policeStationLongitude = intent.getDoubleExtra("policeStationLongitude", 0.0)
        userLatitude = intent.getDoubleExtra("userLatitude", 0.0)
        userLongitude = intent.getDoubleExtra("userLongitude", 0.0)

        // ✅ Set UI Texts
        binding.tvPoliceStationName.text = policeStationName
        binding.tvPoliceStationAddress.text = policeStationAddress

        Log.d("LocationDebug", "User Lat: $userLatitude, User Lon: $userLongitude")
        Log.d("LocationDebug", "PoliceStation Lat: $policeStationLatitude, PoliceStation Lon: $policeStationLongitude")

        // ✅ Calculate & Display Distance
        val distance = calculateDistance(userLatitude, userLongitude, policeStationLatitude, policeStationLongitude)
        binding.tvPoliceStationDistance.text = "Distance: ${String.format("%.2f", distance)} km"

        // ✅ Load Google Maps in WebView
        loadGoogleMapsWebView()

        // ✅ Open Google Maps App when Button Clicked
        binding.btnViewOnMap.setOnClickListener {
            openGoogleMapsApp()
        }
    }

    // ✅ Load Google Maps instead of GoMap
    private fun loadGoogleMapsWebView() {
        val webView: WebView = binding.mapView  // Using View Binding
        webView.webViewClient = WebViewClient() // Ensures it loads inside WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true  // Enables storage for maps
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true

        val googleMapsUrl = "https://www.google.com/maps/search/?api=1&query=$policeStationLatitude,$policeStationLongitude"
        webView.loadUrl(googleMapsUrl)
    }

    // ✅ Open Google Maps app when the button is clicked
    private fun openGoogleMapsApp() {
        val uri = Uri.parse("geo:0,0?q=$policeStationLatitude,$policeStationLongitude($policeStationName)")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps") // Opens Google Maps app if installed
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // If Google Maps app is not installed, open in browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$policeStationLatitude,$policeStationLongitude"))
            startActivity(browserIntent)
        }
    }

    // ✅ Calculate distance between two points (Haversine formula)
    private fun calculateDistance(userLat: Double, userLon: Double, stationLat: Double, stationLon: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(stationLat - userLat)
        val dLon = Math.toRadians(stationLon - userLon)

        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(userLat)) * cos(Math.toRadians(stationLat)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}
