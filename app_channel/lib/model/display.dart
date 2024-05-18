import 'package:json_annotation/json_annotation.dart';

part 'display.g.dart';

Display deserializeDisplay(Map<String, dynamic> json) => Display.fromJson(json);
Map<String, dynamic> serializeDisplay(Display object) => object.toJson();

@JsonSerializable()
class Display {
  Display({
    required this.name,
    required this.width,
    required this.height,
    required this.rotation,
    required this.dump,
    required this.id,
    this.refreshRate,
    this.metrics,
  });

  int rotation;
  int id;
  String name;
  int width;
  int height;
  double? refreshRate;
  String? metrics;
  String dump;

  factory Display.fromJson(Map<String, dynamic> json) => _$DisplayFromJson(json);
  Map<String, dynamic> toJson() => _$DisplayToJson(this);
}

Displays deserializeDisplays(Map<String, dynamic> json) => Displays.fromJson(json);

@JsonSerializable()
class Displays {
  Displays({
    required this.datas,
  });

  List<Display> datas;

  factory Displays.fromJson(Map<String, dynamic> json) => _$DisplaysFromJson(json);
  Map<String, dynamic> toJson() => _$DisplaysToJson(this);
}
