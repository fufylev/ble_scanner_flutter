import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:ble_scanner_flutter/data/model/ble_dto.dart';
import 'package:ble_scanner_flutter/domain/model/ble_entity.dart';
import 'package:flutter/services.dart';

class ScannerService {
  static const platform = MethodChannel('ble_service');
  List<BleEntity> beaconDevices = [];
  StreamController<BleEntity> scannerDevicesController = StreamController<BleEntity>.broadcast();
  StreamController<String> serviceStateController = StreamController<String>.broadcast();

  Stream<BleEntity> getStream() {
    return scannerDevicesController.stream;
  }

  Future<void> startService() async {
    if (Platform.isAndroid) {
      try {
        await platform.invokeMethod('startBLEService');
        _enableListener();
        serviceStateController.add('Started!');
      } on PlatformException catch (e) {
        print("Failed to invoke method: '${e.message}'.");
      }
    }
  }

  Future<void> stopService() async {
    try {
      await platform.invokeMethod('stopBLEService');
      serviceStateController.add('Stopped!');
    } on PlatformException catch (e) {
      print("Failed to invoke method: '${e.message}'.");
    }
  }

  void _enableListener() {
    // Set method call handler before telling platform side we are ready to receive.
    platform.setMethodCallHandler((call) async {
      print('Just received ${call.method} from platform with ${call.arguments}');

      switch (call.method) {
        case 'scanResults':
          {
            BleDto dto = BleDto.fromJson(json.decode(call.arguments));
            BleEntity beaconDevice = dto.toEntity();
            beaconDevices.add(beaconDevice);
            scannerDevicesController.add(beaconDevice);
          }
      }
    });
  }
}
