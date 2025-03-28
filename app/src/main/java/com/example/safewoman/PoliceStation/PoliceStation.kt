package com.example.safewoman.PoliceStation

data class PoliceStation(
    val name: String,
    var address: String,  // Marked `var` so we can update it dynamically
    val latitude: Double,
    val longitude: Double,
    val distance: Float
)
