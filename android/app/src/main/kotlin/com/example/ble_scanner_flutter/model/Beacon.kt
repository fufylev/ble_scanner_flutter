package com.example.ble_scanner_flutter.model

class BeaconEntity(mac: String?) {
    enum class beaconType {
        iBeacon, eddystoneUID, any
    }

    val macAddress = mac
    var manufacturer: String? = null
    var type: beaconType = beaconType.any
    var uuid: String? = null
    var major: Int? = null
    var minor: Int? = null
    var txPower: Int? = null;
    var namespace: String? = null
    var instance: String? = null
    var rssi: Int? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BeaconEntity) return false

        if (macAddress != other.macAddress) return false

        return true
    }

    override fun hashCode(): Int {
        return macAddress?.hashCode() ?: 0
    }

}