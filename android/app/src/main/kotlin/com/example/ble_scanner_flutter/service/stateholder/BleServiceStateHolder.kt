package com.example.ble_scanner_flutter.service.stateholder

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.MutableStateFlow

class BleServiceStateHolder() {
    private val serviceState = MutableStateFlow(false)

    var devicesState: MutableStateFlow<List<Pair<BluetoothDevice?, Int>>> = MutableStateFlow(
        mutableListOf()
    )

    var startedTime: MutableStateFlow<Long?> = MutableStateFlow(
        null
    )
    fun startService() {
        serviceState.value = true
        startedTime.value = System.currentTimeMillis()
    }

    fun updateDeviceList(devices: List<Pair<BluetoothDevice?, Int>>) {
        devicesState.value = devices
    }

    fun stopService() {
        serviceState.value = false
    }

    fun onServiceDestroyed() {
        serviceState.value = false
    }
}