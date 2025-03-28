package com.example.safewoman.PoliceStation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safewoman.databinding.ActivityNearestHospitalBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class NearestPoliceStationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityNearestHospitalBinding
    private val policeStations = mutableListOf<PoliceStation>()
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearestHospitalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.recyclerViewHospitals.layoutManager = LinearLayoutManager(this)

        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLatitude = location.latitude
                userLongitude = location.longitude
                fetchHospitals(location)
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchHospitals(location: Location) {
        val overpassQuery = """
        [out:json];
        node["amenity"="police"](around:100000, ${location.latitude}, ${location.longitude});
        out;
    """.trimIndent()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://overpass-api.de/api/interpreter?data=${Uri.encode(overpassQuery)}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTP_ERROR", "Failed to fetch police stations", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("HTTP_ERROR", "Unexpected response: $response")
                    return
                }

                response.body?.let { responseBody ->
                    val json = JSONObject(responseBody.string())
                    val elements = json.getJSONArray("elements")

                    policeStations.clear()
                    for (i in 0 until elements.length()) {
                        val policeStation = elements.getJSONObject(i)
                        val tags = policeStation.optJSONObject("tags")

                        val name = tags?.optString("name", "Unnamed Police Station") ?: "Unnamed Police Station"
                        var address = tags?.optString("addr:full", "")?.takeIf { it.isNotEmpty() } ?: ""

                        val latitude = policeStation.optDouble("lat", 0.0)
                        val longitude = policeStation.optDouble("lon", 0.0)

                        val distance = FloatArray(1)
                        Location.distanceBetween(
                            userLatitude, userLongitude,
                            latitude, longitude,
                            distance
                        )

                        val hospital = PoliceStation(name, address, latitude, longitude, distance[0])

                        if (address.isEmpty()) {
                            fetchAddressFromCoordinates(latitude, longitude) { fetchedAddress ->
                                hospital.address = fetchedAddress
                                runOnUiThread {
                                    binding.recyclerViewHospitals.adapter?.notifyDataSetChanged()
                                    Toast.makeText(this@NearestPoliceStationActivity, "Fetched Address: $fetchedAddress", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        policeStations.add(hospital)
                    }

                    policeStations.sortBy { it.distance }

                    runOnUiThread {
                        binding.recyclerViewHospitals.adapter = PoliceStationAdapter(policeStations, userLatitude, userLongitude)
                    }
                }
            }
        })
    }

    private fun fetchAddressFromCoordinates(lat: Double, lon: Double, onAddressFound: (String) -> Unit) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                val fullAddress = addresses[0].getAddressLine(0)
                runOnUiThread {
                    onAddressFound(fullAddress)
                    Toast.makeText(this, "Address: $fullAddress", Toast.LENGTH_SHORT).show()
                }
            } else {
                runOnUiThread {
                    onAddressFound("Address not available")
                    Toast.makeText(this, "Address not available", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            Log.e("GEO_ERROR", "Reverse geocoding failed", e)
            runOnUiThread {
                onAddressFound("Address not available")
                Toast.makeText(this, "Address not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
