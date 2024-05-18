// ignore_for_file: non_constant_identifier_names

import 'package:json_annotation/json_annotation.dart';
part 'tasks.g.dart';

Tasks deserializeTasks(Map<String, dynamic> json) => Tasks.fromJson(json);

@JsonSerializable()
class Tasks {
  Tasks({
    required this.datas,
  });

  final List<Task> datas;

  factory Tasks.fromJson(Map<String, dynamic> json) => _$TasksFromJson(json);
  Map<String, dynamic> toJson() => _$TasksToJson(this);
  @override
  String toString() {
    return toJson().toString();
  }
}

Task deserializeApp(Map<String, dynamic> json) => Task.fromJson(json);

@JsonSerializable()
class Task {
  Task({
    required this.persistentId,
    required this.topPackage,
    required this.topActivity,
    required this.label,
    required this.id,
    this.displayId,
  });

  final int id;
  final int persistentId;
  final int? displayId;
  final String topPackage;
  final String topActivity;
  final String label;

  factory Task.fromJson(Map<String, dynamic> json) => _$TaskFromJson(json);
  Map<String, dynamic> toJson() => _$TaskToJson(this);
  @override
  String toString() {
    return toJson().toString();
  }
}
