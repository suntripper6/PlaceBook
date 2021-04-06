package com.raywenderlich.placebook

import android.Manifest.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    // Begin fused location client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps) // Looks at layout: activity_maps.xml
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment // host main UI; displays map and access to GoogleMap object
        mapFragment.getMapAsync(this)  // sets up map and creating GoogleMap object

        setupLocationClient()
    }

    // Part of OnMapReadyCallback interface
    override fun onMapReady(googleMap: GoogleMap) {
        // Initialize the map
        map = googleMap

        getCurrentLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    // Implementation of the fused location
    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    // Implementation for permission of resource hungry fine location
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
    }

    // Get user's current location moves map and centers (with marker)
    private fun getCurrentLocation() {
        // 1
        if (ActivityCompat.checkSelfPermission(this,
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 2
            requestLocationPermission()
        } else {
            map.isMyLocationEnabled = true
            // 3
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null)  {
                    // 4
                    val latLng = LatLng(location.latitude, location.longitude)
                    // 5
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    // 6
                    map.moveCamera(update)
                } else {
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION = 1  // Code passed to requestPermissions()
        private const val TAG = "MapsActivity"  // Prints info to Logcat
    }
}