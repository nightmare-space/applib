# applib
applib server, based on HTTP

view Android's apps/tasks/displays ... on anywhere when network accessibility

details see [DEVELOP.md](docs/DEVELOP_New.md)

## Structure

![](docs/applib.excalidraw.png)

## Start with Flutter
```yaml
  app_channel:
    git: https://github.com/nightmare-space/app_channel
```
and use Dart API to get some info like this

```dart
AppChannel channel =  AppChannel(port: 14000);
AppInfos infos = await channel.getAppInfosV2();
```

or you can impl with any language(kotlin/java/...) by this protocol [API.md](docs/API.md)

## who use this?
- [Speed Share](https://github.com/nightmare-space/speed_share): Select app to send file.
- [ADB KIT](https://github.com/nightmare-space/adb_kit): Select app to install to target device.
- Uncon(Closed source): Obtain apps/tasks/icons about LAN devices
