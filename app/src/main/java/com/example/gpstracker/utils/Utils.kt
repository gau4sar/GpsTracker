package com.example.gpstracker.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amap.api.location.AMapLocation
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

fun sHA1(context: Context): String? {
    return try {
        val info = context.packageManager.getPackageInfo(
            context.packageName, PackageManager.GET_SIGNATURES)
        val cert = info.signatures[0].toByteArray()
        val md = MessageDigest.getInstance("SHA1")
        val publicKey = md.digest(cert)
        val hexString = StringBuilder()
        for (i in publicKey.indices) {
            val appendString = Integer.toHexString(0xFF and publicKey[i].toInt())
                .toUpperCase(Locale.US)
            if (appendString.length == 1)
                hexString.append("0")
            hexString.append(appendString)
            hexString.append(":")
        }
        val result = hexString.toString()
        result.substring(0, result.length - 1)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        null
    }
}

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

/**
 * 根据定位结果返回定位信息的字符串
 * @param location
 * @return
 */
@Synchronized
fun getLocationStr(location: AMapLocation?): String? {
    if (null == location) {
        return null
    }
    val sb = StringBuffer()
    //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
    if (location.errorCode == 0) {
        sb.append(
            """
                定位成功
                
                """.trimIndent()
        )
        sb.append(
            """
                定位类型: ${location.locationType}
                
                """.trimIndent()
        )
        sb.append(
            """
                经    度    : ${location.longitude}
                
                """.trimIndent()
        )
        sb.append(
            """
                纬    度    : ${location.latitude}
                
                """.trimIndent()
        )
        sb.append(
            """
                精    度    : ${location.accuracy}米
                
                """.trimIndent()
        )
        sb.append(
            """
                提供者    : ${location.provider}
                
                """.trimIndent()
        )
        sb.append(
            """
                速    度    : ${location.speed}米/秒
                
                """.trimIndent()
        )
        sb.append(
            """
                角    度    : ${location.bearing}
                
                """.trimIndent()
        )
        // 获取当前提供定位服务的卫星个数
        sb.append(
            """
                星    数    : ${location.satellites}
                
                """.trimIndent()
        )
        sb.append(
            """
                国    家    : ${location.country}
                
                """.trimIndent()
        )
        sb.append(
            """
                省            : ${location.province}
                
                """.trimIndent()
        )
        sb.append(
            """
                市            : ${location.city}
                
                """.trimIndent()
        )
        sb.append(
            """
                城市编码 : ${location.cityCode}
                
                """.trimIndent()
        )
        sb.append(
            """
                区            : ${location.district}
                
                """.trimIndent()
        )
        sb.append(
            """
                区域 码   : ${location.adCode}
                
                """.trimIndent()
        )
        sb.append(
            """
                地    址    : ${location.address}
                
                """.trimIndent()
        )
        sb.append(
            """
                兴趣点    : ${location.poiName}
                
                """.trimIndent()
        )
        //定位完成的时间
        sb.append(
            """
                定位时间: ${formatUTC(location.time, "yyyy-MM-dd HH:mm:ss")}
                
                """.trimIndent()
        )
    } else {
        //定位失败
        sb.append(
            """
            定位失败
            
            """.trimIndent()
        )
        sb.append(
            """
                错误码:${location.errorCode}
                
                """.trimIndent()
        )
        sb.append(
            """
                错误信息:${location.errorInfo}
                
                """.trimIndent()
        )
        sb.append(
            """
                错误描述:${location.locationDetail}
                
                """.trimIndent()
        )
    }
    //定位之后的回调时间
    sb.append(
        """
            回调时间: ${formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")}
            
            """.trimIndent()
    )
    return sb.toString()
}

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

/**
 * 获取app的名称
 * @param context
 * @return
 */
fun getAppName(context: Context): String? {
    var appName = ""
    try {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(
            context.packageName, 0
        )
        val labelRes = packageInfo.applicationInfo.labelRes
        appName = context.resources.getString(labelRes)
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    return appName
}