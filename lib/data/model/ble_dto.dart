import 'package:ble_scanner_flutter/domain/model/ble_entity.dart';
import 'package:equatable/equatable.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ble_dto.g.dart';

@JsonSerializable(includeIfNull: false)
class BleDto extends Equatable {
  /// уникальный UUID устройства
  final String? uuid;

  /// mac-адрес устройства
  final String? macAddress;

  /// компания-производитель устройства
  final String? manufacturer;

  /// 16-битное беззнаковое значение, с помощью которого можно группировать маяки с одинаковым UUID (из вики)
  final int? major;

  /// 16-битное беззнаковое значение, с помощью которого можно группировать маяки с одинаковым UUID и Major
  final int? minor;

  /// мощность сигнала, в паре с txPower позволяет высчитать расстояние до устройства
  final int? rssi;

  /// тип устройства
  final String? type;

  /// мощность беспроводного сигнала устройства. Чем больше - тем дальше может передавать.
  final int? txPower;

  /// Время регистрации чекина
  final DateTime? dateTime;

  const BleDto({
    this.macAddress,
    this.major,
    this.manufacturer,
    this.minor,
    this.rssi,
    this.type,
    this.uuid,
    this.txPower,
    this.dateTime,
  });

  // Пример
  // {"macAddress":"72:D1:CD:94:63:D4","major":16412,"minor":1968,"rssi":-55,"txPower":127,"type":"iBeacon","uuid":"081ef4aeca4e0e877ceaccb72f411005"}

  Map<String, dynamic> toJson() => _$BleDtoToJson(this);

  factory BleDto.fromJson(Map<String, dynamic> json) => _$BleDtoFromJson(json);

  BleEntity toEntity() {
    return BleEntity(
      macAddress: macAddress,
      major: major,
      minor: minor,
      manufacturer: manufacturer,
      rssi: rssi,
      type: type,
      uuid: uuid,
      txPower: txPower,
      dateTime: dateTime,
    );
  }

  @override
  String toString() {
    return 'BleDto('
        'macAddress: $macAddress, '
        'major: $major, '
        'manufacturer: $manufacturer, '
        'minor: $minor, '
        'rssi: $rssi, '
        'type: $type, '
        'uuid: $uuid, '
        'txPower: $txPower, '
        'dateTime: $dateTime, '
        ')';
  }

  @override
  List<Object?> get props => [macAddress, major, minor, uuid];
}
