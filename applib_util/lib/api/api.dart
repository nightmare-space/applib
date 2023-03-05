// ignore_for_file: non_constant_identifier_names

import 'package:app_channel/foundation/protocol.dart';
import 'package:app_channel/model/tasks.dart';
import 'package:dio/dio.dart' hide Headers;
import 'package:flutter/foundation.dart';
import 'package:retrofit/retrofit.dart';
part 'api.g.dart';

@RestApi(baseUrl: "", parser: Parser.FlutterCompute)
abstract class Api {
  factory Api(Dio dio, {String baseUrl}) = _Api;

  /// 获取应用列表
  @GET('/${Protocol.getAllAppInfo}')
  Future<String> getAllAppInfo({
    @DioOptions() RequestOptions? options,
    @Query("is_system_app") bool? is_system_app,
  });

  /// 获取应用详情
  @GET('/${Protocol.getAppDetail}')
  Future<String> getAppDetail({
    @DioOptions() RequestOptions? options,
    @Query("package") String? package,
  });

  /// 通过包名获取 MainActivity
  @GET('/${Protocol.getAppMainActivity}')
  Future<String> getAppMainActivity({
    @DioOptions() RequestOptions? options,
    @Query("package") String? package,
  });

  /// 通过包名获取 所有的 Activitys
  @GET('/${Protocol.getAppActivity}')
  Future<String> getAppActivity({
    @DioOptions() RequestOptions? options,
    @Query("package") String? package,
  });

  /// 通过包名获取所有的 Permission
  @GET('/${Protocol.getAppPermissions}')
  Future<String> getAppPermissions({
    @DioOptions() RequestOptions? options,
    @Query("package") String? package,
  });

  /// 启动App
  @GET('/${Protocol.openAppByPackage}')
  Future<String> openAppByPackage({
    @DioOptions() RequestOptions? options,
    @Query("package") String? package,
    @Query("activity") String? activity,
    @Query("displayId") String? displayId,
  });

  /// 启动App
  @GET('/${Protocol.getAppInfos}')
  Future<String> getAppInfos({
    @DioOptions() RequestOptions? options,
    @Query("apps") required List<String> apps,
  });

  /// 获得显示器列表
  @GET('/displays')
  Future<String> displays({
    @DioOptions() RequestOptions? options,
  });

  /// 获得显示器列表
  @GET('/tasks')
  Future<Tasks> getTasks({
    @DioOptions() RequestOptions? options,
  });

  /// 获得显示器列表
  @POST('/createVirtualDisplay')
  Future<String> createVirtualDisplay({
    @DioOptions() RequestOptions? options,
    @Query("width") required String width,
    @Query("height") required String height,
  });
}

Api restClient = Api(Dio());
