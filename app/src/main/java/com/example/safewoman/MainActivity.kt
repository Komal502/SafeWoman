package com.example.safewoman

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.safewoman.volumesafety.VolumeService
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Customize the background color of the activity
        window.decorView.setBackgroundColor(resources.getColor(androidx.cardview.R.color.cardview_light_background))

        // Set up BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        val serviceIntent = Intent(this, VolumeService::class.java)
        startService(serviceIntent)  // Starts the foreground service

        // Apply a custom style or animation to the BottomNavigationView
        bottomNavigationView.setBackgroundColor(resources.getColor(R.color.white))
        bottomNavigationView.setItemIconTintList(null) // Disable icon tinting
        bottomNavigationView.setItemTextColor(resources.getColorStateList(androidx.cardview.R.color.cardview_light_background))

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    loadFragment(HomeFragment()) // Home Fragment
                    true
                }
                R.id.map -> {
                    loadFragment(MapFragment()) // Map Fragment
                    true
                }
                R.id.community -> {
                    loadFragment(CommunityFragment()) // Community Fragment
                    true
                }
                R.id.criminals -> {
                    loadFragment(CriminalFragment()) // Criminal Fragment
                    true
                }
                else -> false
            }
        }

        // Set the default fragment (optional)
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.home // Set default item
        }
    }

    // Function to load a generic Fragment
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out) // Add fade transition
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
