import 'ble_beacon_service/service/scanner_service.dart';

void main() {
  final ScannerService service = ScannerService();
  service.startService();
}
