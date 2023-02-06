import 'dart:async';

import 'package:flutter/services.dart';
export 'interface/app_channel.dart';
export 'implement/local_app_channel.dart';
export 'foundation/app.dart';
export 'foundation/app_details.dart';

class ApplibUtil {
  static const MethodChannel _channel = MethodChannel('apputils');

  static Future<int> get port async {
    final int port = await _channel.invokeMethod('getPort');
    return port;
  }
}
