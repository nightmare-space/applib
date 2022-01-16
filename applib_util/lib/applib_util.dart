
import 'dart:async';

import 'package:flutter/services.dart';

class ApplibUtil {
  static const MethodChannel _channel = MethodChannel('apputils');

  static Future<int> get port async {
    final int port = await _channel.invokeMethod('getPort');
    return port;
  }
}
