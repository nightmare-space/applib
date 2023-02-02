// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'api.dart';

// **************************************************************************
// RetrofitGenerator
// **************************************************************************

// ignore_for_file: unnecessary_brace_in_string_interps,no_leading_underscores_for_local_identifiers

class _Api implements Api {
  _Api(
    this._dio, {
    this.baseUrl,
  });

  final Dio _dio;

  String? baseUrl;

  @override
  Future<String> getAllAppInfo({
    options,
    is_system_app,
  }) async {
    const _extra = <String, dynamic>{};
    final queryParameters = <String, dynamic>{r'is_system_app': is_system_app};
    queryParameters.removeWhere((k, v) => v == null);
    final _headers = <String, dynamic>{};
    final _data = <String, dynamic>{};
    final newOptions = newRequestOptions(options);
    newOptions.extra.addAll(_extra);
    newOptions.headers.addAll(_dio.options.headers);
    newOptions.headers.addAll(_headers);
    final _result = await _dio.fetch<String>(newOptions.copyWith(
      method: 'GET',
      baseUrl: baseUrl ?? _dio.options.baseUrl,
      queryParameters: queryParameters,
      path: '/allappinfo',
    )..data = _data);
    final value = _result.data!;
    return value;
  }

  @override
  Future<String> getAppDetail({
    options,
    package,
  }) async {
    const _extra = <String, dynamic>{};
    final queryParameters = <String, dynamic>{r'package': package};
    queryParameters.removeWhere((k, v) => v == null);
    final _headers = <String, dynamic>{};
    final _data = <String, dynamic>{};
    final newOptions = newRequestOptions(options);
    newOptions.extra.addAll(_extra);
    newOptions.headers.addAll(_dio.options.headers);
    newOptions.headers.addAll(_headers);
    final _result = await _dio.fetch<String>(newOptions.copyWith(
      method: 'GET',
      baseUrl: baseUrl ?? _dio.options.baseUrl,
      queryParameters: queryParameters,
      path: '/appdetail',
    )..data = _data);
    final value = _result.data!;
    return value;
  }

  @override
  Future<String> getAppMainActivity({
    options,
    package,
  }) async {
    const _extra = <String, dynamic>{};
    final queryParameters = <String, dynamic>{r'package': package};
    queryParameters.removeWhere((k, v) => v == null);
    final _headers = <String, dynamic>{};
    final _data = <String, dynamic>{};
    final newOptions = newRequestOptions(options);
    newOptions.extra.addAll(_extra);
    newOptions.headers.addAll(_dio.options.headers);
    newOptions.headers.addAll(_headers);
    final _result = await _dio.fetch<String>(newOptions.copyWith(
      method: 'GET',
      baseUrl: baseUrl ?? _dio.options.baseUrl,
      queryParameters: queryParameters,
      path: '/appmainactivity',
    )..data = _data);
    final value = _result.data!;
    return value;
  }

  @override
  Future<String> getAppActivity({
    options,
    package,
  }) async {
    const _extra = <String, dynamic>{};
    final queryParameters = <String, dynamic>{r'package': package};
    queryParameters.removeWhere((k, v) => v == null);
    final _headers = <String, dynamic>{};
    final _data = <String, dynamic>{};
    final newOptions = newRequestOptions(options);
    newOptions.extra.addAll(_extra);
    newOptions.headers.addAll(_dio.options.headers);
    newOptions.headers.addAll(_headers);
    final _result = await _dio.fetch<String>(newOptions.copyWith(
      method: 'GET',
      baseUrl: baseUrl ?? _dio.options.baseUrl,
      queryParameters: queryParameters,
      path: '/appactivity',
    )..data = _data);
    final value = _result.data!;
    return value;
  }

