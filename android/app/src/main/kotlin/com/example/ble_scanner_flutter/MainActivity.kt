package com.example.ble_scanner_flutter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

const val ACCESS_FINE_LOCATION_AND_BLUETOOTH_REQUEST_CODE = 1

class MainActivity : FlutterActivity() {

    companion object {
        lateinit var methodChannel: MethodChannel
    }

    private val CHANNEL = "ble_service"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        val serviceIntent = Intent(this, BLEService::class.java)
        methodChannel.setMethodCallHandler {
            // Note: this method is invoked on the main thread.
            call, result ->
            when (call.method) {
                "startBLEService" -> {
                    requestPermissionsAndStartService();
                    result.success("Started!")
                }

                "stopBLEService" -> {
                    stopService(serviceIntent)
                    result.success("Stopped!")
                }

                else -> {
                    result.notImplemented()
                }
            }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            ACCESS_FINE_LOCATION_AND_BLUETOOTH_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        val serviceIntent = Intent(context, BLEService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            activity.startForegroundService(serviceIntent)
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissionsAndStartService() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            val permissions = mutableSetOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }

            ActivityCompat.requestPermissions(
                    activity, permissions.toTypedArray(), ACCESS_FINE_LOCATION_AND_BLUETOOTH_REQUEST_CODE
            )
            return;
        } else {
            val serviceIntent = Intent(context, BLEService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.startForegroundService(serviceIntent)
            }
        }
    }
}