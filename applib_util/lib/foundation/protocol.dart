class Protocol {
  Protocol._();
  /// 获得某个 package 的图标信息
  static const String getIconData = "icon";
  /// 获得所有的 app 基本信息
  static const String getAllAppInfo = "allappinfo";
  /// 获得给定的 app 的基本信息
  static const String getAppInfos = "appinfos";
  static const String getAppActivity = "appactivity";
  static const String getAppPermissions = "apppermission";
  static const String getAppDetail = "appdetail";
  static const String getAppMainActivity = "appmainactivity";
  static const String openAppByPackage = "openapp";
  static const String checkToken = "check";
}
