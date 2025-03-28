package com.example.safewoman.PoliceStation

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.safewoman.R
import kotlin.math.*

class PoliceStationAdapter(
    private val policeStations: List<PoliceStation>,
    private val userLatitude: Double,
    private val userLongitude: Double
) : RecyclerView.Adapter<PoliceStationAdapter.PoliceStationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoliceStationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hospital, parent, false) // Using the same UI layout
        return PoliceStationViewHolder(view)
    }

    override fun onBindViewHolder(holder: PoliceStationViewHolder, position: Int) {
        val policeStation = policeStations[position]
        holder.tvPoliceName.text = policeStation.name
        holder.tvPoliceAddress.text = policeStation.address
        holder.tvPoliceDistance.text = "Distance: ${calculateDistance(policeStation.latitude, policeStation.longitude)} km"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ViewPoliceStationActivity::class.java).apply {
                putExtra("policeStationName", policeStation.name)
                putExtra("policeStationAddress", policeStation.address)
                putExtra("policeStationLatitude", policeStation.latitude)
                putExtra("policeStationLongitude", policeStation.longitude)
                putExtra("userLatitude", userLatitude)
                putExtra("userLongitude", userLongitude)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = policeStations.size

    class PoliceStationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPoliceName: TextView = itemView.findViewById(R.id.tvHospitalName)
        val tvPoliceAddress: TextView = itemView.findViewById(R.id.tvHospitalAddress)
        val tvPoliceDistance: TextView = itemView.findViewById(R.id.tvHospitalDistance)
    }

    // Calculate distance between two points (Haversine formula)
    private fun calculateDistance(stationLat: Double, stationLon: Double): String {
        val earthRadius = 6371.0 // Radius of Earth in kilometers
        val dLat = Math.toRadians(stationLat - userLatitude)
        val dLon = Math.toRadians(stationLon - userLongitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(userLatitude)) * cos(Math.toRadians(stationLat)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return String.format("%.2f", earthRadius * c) // Returns distance in km
    }
}
