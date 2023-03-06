package com.example.gpstracker.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gpstracker.MainActivity
import com.example.gpstracker.ui.theme.ALPHA60_BLACK
import com.example.gpstracker.ui.theme.Orange
import com.example.gpstracker.utils.Constant.PERMISSIONS_REQUEST_LOCATION
import com.example.gpstracker.utils.LocationItem

@Composable
fun LocationTrackingScreen(
    status: MutableState<String>,
    locationList: SnapshotStateList<LocationItem>,
    startObservingLocation: () -> Unit,
    stopObservingLocation: () -> Unit,
    onRequestGps: () -> Unit,
    logs: SnapshotStateList<String>
) {
    var isLocationObserverStarted by remember {
        mutableStateOf(false)
    }
    val lazyListState = rememberLazyListState()
    val lazyListStateForLogs = rememberLazyListState()
    // Scroll to the latest item when a new item is added
    LaunchedEffect(locationList.size) {
        if (locationList.size > 0) {
            lazyListState.animateScrollToItem(locationList.size - 1)
        }
    }
    LaunchedEffect(logs.size) {
        if (logs.size > 0) {
            lazyListStateForLogs.animateScrollToItem(logs.size - 1)
        }
    }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize(), contentAlignment = Alignment.BottomEnd
    )
    {
        Column(modifier = Modifier.padding(16.dp)) {

            if (status.value.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(bottom = 4.dp),
                    text = status.value,
                    color = Orange
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize(), state = lazyListState) {

                itemsIndexed(locationList) { index, item ->

                    Text(
                        modifier = Modifier.padding(bottom = 4.dp),
                        text = "#$index-Time: " + item.time,
                        color = Orange
                    )

                    Text(text = item.locationInfo)

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .height(1.dp)
                            .background(Color.Black)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight()
                .background(ALPHA60_BLACK).padding(bottom = 60.dp).padding(4.dp),
            state = lazyListStateForLogs,
            horizontalAlignment = Alignment.End
        ) {

            itemsIndexed(logs) { _, item ->

                Text(
                    modifier = Modifier.padding(bottom = 4.dp),
                    text = item,
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .height(1.dp)
                        .background(Color.Red)
                )
            }
        }

        Button(modifier = Modifier.padding(16.dp), onClick = {

            Log.d("locationCollector", "")
            // Check if the app has permission to access location
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                logs.add("Location permission not granted")
                // Request permission from the user
                ActivityCompat.requestPermissions(
                    (context as ComponentActivity),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_LOCATION
                )
            } else {
                Log.d("locationCollector", "locationCollector started")
                // Permission has already been granted, start location updates
                if (isLocationObserverStarted) {
                    logs.add("Stop observing location")
                    stopObservingLocation()
                    isLocationObserverStarted = false
                } else {
                    logs.add("-->Start observing location")
                    onRequestGps()
                    startObservingLocation()
                    isLocationObserverStarted = true
                }
            }

        }) {
            Text(text = if (isLocationObserverStarted) "Stop" else "Start")
        }
    }
}