  @override
  Future<String> getAppPermissions({
    options,
    package,
  }) async {
    const _extra = <String, dynamic>{};
    final queryParameters = <String, dynamic>{r'package': package};
    queryParameters.removeWhere((k, v) => v == null);
    final _headers = <String, dynamic>{};
    final _data = <String, dynamic>{};
    final newOptions = newRequestOptions(options);
    newOptions.extra.addAll(_extra);
    newOptions.headers.addAll(_dio.options.headers);
    newOptions.headers.addAll(_headers);
    final _result = await _dio.fetch<String>(newOptions.copyWith(
      method: 'GET',
      baseUrl: baseUrl ?? _dio.options.baseUrl,
      queryParameters: queryParameters,
      path: '/apppermission',
    )..data = _data);
    final value = _result.data!;
    return value;
  }

  @override
  Future<String> openAppByPackage({
    options,
    package,
    activity,
    displayId,
  }) async {
    const _extra = <String, dynamic>{};
    final queryParameters = <String, dynamic>{
      r'package': package,
      r'activity': activity,
      r'displayId': displayId,
    };
    queryParameters.removeWhere((k, v) => v == null);
    final _headers = <String, dynamic>{};
    final _data = <String, dynamic>{};
    final newOptions = newRequestOptions(options);
    newOptions.extra.addAll(_extra);
    newOptions.headers.addAll(_dio.options.headers);
    newOptions.headers.addAll(_headers);
    final _result = await _dio.fetch<String>(newOptions.copyWith(
      method: 'GET',
      baseUrl: baseUrl ?? _dio.options.baseUrl,
      queryParameters: queryParameters,
      path: '/openapp',
    )..data = _data);
    final value = _result.data!;
    return value;
  }

  @override
  Future<String> getAppInfos({
    options,
    required apps,
  }) async {
    const _extra = <String, dynamic>{};
    final queryParameters = <String, dynamic>{r'apps': apps};
    queryParameters.removeWhere((k, v) => v == null);
    final _headers = <String, dynamic>{};
    final _data = <String, dynamic>{};
    final newOptions = newRequestOptions(options);
    newOptions.extra.addAll(_extra);
    newOptions.headers.addAll(_dio.options.headers);
    newOptions.headers.addAll(_headers);
    final _result = await _dio.fetch<String>(newOptions.copyWith(
      method: 'GET',
      baseUrl: baseUrl ?? _dio.options.baseUrl,
      queryParameters: queryParameters,
      path: '/appinfos',
    )..data = _data);
    final value = _result.data!;
    return value;
  }

  @override
  Future<String> displays({options}) async {
    const _extra = <String, dynamic>{};
    final queryParameters = <String, dynamic>{};
    queryParameters.removeWhere((k, v) => v == null);
    final _headers = <String, dynamic>{};
    final _data = <String, dynamic>{};
    final newOptions = newRequestOptions(options);
    newOptions.extra.addAll(_extra);
    newOptions.headers.addAll(_dio.options.headers);
    newOptions.headers.addAll(_headers);
    final _result = await _dio.fetch<String>(newOptions.copyWith(
      method: 'GET',
      baseUrl: baseUrl ?? _dio.options.baseUrl,
      queryParameters: queryParameters,
      path: '/displays',
    )..data = _data);
    final value = _result.data!;
    return value;
  }

  RequestOptions newRequestOptions(Object? options) {
    if (options is RequestOptions) {
      return options as RequestOptions;
    }
    if (options is Options) {
      return RequestOptions(
        method: options.method,
        sendTimeout: options.sendTimeout,
        receiveTimeout: options.receiveTimeout,
        extra: options.extra,
        headers: options.headers,
        responseType: options.responseType,
        contentType: options.contentType.toString(),
        validateStatus: options.validateStatus,
        receiveDataWhenStatusError: options.receiveDataWhenStatusError,
        followRedirects: options.followRedirects,
        maxRedirects: options.maxRedirects,
        requestEncoder: options.requestEncoder,
        responseDecoder: options.responseDecoder,
        path: '',
      );
    }
    return RequestOptions(path: '');
  }

  RequestOptions _setStreamType<T>(RequestOptions requestOptions) {
    if (T != dynamic &&
        !(requestOptions.responseType == ResponseType.bytes ||
            requestOptions.responseType == ResponseType.stream)) {
      if (T == String) {
        requestOptions.responseType = ResponseType.plain;
      } else {
        requestOptions.responseType = ResponseType.json;
      }
    }
    return requestOptions;
  }
}
