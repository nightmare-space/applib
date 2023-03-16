// ignore_for_file: non_constant_identifier_names
import 'package:dio/dio.dart' hide Headers;
import 'package:flutter/foundation.dart';
import 'package:retrofit/retrofit.dart';
part 'behavior.g.dart';

@RestApi(baseUrl: "https://nightmare.fun:444/api/v1", parser: Parser.FlutterCompute)
abstract class BehaviorAPI {
  factory BehaviorAPI(Dio dio, {String baseUrl}) = _BehaviorAPI;

  ///
  @POST('/user_behavior/')
  Future<String> appInit({
    @DioOptions() RequestOptions? options,
    @Body() required Map<String, dynamic> params,
  });
}

BehaviorAPI behaviorAPI = BehaviorAPI(Dio());
