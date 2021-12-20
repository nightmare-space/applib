import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:applib_util/applib_util.dart';

void main() {
  const MethodChannel channel = MethodChannel('applib_util');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await ApplibUtil.platformVersion, '42');
  });
}
