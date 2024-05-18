// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tasks.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Tasks _$TasksFromJson(Map<String, dynamic> json) => Tasks(
      datas: (json['datas'] as List<dynamic>)
          .map((e) => Task.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$TasksToJson(Tasks instance) => <String, dynamic>{
      'datas': instance.datas,
    };

Task _$TaskFromJson(Map<String, dynamic> json) => Task(
      persistentId: (json['persistentId'] as num).toInt(),
      topPackage: json['topPackage'] as String,
      topActivity: json['topActivity'] as String,
      label: json['label'] as String,
      id: (json['id'] as num).toInt(),
      displayId: (json['displayId'] as num?)?.toInt(),
    );

Map<String, dynamic> _$TaskToJson(Task instance) => <String, dynamic>{
      'id': instance.id,
      'persistentId': instance.persistentId,
      'displayId': instance.displayId,
      'topPackage': instance.topPackage,
      'topActivity': instance.topActivity,
      'label': instance.label,
    };
