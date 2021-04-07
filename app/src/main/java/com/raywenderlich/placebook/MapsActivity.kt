package com.raywenderlich.placebook

import android.Manifest.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private lateinit var placesClient: PlacesClient

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
        setupPlacesClient()
    }

    // Part of OnMapReadyCallback interface
    override fun onMapReady(googleMap: GoogleMap) {
        // Initialize the map
        map = googleMap

        // Assign adapter
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))

        getCurrentLocation()

        // POI - Points Of Interest - gives info on places tapped (pop-up)
        map.setOnPoiClickListener { displayPOI(it) }
    }

    // Creates PlacesClient
    private fun setupPlacesClient() {
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    // Retrieve place details
    private fun displayPOI(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        // 1 Retrieve Place ID
        val placeId = pointOfInterest.placeId

        //2
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        // 3
        val request = FetchPlaceRequest
            .builder(placeId, placeFields)
            .build()

        // 4 - Callbacks
        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            // 5 fetches place and phone number
            val place = response.place
//            Toast.makeText(
//                this,
//                "${place.name}, " +
//                        "${place.phoneNumber}",
//                Toast.LENGTH_LONG
//            ).show()
            displayPoiGetPhotoStep(place)
        }.addOnFailureListener { exception ->
            // 6
            if (exception is ApiException) {
                val statusCode = exception.statusCode
                Log.e(
                    TAG, "Place Not found: " + exception.message + ", " +
                            "statusCode: " + statusCode
                )
            }
        }
    }

    // Get photo for selected place
    private fun displayPoiGetPhotoStep(place: Place) {
        // 1
        val photoMetadata = place
            .getPhotoMetadatas()?.get(0)

        //2
        if (photoMetadata == null) {
            displayPoiDisplayStep(place, null)
            return
        }

        // 3
        val photoRequest = FetchPhotoRequest
            .builder(photoMetadata)
                // Set max width and height - necessary
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))  // dimens.xml
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height)) //dimens.xml
            .build()

        //4 - Callbacks
        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener { fetchPhotoResponse ->
                val bitmap = fetchPhotoResponse.bitmap
                displayPoiDisplayStep(place, bitmap)
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(TAG,
                        "Place not found: " +
                               exception.message + ", " +
                               "statusCode: " + statusCode)
                }
            }
    }

    // Displays marker with place and photo
    private fun displayPoiDisplayStep(place:Place, photo: Bitmap?) {
        map.addMarker(MarkerOptions()
            .position(place.latLng as LatLng)
            .title(place.name)
            .snippet(place.phoneNumber))
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