package com.example.sniffout.ui.Map

import LocationViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sniffout.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.example.sniffout.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.runBlocking

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var locationViewModel: LocationViewModel
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //check for location permission and if not ask for location
        runBlocking {
            checkLocationPermissionAndRequest()
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isCompassEnabled=true

       val viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        viewModel.currentLocation.observe(this) { location ->
            //Update Location Ui
            updateMap(location)
        }

        viewModel.fetchCurrentLocation()

    }
     fun updateMap(location: LatLng) {
        mMap.clear() // Clear existing markers on the map

        // Add marker for the new location
        val markerOptions = MarkerOptions()
            .position(location)
            .title("Your Location")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        mMap.addMarker(markerOptions)

        // Move the camera to the new location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
//         getnearbyplaces(location)
    }
//    private fun getnearbyplaces(location: LatLng) {
//        val radius = 2000
//        val apiKey="AIzaSyA3zeQUA47kyCgI5XJFJMn6zybxb3jPqeQ"
//        viewModel.loadPlaces(location.toString(), 1000, apiKey)
////        println("We called view model")
//        viewModel.places.observe(this) { places ->
//            for (place in places) {
//                val markerOptions = MarkerOptions()
//                    .position(LatLng(place.geometry.location.lat, place.geometry.location.lng))
//                    .title(place.name)
//                mMap.addMarker(markerOptions)
//            }
//        }
//    }

    suspend fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

     fun handlePermissionRequestResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {
                    showToast("Location permission denied")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    suspend fun checkLocationPermissionAndRequest() {
        if (checkLocationPermissions()) {
            // Location permission is already granted
            // You can proceed with location-related operations here
            checkGpsAndPromptEnable()
        } else {
            // Location permission is not granted, request it
            requestLocationPermission()
        }
    }

    override  fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handlePermissionRequestResult(requestCode, permissions, grantResults)
    }

    suspend fun checkGpsAndPromptEnable() {
        if (!isGpsEnabled()) {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }
    suspend fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

}
