import 'dart:async';

import 'package:flutter/services.dart';

class HoneywellScan {
  static MethodChannel _methodChannel = const MethodChannel('honeywell_scan/method_channel');
  static EventChannel _eventChannel = const EventChannel('honeywell_scan/event_channel');
  static Stream<String> _onScanCompleted;

  static Future<String> get platformVersion async {
    final String version = await _methodChannel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> startScanning() async {
    await _methodChannel.invokeMethod('startScanning');
  }

  static Future<void> stopScanning() async {
    await _methodChannel.invokeMethod('stopScanning');
  }

  static Stream<String> get onScanCompleted {
    if (_onScanCompleted == null) {
      _onScanCompleted = _eventChannel.receiveBroadcastStream().map((dynamic event) => event);
    }
    return _onScanCompleted;
  }
}
