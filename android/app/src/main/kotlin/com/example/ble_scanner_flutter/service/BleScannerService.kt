package com.example.ble_scanner_flutter.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.ble_scanner_flutter.service.stateholder.BleServiceStateHolder
import no.nordicsemi.android.support.v18.scanner.*
import org.koin.core.parameter.parametersOf
import com.example.ble_scanner_flutter.utils.toHexString
import org.koin.android.ext.android.inject
import java.util.Arrays

private val TAG = BleScannerService::class.java.name
private const val NOTIFICATION_ID = 123

class BleScannerService : Service() {
    private var _devicesState: MutableList<Pair<BluetoothDevice?, Int>> = mutableListOf()

    private var bleServiceStateHolder: BleServiceStateHolder = BleServiceStateHolder()

    private var scanning = false

    private var notificationManager: NotificationManager? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val eddystoneServiceId: ParcelUuid = ParcelUuid.fromString("0000feaa-0000-1000-8000-00805f9b34fb")

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val scanRecord = result.scanRecord
            val beacon = Beacon(result.device.address)
//            beacon.manufacturer = result.device.name
            beacon.rssi = result.rssi

            if (scanRecord != null) {
                val serviceUuids = scanRecord.serviceUuids
                if (serviceUuids != null) {
                    Log.d("BleService", serviceUuids[0]?.uuid.toString())
                }
                if (serviceUuids != null && serviceUuids.size > 0 && serviceUuids.contains(
                        eddystoneServiceId
                    )
                ) {
                    val serviceData = scanRecord.getServiceData(eddystoneServiceId)
                    if (serviceData != null && serviceData.size > 18) {
                        val eddystoneUUID =
                            toHexString(Arrays.copyOfRange(serviceData, 2, 18))
                        val namespace = String(eddystoneUUID.toCharArray().sliceArray(0..19))
                        val instance = String(
                            eddystoneUUID.toCharArray()
                                .sliceArray(20 until eddystoneUUID.toCharArray().size)
                        )
                        beacon.type = Beacon.beaconType.eddystoneUID
                        beacon.namespace = namespace
                        beacon.instance = instance

                        Log.e("BleService", "Namespace:$namespace Instance:$instance")
                    }
                }

            }

            if (scanRecord != null) {
                val iBeaconManufactureData = scanRecord.getManufacturerSpecificData(0X004c)
                if (iBeaconManufactureData != null && iBeaconManufactureData.size >= 23) {
                    Log.d("BleService", "device name: ${result.device.name}")
                    val iBeaconUUID = toHexString(iBeaconManufactureData.copyOfRange(2, 18))
                    val major = Integer.parseInt(toHexString(iBeaconManufactureData.copyOfRange(18, 20)), 16)
                    val minor = Integer.parseInt(toHexString(iBeaconManufactureData.copyOfRange(20, 22)), 16)
                    beacon.type = Beacon.beaconType.iBeacon
                    beacon.uuid = iBeaconUUID
                    beacon.major = major
                    beacon.minor = minor
                    Log.e("BleService", "iBeaconUUID:$iBeaconUUID major:$major minor:$minor")
                }
            }

            onScanResult(
                device = result.device,
                rssi = result.rssi
            )
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            for (result in results) {
                onScanResult(
                    device = result.device,
                    rssi = result.rssi
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun onScanResult(device: BluetoothDevice, rssi: Int) {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }

        if (!_devicesState.any { (it.first?.address ?: device.address) == device.address }) {
            _devicesState = _devicesState.toMutableList().apply {
                add(device to rssi)
            }
            bleServiceStateHolder.updateDeviceList(_devicesState)
            Log.d(TAG, "Added ${device.name}, ${device.address}")
        }
    }

//    init {
//        if (!scanning)
//            scanLeDevice()
//    }

    private fun scanLeDevice() {
        Log.d(TAG, "Init scanner")
        val scanner = BluetoothLeScannerCompat.getScanner()
        if (!scanning) {
            val settings: ScanSettings = ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setUseHardwareBatchingIfSupported(true)
                .build()
            scanning = true
            val filters: MutableList<ScanFilter> = ArrayList()
            scanner.startScan(filters, settings, scanCallback)
        } else {
            scanning = false
            scanner.stopScan(scanCallback)
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bleServiceStateHolder.startService()
        generateForegroundNotification()
        if (!scanning)
            scanLeDevice()
        return START_STICKY
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    override fun onDestroy() {
        stopForegroundNotification()
        stopSelf()
        bleServiceStateHolder.onServiceDestroyed()
        super.onDestroy()
    }

    private fun generateForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager == null) {
                notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager?.createNotificationChannelGroup(
                    NotificationChannelGroup("service_group", "Service group")
                )
                val notificationChannel = NotificationChannel("service_channel", "Service notifications", NotificationManager.IMPORTANCE_MIN)
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                notificationManager?.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, "service_channel")

            val serviceIsRunningHint = "Service" //applicationContext
//                .getString(R.string.service_title, applicationContext.getString(appNameId))
            builder.setContentTitle(serviceIsRunningHint)
                .setTicker(serviceIsRunningHint)
                .setContentText("Service")
//                .setSmallIcon(R.mipmap.ic_launcher_round)
//                .setContentText(applicationContext.getString(R.string.service_description))
//                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setOngoing(true)

//            builder.color = ContextCompat.getColor(applicationContext, R.color.purple_200)

            val notification = builder.build()

            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}