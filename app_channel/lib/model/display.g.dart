// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'display.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Display _$DisplayFromJson(Map<String, dynamic> json) => Display(
      name: json['name'] as String,
      width: (json['width'] as num).toInt(),
      height: (json['height'] as num).toInt(),
      rotation: (json['rotation'] as num).toInt(),
      dump: json['dump'] as String,
      id: (json['id'] as num).toInt(),
      refreshRate: (json['refreshRate'] as num?)?.toDouble(),
      metrics: json['metrics'] as String?,
    );

Map<String, dynamic> _$DisplayToJson(Display instance) => <String, dynamic>{
      'rotation': instance.rotation,
      'id': instance.id,
      'name': instance.name,
      'width': instance.width,
      'height': instance.height,
      'refreshRate': instance.refreshRate,
      'metrics': instance.metrics,
      'dump': instance.dump,
    };

Displays _$DisplaysFromJson(Map<String, dynamic> json) => Displays(
      datas: (json['datas'] as List<dynamic>)
          .map((e) => Display.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$DisplaysToJson(Displays instance) => <String, dynamic>{
      'datas': instance.datas,
    };
