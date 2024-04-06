import 'package:equatable/equatable.dart';

class BleEntity extends Equatable {
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

  const BleEntity({
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

  BleEntity copyWith({
    String? uuid,
    String? macAddress,
    String? manufacturer,
    int? major,
    int? minor,
    int? rssi,
    String? type,
    int? txPower,
    DateTime? dateTime,
  }) {
    return BleEntity(
      uuid: uuid ?? this.uuid,
      macAddress: macAddress ?? this.macAddress,
      manufacturer: manufacturer ?? this.manufacturer,
      major: major ?? this.major,
      minor: minor ?? this.minor,
      rssi: rssi ?? this.rssi,
      type: type ?? this.type,
      txPower: txPower ?? this.txPower,
      dateTime: dateTime ?? this.dateTime,
    );
  }

  // Пример
  // {"macAddress":"72:D1:CD:94:63:D4","major":16412,"minor":1968,"rssi":-55,"txPower":127,"type":"iBeacon","uuid":"081ef4aeca4e0e877ceaccb72f411005"}

  bool isEqual(BleEntity bleEntity) {
    return bleEntity.uuid == uuid;
  }

  @override
  String toString() {
    return 'BleEntity('
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
