package com.example.gpstracker

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode
import com.amap.api.location.AMapLocationListener
import com.amap.api.location.AMapLocationQualityReport
import com.amap.api.maps.MapsInitializer
import com.example.gpstracker.screens.LocationTrackingScreen
import com.example.gpstracker.utils.Constant.REQUEST_CODE_LOCATION_SETTINGS
import com.example.gpstracker.utils.LocationItem
import com.example.gpstracker.utils.formatUTC
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private var locationList = mutableStateListOf<LocationItem>()
    private var statusState = mutableStateOf("")
    private var logs = mutableStateListOf<String>()

    //private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationClient: AMapLocationClient? = null
    private var locationOption: AMapLocationClientOption? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Todo
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)

        locationOption = AMapLocationClientOption()
        locationOption!!.locationMode = AMapLocationMode.Hight_Accuracy
        locationOption!!.geoLanguage = AMapLocationClientOption.GeoLanguage.EN


        // Set whether to display address information
        locationOption!!.isNeedAddress = true
        // Set whether to enable caching
        locationOption!!.isLocationCacheEnable = true

        // Set the time interval for sending positioning requests. The minimum value is 1000. If it is less than 1000, it will be calculated based on 1000.
        locationOption!!.interval = 500

        //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        initLocation()

        setContent {

            LocationTrackingScreen(locationList = locationList, startObservingLocation = {
                startLocationUpdates()
            }, stopObservingLocation = {
                //stopLocationUpdates()

                stopLocation()
            },
                status = statusState,
                logs = logs
                ,
                onRequestGps = {

                    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        startLocationUpdates()
                        startLocation()
                        logs.add("Gps available")
                    } else {
                        // GPS is still not enabled, show an error message or exit the app
                        logs.add("Gps not provided")
                        requestLocationSettings()
                        startLocation()
                    }
                })
        }
    }

    private fun privacyCompliance() {
        MapsInitializer.updatePrivacyShow(this, true, true)
        val spannable =
            SpannableStringBuilder("Dear user, thank you for your trust in XXX! According to the latest regulatory requirements, we have updated the Privacy Policy of XXX, and hereby explain the following:\n1. In order to provide you with basic transaction-related functions, we may collect and use necessary information;\n2. With your explicit authorization, we may obtain information such as your location (to provide you with information about nearby products, stores and promotions), and you have the right to refuse or cancel authorization;\n3. We will take advanced security measures in the industry to protect the security of your information;\n4. Without your consent, we will not obtain, share or provide your information from third parties;\n")
        spannable.setSpan(
            ForegroundColorSpan(Color.BLUE),
            35,
            42,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        AlertDialog.Builder(this)
            .setTitle("Warm Reminder (Privacy Compliance Example)")
            .setMessage(spannable)
            .setPositiveButton(
                "Agree"
            ) { dialogInterface, i -> MapsInitializer.updatePrivacyAgree(this, true) }
            .setNegativeButton(
                "Disagree"
            ) { dialogInterface, i ->
                MapsInitializer.updatePrivacyAgree(
                    this,
                    false
                )
            }
            .show()
    }

    private fun initLocation() {
        try {
            locationClient = AMapLocationClient(this.applicationContext)
            //设置定位参数
            locationClient!!.setLocationOption(locationOption)
            // 设置定位监听
            locationClient!!.setLocationListener(locationListener)
        } catch (e: Exception) {
            e.printStackTrace()
            logs.add(e.message.toString())
        }
    }

    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private fun startLocation() {
        try {
            // 设置定位参数
            locationClient!!.setLocationOption(locationOption)
            // 启动定位
            locationClient!!.startLocation()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 停止定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private fun stopLocation() {
        try {
            // 停止定位
            locationClient!!.stopLocation()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Location listener
     */
    var locationListener =
        AMapLocationListener { location ->
            if (null != location) {
                val sb = StringBuffer()
                // errCode = 0 means location success, other values indicate location failure, please refer to the official website for location error code description
                if (location.errorCode == 0) {

                    // Update the UI with the location data
                    val locationInfo =
                        "LatLng-> ${location.latitude} - ${location.longitude}" + "\n" +
                                "Speed-> ${location.speed}" + "\n" +
                                "Altitude-> ${location.altitude}" + "\n" +
                                "Accuracy-> ${location.accuracy} - ${location.longitude}\n"

                    sb.append(locationInfo)
                    locationList.add(
                        LocationItem(
                            time = convertLongToTime(location.time),
                            locationInfo = locationInfo
                        )
                    )

                } else {
                    // Location failure
                    sb.append(
                        """
                             Location failed
                             
                             """.trimIndent()
                    )
                    sb.append(
                        """
                             Error code: ${location.errorCode}
                             
                             """.trimIndent()
                    )
                    Log.e("Error message:", "Error message: " + location.errorInfo)
                    Log.e("Error message:", "Error description: " + location.locationDetail)
                    sb.append(
                        """
                             Error message: ${location.errorInfo}
                             
                             """.trimIndent()
                    )
                    sb.append(
                        """
                             Error description: ${location.locationDetail}
                             
                             """.trimIndent()
                    )


                    // Parse the result
                    val result = sb.toString()

                    logs.add(result)

                }
                sb.append("***Location quality report***").append("\n")
                sb.append("* WIFI switch: ")
                    .append(if (location.locationQualityReport.isWifiAble) "On" else "Off")
                    .append("\n")
                sb.append("* GPS status: ")
                    .append(getGPSStatusString(location.locationQualityReport.gpsStatus))
                    .append("\n")
                sb.append("* GPS satellites: ").append(location.locationQualityReport.gpsSatellites)
                    .append("\n")
                sb.append("* Network type: " + location.locationQualityReport.networkType)
                    .append("\n")
                sb.append("* Network usage time: " + location.locationQualityReport.netUseTime)
                    .append("\n")
                sb.append("****************").append("\n")
                // Callback time after location
                sb.append(
                    """
                         Callback time: ${
                        formatUTC(
                            System.currentTimeMillis(),
                            "yyyy-MM-dd HH:mm:ss"
                        ).toString()
                    }
                         
                         """.trimIndent()
                )

                // Parse the location result
                val result = sb.toString()

                logs.add(result)

            } else {
                logs.add("Location failed, loc is null")
            }
        }


    /**
     * Get GPS status string
     *
     * @param statusCode GPS status code
     * @return GPS status string
     */
    private fun getGPSStatusString(statusCode: Int): String? {
        var str = ""
        when (statusCode) {
            AMapLocationQualityReport.GPS_STATUS_OK -> str = "GPS is working properly"
            AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER -> str =
                "No GPS provider in the phone, unable to locate with GPS"
            AMapLocationQualityReport.GPS_STATUS_OFF -> str =
                "GPS is turned off, it is recommended to turn on GPS to improve the positioning quality"
            AMapLocationQualityReport.GPS_STATUS_MODE_SAVING -> str =
                "The selected positioning mode does not include GPS positioning, it is recommended to choose a mode that includes GPS positioning to improve the positioning quality"
            AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION -> str =
                "No GPS positioning permission, it is recommended to enable GPS positioning permission"
        }
        return str
    }

    private fun startLocationUpdates() {


        /*val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(500)
            .setFastestInterval(500)

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )*/
    }

    /*private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            if(locationResult==null){
                logs.add("locationResult is empty")
            }
            locationResult ?: return
            for (location in locationResult.locations) {

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
        }

        override fun onLocationAvailability(p0: LocationAvailability?) {
            super.onLocationAvailability(p0)

            logs.add("onLocationAvailability->${p0}")

        }
    }

    private fun stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }*/

    override fun onResume() {
        super.onResume()
        //requestLocationPermission(this)
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
                startLocationUpdates()
                logs.add("Gps available")
            } else {
                // GPS is still not enabled, show an error message or exit the app
                logs.add("Gps not provided")
            }
        }
    }
}