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
      persistentId: json['persistentId'] as int,
      displayId: json['displayId'] as int,
      topPackage: json['topPackage'] as String,
      topAcivity: json['topAcivity'] as String,
      label: json['label'] as String,
      id: json['id'] as int,
    );

Map<String, dynamic> _$TaskToJson(Task instance) => <String, dynamic>{
      'id': instance.id,
      'persistentId': instance.persistentId,
      'displayId': instance.displayId,
      'topPackage': instance.topPackage,
      'topAcivity': instance.topAcivity,
      'label': instance.label,
    };
