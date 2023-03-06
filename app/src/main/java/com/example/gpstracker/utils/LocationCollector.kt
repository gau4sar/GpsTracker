package com.example.gpstracker.utils

import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList

class LocationCollector(
    var logs: SnapshotStateList<String>,
    private val context: Context,
    private val locationListener: LocationListener,
) {
    private var locationManager: LocationManager? = null

    fun start() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        try {
            locationManager?.let{
                logs.add("locationManager is initialized")
            }
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500,
                0f,
                locationListener
            )
            logs.add("requestLocationUpdates called")
        } catch (ex: SecurityException) {
            logs.add("SecurityException $ex")
            logs.add("SecurityException ${ex.message}")
            logs.add("SecurityException ${ex.localizedMessage}")
            Log.e(TAG, "SecurityException failed to request location updates", ex)
        } catch (e: Exception) {
            logs.add("LocationCollector Exception $e")
            logs.add("LocationCollector Exception ${e.message}")
            logs.add("LocationCollector Exception ${e.localizedMessage}")
            Log.e(TAG, "Exception failed to request location updates", e)
        }
    }

    fun stop() {
        logs.add("requestLocationUpdates stop")
        locationManager?.removeUpdates(locationListener)
    }

    companion object {
        private const val TAG = "LocationCollector"
    }
}