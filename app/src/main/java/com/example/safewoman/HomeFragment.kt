package com.example.safewoman
import ImageSliderAdapter
import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.safewoman.Hospital.NearestHospitalActivity
import com.example.safewoman.PoliceStation.NearestPoliceStationActivity

import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.safewoman.databinding.FragmentHomeBinding
import com.example.safewoman.volumesafety.VolumeService

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private val emergencyContacts = mutableListOf<String>()
    private val PICK_CONTACT_REQUEST = 1
    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("SafeWomanPrefs", 0)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        loadEmergencyContacts()
        requestPermissions()
        setupDrawerAnimation()

        binding.btnEmergencyContact.setOnClickListener { pickContact() }
        binding.btnSOS.setOnClickListener { sendAccurateLocationToEmergencyContacts() }
        binding.cardPoliceStation.setOnClickListener {
            Toast.makeText(requireContext(), "Opening Nearest Police Stations", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), NearestPoliceStationActivity::class.java)
            startActivity(intent)
        }

        binding.cardHospiital.setOnClickListener {
            Toast.makeText(requireContext(), "Opening Nearest Hospital", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), NearestHospitalActivity::class.java)
            startActivity(intent)
        }
        setupImageSlider()
        return binding.root

        val serviceIntent = Intent(requireContext(), VolumeService::class.java)
        requireContext().startForegroundService(serviceIntent) // Start the foreground service

        // Open NewsActivity when clicking on Nearest Police Station button
//        binding.btnNearestPoliceStations.setOnClickListener {
//            Toast.makeText(requireContext(), "Button Clicked", Toast.LENGTH_SHORT).show()
//            val intent = Intent(requireContext(), NewsActivity::class.java)
//            startActivity(intent)
//        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tutorials.setOnClickListener {
            val intent = Intent(requireContext(), NewsActivity::class.java)
            startActivity(intent)
        }

            requestPermissions()


    }
    private fun requestPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.SEND_SMS)
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.READ_CONTACTS)
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResult(intent, this@HomeFragment.PICK_CONTACT_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this@HomeFragment.PICK_CONTACT_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            val contactUri: Uri = data?.data ?: return
            val cursor = requireActivity().contentResolver.query(contactUri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val phoneIndex =
                        it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val phoneNumber = it.getString(phoneIndex)?.replace("\\s".toRegex(), "")
                    if (!phoneNumber.isNullOrEmpty()) {
                        emergencyContacts.add(phoneNumber)
                        saveEmergencyContacts()
                        Toast.makeText(
                            requireContext(),
                            "Contact Added: $phoneNumber",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Invalid phone number!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun sendAccurateLocationToEmergencyContacts() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude
                            val message =
                                "Help me! My location: https://maps.google.com/?q=$latitude,$longitude"
                            sendSmsToEmergencyContacts(message)
                            saveLocationToFirebase(latitude, longitude)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Unable to retrieve location.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                Looper.getMainLooper()
            )
        } else {
            Toast.makeText(requireContext(), "Location permission not granted.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun sendSmsToEmergencyContacts(message: String) {
        val smsManager = SmsManager.getDefault()
        for (contact in emergencyContacts) {
            try {
                smsManager.sendTextMessage(contact, null, message, null, null)
                Toast.makeText(requireContext(), "SMS sent to $contact", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to send SMS to $contact",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveLocationToFirebase(latitude: Double, longitude: Double) {
        val userId = auth.currentUser?.uid ?: return
        val ref =
            FirebaseDatabase.getInstance().getReference("users").child(userId).child("location")
        val locationData = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to System.currentTimeMillis()
        )
        ref.setValue(locationData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Location saved successfully!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to save location to Firebase.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveEmergencyContacts() {
        val editor = sharedPreferences.edit()
        editor.putStringSet("emergencyContacts", emergencyContacts.toSet())
        editor.apply()
    }

    private fun loadEmergencyContacts() {
        val savedContacts = sharedPreferences.getStringSet("emergencyContacts", emptySet())
        emergencyContacts.clear()
        emergencyContacts.addAll(savedContacts.orEmpty())
    }

    private fun setupImageSlider() {
        val images = listOf(R.drawable.w1, R.drawable.w2, R.drawable.w3)
        val adapter = ImageSliderAdapter(images)
        binding.imageSlider.adapter = adapter

        val handler = Handler(Looper.getMainLooper())
        var currentPage = 0
        val runnable = object : Runnable {
            override fun run() {
                binding.imageSlider.setCurrentItem(currentPage, true)
                currentPage = (currentPage + 1) % images.size
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(runnable)
    }

    private fun setupDrawerAnimation() {
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                binding.imageSlider?.apply {
                    scaleX = 1 - (slideOffset / 6)
                    scaleY = 1 - (slideOffset / 6)
                }
            }
        })
    }
}

