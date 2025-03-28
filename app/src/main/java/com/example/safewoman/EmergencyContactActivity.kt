package com.example.safewoman

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EmergencyContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contact)

        val tvEmergencyContacts: TextView = findViewById(R.id.tvEmergencyContacts)
        val sharedPrefs = getSharedPreferences("emergencyContactsPrefs", Context.MODE_PRIVATE)
        val contactsSet = sharedPrefs.getStringSet("emergencyContacts", setOf())

        tvEmergencyContacts.text = contactsSet?.joinToString("\n") ?: "No contacts added"
    }
}
