import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:honeywell_scan/honeywell_scan.dart';

void main() {
  const MethodChannel channel = MethodChannel('honeywell_scan');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await HoneywellScan.platformVersion, '42');
  });
}
