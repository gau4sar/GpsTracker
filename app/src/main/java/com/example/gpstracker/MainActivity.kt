package com.example.gpstracker

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.gpstracker.screens.LocationTrackingScreen
import com.example.gpstracker.utils.Constant.REQUEST_CODE_LOCATION_SETTINGS
import com.example.gpstracker.utils.LocationCollector
import com.example.gpstracker.utils.LocationItem
import com.example.gpstracker.utils.requestLocationPermission
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : ComponentActivity() {

    private lateinit var locationCollector: LocationCollector

    private var locationList = mutableStateListOf<LocationItem>()
    private var statusState = mutableStateOf("")
    private var logs = mutableStateListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationCollector = LocationCollector(
            logs = logs,
            context = this,
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
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

                override fun onProviderEnabled(provider: String) {
                    logs.add("onProviderEnabled $provider")
                }

                override fun onProviderDisabled(provider: String) {
                    logs.add("onProviderDisabled $provider")
                }

                override fun onStatusChanged(
                    provider: String?,
                    status: Int,
                    extras: Bundle?
                ) {
                    provider?.let {
                        logs.add("provider-> $it status-> $status extras-> $extras")
                    }
                }
            }
        )

        setContent {


            LocationTrackingScreen(locationList = locationList, startObservingLocation = {
                locationCollector.start()
            }, stopObservingLocation = {
                locationCollector.stop()
            },
                status = statusState,
                logs = logs,
                onRequestGps = {

                    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationCollector.start()
                        logs.add("Gps available")
                    } else {
                        logs.add("Gps not provided")
                        requestLocationSettings()
                    }
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCollector.stop()
    }

    override fun onResume() {
        super.onResume()
        requestLocationPermission(this)
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
        return format.format(date)
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
                locationCollector.start()
                logs.add("Gps available")
            } else {
                // GPS is still not enabled, show an error message or exit the app
                logs.add("Gps not provided")
            }
        }
    }
}