// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ble_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BleDto _$BleDtoFromJson(Map<String, dynamic> json) => BleDto(
      macAddress: json['macAddress'] as String?,
      major: json['major'] as int?,
      manufacturer: json['manufacturer'] as String?,
      minor: json['minor'] as int?,
      rssi: json['rssi'] as int?,
      type: json['type'] as String?,
      uuid: json['uuid'] as String?,
      txPower: json['txPower'] as int?,
      dateTime: json['dateTime'] == null
          ? null
          : DateTime.parse(json['dateTime'] as String),
    );

Map<String, dynamic> _$BleDtoToJson(BleDto instance) {
  final val = <String, dynamic>{};

  void writeNotNull(String key, dynamic value) {
    if (value != null) {
      val[key] = value;
    }
  }

  writeNotNull('uuid', instance.uuid);
  writeNotNull('macAddress', instance.macAddress);
  writeNotNull('manufacturer', instance.manufacturer);
  writeNotNull('major', instance.major);
  writeNotNull('minor', instance.minor);
  writeNotNull('rssi', instance.rssi);
  writeNotNull('type', instance.type);
  writeNotNull('txPower', instance.txPower);
  writeNotNull('dateTime', instance.dateTime?.toIso8601String());
  return val;
}
