import 'dart:async';
import 'dart:io';

import 'package:app_channel/api/api.dart';
import 'package:app_channel/foundation/app.dart';
import 'package:app_channel/interface/app_channel.dart';
import 'package:dio/dio.dart';
import 'package:global_repository/global_repository.dart';

class RemoteAppChannel implements AppChannel {
  RemoteAppChannel({int? port}) {
    if (Platform.isMacOS || Platform.isLinux || Platform.isWindows) {
      port = 0;
    }
    this.port = port;
    api = Api(Dio(), baseUrl: 'http://127.0.0.1:${port ?? getPort()}');
  }
  @override
  int? port;

  late Api api;

  int? getPort() {
    if (port != null) {
      Log.e('port -> $port');
      return port;
    }
    String data = File(
      RuntimeEnvir.filesPath! + '/server_port',
    ).readAsStringSync();
    port = int.tryParse(data);
    Log.w('成功获取 LocalAppChannel port -> $port');
    return port;
  }

  @override
  Future<List<AppInfo>> getAllAppInfo(bool isSystemApp) async {
    Stopwatch watch = Stopwatch();
    watch.start();
    final result = await api.getAllAppInfo(is_system_app: isSystemApp);
    final List<String> infos = (result).split('\n');
    Log.e('watch -> ${watch.elapsed}');
    final List<AppInfo> entitys = <AppInfo>[];

    /// 为了减少数据包大小，自定义了一个简单的协议，没用 json
    for (int i = 0; i < infos.length; i++) {
      List<String> infoList = infos[i].split('\r');
      // Log.d('infoList line$i $infoList');
      final AppInfo appInfo = AppInfo(
        infoList[0],
        appName: infoList[1],
        minSdk: infoList[2],
        targetSdk: infoList[3],
        versionCode: infoList[5],
        versionName: infoList[4],
        freeze: infoList[6] == 'false',
        hide: infoList[7] == 'true',
        uid: infoList[8],
        apkPath: infoList[9],
      );
      entitys.add(appInfo);
    }
    return entitys;
  }

  @override
  Future<String> getAppDetails(String package) async {
    String result = await api.getAppDetail(package: package);
    return result;
  }

  @override
  Future<String> getAppMainActivity(String packageName) async {
    String result = await api.getAppMainActivity(package: packageName);
    Log.e('getAppMainActivity $result');
    return result;
  }

  @override
  Future<List<String>> getAppActivitys(String package) async {
    String result = await api.getAppActivity(package: package);
    final List<String> infos = result.split('\n');
    infos.removeLast();
    return infos;
  }

  @override
  Future<List<String>> getAppPermission(String package) async {
    String result = await api.getAppPermissions(package: package);
    final List<String> infos = result.split('\r');
    infos.removeLast();
    return infos;
  }

  @override
  Future<void> openApp(String package, String activity, String id) async {
    await api.openAppByPackage(package: package, activity: activity, displayId: id);
  }

  @override
  Future<List<AppInfo>> getAppInfos(List<String> packages) async {
    String result = await api.getAppInfos(apps: packages);
    final List<String> infos = result.split('\n');
    final List<AppInfo> entitys = <AppInfo>[];
    for (int i = 0; i < infos.length; i++) {
      List<String> infoList = infos[i].split('\r');
      final AppInfo appInfo = AppInfo(
        infoList[0],
        appName: infoList[1],
        minSdk: infoList[2],
        targetSdk: infoList[3],
        versionCode: infoList[5],
        versionName: infoList[4],
        freeze: infoList[6] == 'false',
        hide: infoList[7] == 'true',
        uid: infoList[8],
        apkPath: infoList[9],
      );
      entitys.add(appInfo);
    }
    return entitys;
  }

  /// 获得DisplayID List
  Future<List<String>> getDisplays() async {
    String result = await api.displays();
    Log.i(result);
    return result.trim().split('\n');
  }

  @override
  Future<bool> clearAppData(String packageName) async {
    String result = await exec('pm clear $packageName');
    return result.isNotEmpty;
  }

  @override
  Future<bool> hideApp(String packageName) async {
    String result = await exec('pm hide $packageName');
    return result.isNotEmpty;
  }

  @override
  Future<bool> showApp(String packageName) async {
    String result = await exec('pm unhide $packageName');
    return result.isNotEmpty;
  }

  @override
  Future<bool> freezeApp(String packageName) async {
    Log.i('pm disable $packageName');
    String result = await exec(
      'pm disable-user --user 0 $packageName',
    );
    return result.isNotEmpty;
  }

  @override
  Future<bool> unFreezeApp(String packageName) async {
    String result = await exec('pm enable --user 0 $packageName');
    return result.isNotEmpty;
  }

  @override
  Future<bool> unInstallApp(String packageName) async {
    String result = await exec('pm uninstall  $packageName');
    return result.isNotEmpty;
  }

  @override
  Future<String> getFileSize(String path) async {
    return await exec('stat -c "%s" $path');
  }
}
