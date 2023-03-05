import 'package:app_channel/foundation/app.dart';
import 'package:app_channel/model/tasks.dart';

/// 抽象的一层 channel
/// 其他地方依赖抽象
/// 用 GetX 将这个的实现注入到依赖
abstract class AppChannel {
  int? port;

  Future<List<AppInfo>> getAllAppInfo(bool isSystemApp);

  Future<List<AppInfo>> getAppInfos(List<String> packages);

  Future<String> getAppDetails(String package);

  Future<List<String>> getAppActivitys(String package);

  Future<List<String>> getAppPermission(String package);

  Future<String> getAppMainActivity(String packageName);

  Future<bool> clearAppData(String packageName);

  Future<bool> hideApp(String packageName);

  Future<bool> showApp(String packageName);

  Future<bool> freezeApp(String packageName);

  Future<bool> unFreezeApp(String packageName);

  Future<bool> unInstallApp(String packageName);

  /// 获得文件的大小
  Future<String> getFileSize(String path);

  Future<void> openApp(String packageName, String activity, String id);

  Future<List<String>> getDisplays();

  Future<Tasks> getTasks();

  /// 创建虚拟显示器
  void createVirtualDisplay(int width, int height);
}
