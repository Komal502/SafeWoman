package com.example.safewoman
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.safewoman.BuildConfig // ✅ Correct import
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class PoliceStationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var currentLocation: Location? = null
    private val apiKey = BuildConfig.MAPS_API_KEY // ✅ Corrected API Key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_police_station)

        // Initialize Google Maps
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        placesClient = Places.createClient(this)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        // Check location permission and fetch location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLocation = location
                val userLatLng = LatLng(location.latitude, location.longitude)

                // Move camera to user location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))

                // Add marker for user location
                mMap.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )

                // Show nearby police stations
                getNearbyPoliceStations(userLatLng)
            } else {
                Log.e("PoliceStationActivity", "Location is null. Retrying...")
            }
        }.addOnFailureListener {
            Log.e("PoliceStationActivity", "Failed to get location: ${it.message}")
        }
    }

    private fun getNearbyPoliceStations(userLatLng: LatLng) {
        val radius = 5000  // 5 km range
        val placeType = "police"
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${userLatLng.latitude},${userLatLng.longitude}" +
                "&radius=$radius" +
                "&type=$placeType" +
                "&key=$apiKey"

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            handlePlacesResponse(response)
        }, { error ->
            Log.e("PoliceStationActivity", "Volley Error: ${error.message}")
        })

        queue.add(request)
    }

    private fun handlePlacesResponse(response: JSONObject) {
        try {
            val results = response.getJSONArray("results")

            if (results.length() == 0) {
                Log.w("PoliceStationActivity", "No police stations found nearby.")
            }

            for (i in 0 until results.length()) {
                val place = results.getJSONObject(i)
                val location = place.getJSONObject("geometry").getJSONObject("location")
                val latLng = LatLng(location.getDouble("lat"), location.getDouble("lng"))
                val name = place.getString("name")

                // Add marker on map
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
            }
        } catch (e: Exception) {
            Log.e("PoliceStationActivity", "Error parsing places: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    Log.e("PoliceStationActivity", "Location permission denied.")
                }
            }
        }
    }
}
