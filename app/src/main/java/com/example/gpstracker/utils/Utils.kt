package com.example.gpstracker.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

fun requestLocationPermission(context: Context) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            (context as ComponentActivity),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            Constant.PERMISSIONS_REQUEST_LOCATION
        )
    }
}

fun Context.toast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()


/**
 * 开始定位
 */
const val MSG_LOCATION_START = 0

/**
 * 定位完成
 */
const val MSG_LOCATION_FINISH = 1

/**
 * 停止定位
 */
const val MSG_LOCATION_STOP = 2

const val KEY_URL = "URL"
const val URL_H5LOCATION = "file:///android_asset/sdkLoc.html"

private var sdf: SimpleDateFormat? = null
fun formatUTC(l: Long, strPattern: String?): String {
    var strPattern = strPattern
    if (TextUtils.isEmpty(strPattern)) {
        strPattern = "yyyy-MM-dd HH:mm:ss"
    }
    if (sdf == null) {
        try {
            sdf = SimpleDateFormat(strPattern, Locale.CHINA)
        } catch (e: Throwable) {
        }
    } else {
        sdf!!.applyPattern(strPattern)
    }
    return if (sdf == null) "NULL" else sdf!!.format(l)
}
