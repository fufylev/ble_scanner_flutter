package com.example.ble_scanner_flutter.utils

private val HEX = "0123456789abcdef".toCharArray()
fun toHexString(bytes: ByteArray): String {
    if (bytes.isEmpty()) {
        return ""
    }
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v = (bytes[j].toInt() and 0xFF)
        hexChars[j * 2] = HEX[v ushr 4]
        hexChars[j * 2 + 1] = HEX[v and 0x0F]
    }
    return String(hexChars)
}