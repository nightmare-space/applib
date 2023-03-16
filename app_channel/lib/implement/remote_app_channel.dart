// import 'package:adbutil/adbutil.dart';
// import 'package:app_manager/global/global.dart';
// import 'package:app_manager/model/app.dart';
// import 'package:global_repository/global_repository.dart';

// import 'local_app_channel.dart';

// class RemoteAppChannel extends LocalAppChannel {
  // String? serial;
  // @override
  // Future<void> openApp(String packageName, String activity, String id) async {
  //   Log.e('openApp $packageName');
  //   execCmd('adb -s $serial shell am start -n $packageName/$activity');
  // }

  // @override
  // Future<List<AppInfo>> getAllAppInfo(bool isSystemApp) async {
  //   // Log.w('连接成功');
  //   Stopwatch watch = Stopwatch();
  //   watch.start();
  //   String result = await execCmd(
  //     'adb -s $serial shell CLASSPATH=/sdcard/app_server app_process /data/local/tmp/ com.nightmare.applib.AppChannel open',
  //     throwException: false,
  //   );
  //   final List<String> infos = (result).split('\n');
  //   Log.e('watch -> ${watch.elapsed}');
  //   final List<AppInfo> entitys = <AppInfo>[];
  //   for (int i = 0; i < infos.length; i++) {
  //     List<String> infoList = infos[i].split('\r');
  //     final AppInfo appInfo = AppInfo(
  //       infoList[0],
  //       appName: infoList[1],
  //       minSdk: infoList[2],
  //       targetSdk: infoList[3],
  //       versionCode: infoList[5],
  //       versionName: infoList[4],
  //       freeze: infoList[6] == 'false',
  //       hide: infoList[7] == 'true',
  //       uid: infoList[8],
  //       apkPath: infoList[9],
  //     );
  //     entitys.add(appInfo);
  //   }
  //   return entitys;
  // }
// }
