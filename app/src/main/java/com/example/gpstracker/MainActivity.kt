package com.example.gpstracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.gpstracker.screens.LocationTrackingScreen
import com.example.gpstracker.utils.Constant.REQUEST_CODE_LOCATION_SETTINGS
import com.example.gpstracker.utils.Constant.locationFastestInterval
import com.example.gpstracker.utils.Constant.locationInterval
import com.example.gpstracker.utils.Constant.locationMaxWaitTime
import com.example.gpstracker.utils.LocationItem
import com.example.gpstracker.utils.convertLongToTime
import com.example.gpstracker.utils.requestLocationPermission
import com.google.android.gms.location.*
import java.util.*

class MainActivity : ComponentActivity() {
    private var locationList = mutableStateListOf<LocationItem>()
    private var statusState = mutableStateOf("")
    private var logs = mutableStateListOf<String>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {

            LocationTrackingScreen(locationList = locationList,
                stopObservingLocation = {
                    stopLocationUpdates()
                },
                status = statusState,
                logs = logs,
                onRequestGps = {

                    val locationManager =
                        getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        startLocationUpdates()

                        logs.add("Gps available")
                    } else {
                        // GPS is still not enabled, show an error message or exit the app
                        logs.add("Gps not provided")
                        requestLocationSettings()
                    }
                })
        }
    }

    private val locationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
            .setMinUpdateIntervalMillis(locationFastestInterval)
            .setMaxUpdateDelayMillis(locationMaxWaitTime)
            .build()

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation

            if (location == null) {
                logs.add("locationResult is empty")
                return
            }

            // Update the UI with the location data
            val locationInfo =
                "LatLng-> ${location.latitude} - ${location.longitude}" + "\n" +
                        "Speed-> ${location.speed}" + "\n" +
                        "Altitude-> ${location.altitude}" + "\n" +
                        "Accuracy-> ${location.accuracy} - ${location.longitude}"

            locationList.add(
                LocationItem(
                    time = convertLongToTime(location.time),
                    locationInfo = locationInfo
                )
            )
            logs.add("onLocationChanged->${location.latitude}-${location.longitude}")
        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)


            logs.add("onLocationAvailability: isLocationAvailable->${p0.isLocationAvailable}")
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        requestLocationPermission(this)
    }

    private fun requestLocationSettings() {
        val locationSettingsIntent = Intent(ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(locationSettingsIntent, REQUEST_CODE_LOCATION_SETTINGS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOCATION_SETTINGS) {
            // The user has returned from the location settings activity.
            // Check if GPS is now enabled and start location updates if it is.
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                startLocationUpdates()
                logs.add("Gps available")
            } else {
                // GPS is still not enabled, show an error message or exit the app
                logs.add("Gps not provided")
            }
        }
    }
}