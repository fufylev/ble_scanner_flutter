package com.example.ble_scanner_flutter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.example.ble_scanner_flutter.model.BeaconEntity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import com.example.ble_scanner_flutter.R
import no.nordicsemi.android.support.v18.scanner.*
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region
import java.util.Timer
import java.util.TimerTask


class BLEService : Service(), MethodChannel.MethodCallHandler {
    private val TAG = BLEService::class.java.name
    val notificationId = 1
    var serviceRunning = false
    var delay = 500;
    private var currFlutterEngine: FlutterEngine? = null
    lateinit var builder: NotificationCompat.Builder
    lateinit var channel: NotificationChannel
    lateinit var manager: NotificationManager
    private var backgroundChannel: MethodChannel? = null


    private var scanning = false

    private fun scanLeDevice() {
        MainActivity.methodChannel.invokeMethod(TAG, "initScanner");
        if (!scanning) {
            val beaconManager = BeaconManager.getInstanceForApplication(this)
            val region = Region("all-beacons-region", null, null, null)

            beaconManager.getRegionViewModel(region).rangedBeacons.observeForever(rangingObserver)
            beaconManager.startRangingBeacons(region)
            scanning = true
        } else {
            scanning = false
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground()
        serviceRunning = true

    }

    override fun onDestroy() {
        super.onDestroy()
        serviceRunning = false
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        channel = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        return channelId
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        bleServiceStateHolder.startService()
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (serviceRunning) {
                    updateNotification("I got updated!")
                }
            }
        }, 5000)
        val extras = intent!!.extras
        delay = extras?.getInt("delay") ?: 500;
        MainActivity.methodChannel.invokeMethod("start_scanning", "start");
        return START_STICKY
    }

    private fun startForeground() {
        val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("ieye_ble_service", "Ieye BLEService")
                } else {
                    // If earlier version channel ID is not used
                    // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                    ""
                }
        initBackgroundChannel()
        builder = NotificationCompat.Builder(this, channelId)
        builder
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Ieye BLEService")
                .setContentText("Ieye BLEService is running")
                .setCategory(Notification.CATEGORY_SERVICE)
        val beaconManager = BeaconManager.getInstanceForApplication(this)
//        beaconManager.backgroundScanPeriod = 3000;
//        beaconManager.backgroundBetweenScanPeriod = 3000;


        // Set up a Live Data observer so this Activity can get monitoring callbacks
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        val parser = BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        parser.setHardwareAssistManufacturerCodes(arrayOf(0xffff).toIntArray())
        beaconManager.beaconParsers.add(
                parser)
        startForeground(1, builder.build())
        beaconManager.enableForegroundServiceScanning(builder.build(), 1)
        beaconManager.setEnableScheduledScanJobs(false)
        scanLeDevice()
    }

    private fun updateNotification(text: String) {
        builder
                .setContentText(text)
        manager.notify(notificationId, builder.build());
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun initBackgroundChannel() {
        currFlutterEngine = FlutterEngine(this)
        val messenger = currFlutterEngine?.dartExecutor?.binaryMessenger ?: return
        backgroundChannel = MethodChannel(messenger, "ieye_service/background")
        backgroundChannel?.setMethodCallHandler(this)
    }

    val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d(TAG, "Ranged: ${beacons.count()} beacons")
        for (beacon: Beacon in beacons) {
            Log.d(TAG, "$beacon about ${Gson().toJson(beacon)} meters away")
            val scannedBeacon = BeaconEntity(beacon.bluetoothAddress)
            scannedBeacon.manufacturer = beacon.bluetoothName
            scannedBeacon.rssi = beacon.rssi
            scannedBeacon.txPower = beacon.txPower
            scannedBeacon.type = BeaconEntity.beaconType.iBeacon
            scannedBeacon.uuid = beacon.id1.toString()
            scannedBeacon.major = beacon.id2.toInt()
            scannedBeacon.minor = beacon.id3.toInt()
            val json = Gson().toJson(scannedBeacon)
            MainActivity.methodChannel.invokeMethod("scanResults", json)
        }
    }

    val monitoringObserver = Observer<Int> { state ->
        if (state == MonitorNotifier.INSIDE) {
            Log.d(TAG, "Beacons detected")
        } else {
            Log.d(TAG, "No beacons detected")
        }
    }
}