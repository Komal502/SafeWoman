package com.example.safewoman

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Load the map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Check permissions and get location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getUserLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun getUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                val userLatLng = LatLng(location.latitude, location.longitude)

                // Move camera to user location
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                // Add marker for user location
                googleMap.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )

                // Fetch and show nearby police stations
                fetchNearbyPoliceStations(location.latitude, location.longitude)
            } else {
                Log.e("MapsActivity", "Location is null. Please check GPS.")
            }
        }
    }

    private fun fetchNearbyPoliceStations(lat: Double, lng: Double) {
        val apiKey = "AIzaSyDgASCn00MOUJf_0dtj5TXDLrT5PVWvVoo"  // Replace with your API Key
        val radius = 5000  // 5 KM range
        val type = "police"
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$lat,$lng&radius=$radius&type=$type&key=$apiKey"

        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            try {
                val results = response.getJSONArray("results")

                for (i in 0 until results.length()) {
                    val place = results.getJSONObject(i)
                    val location = place.getJSONObject("geometry").getJSONObject("location")
                    val latLng = LatLng(location.getDouble("lat"), location.getDouble("lng"))
                    val name = place.getString("name")

                    // Add marker for police station
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                }

                if (results.length() == 0) {
                    Log.w("MapsActivity", "No police stations found nearby.")
                }

            } catch (e: Exception) {
                Log.e("MapsActivity", "Error parsing places: ${e.message}")
            }
        }, { error ->
            Log.e("MapsActivity", "Volley Error: ${error.message}")
        })

        queue.add(jsonObjectRequest)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation()
        } else {
            Log.e("MapsActivity", "Location permission denied.")
        }
    }
}
