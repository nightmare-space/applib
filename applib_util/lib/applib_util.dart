
import 'dart:async';

import 'package:flutter/services.dart';

class ApplibUtil {
  static const MethodChannel _channel = MethodChannel('applib_util');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
