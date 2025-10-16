### Compile Native Image

On Windows:

```shell
.\gradlew clean quarkusBuild
```

On Linux/Mac:

```shell
./gradlew clean quarkusBuild
```

### Run Examples

On Windows:

```shell
.\build\quarkus-awt-native-demo-1.0.0-runner.exe SHOW_SYSTEM_TRAY_ICON
.\build\quarkus-awt-native-demo-1.0.0-runner.exe CREATE_QR_IMAGE
.\build\quarkus-awt-native-demo-1.0.0-runner.exe CREATE_PDF_FILE
.\build\quarkus-awt-native-demo-1.0.0-runner.exe CREATE_EXCEL_FILE
```

On Linux/Mac:

```shell
./build/quarkus-awt-native-demo-1.0.0-runner SHOW_SYSTEM_TRAY_ICON
./build/quarkus-awt-native-demo-1.0.0-runner CREATE_QR_IMAGE
./build/quarkus-awt-native-demo-1.0.0-runner CREATE_PDF_FILE
./build/quarkus-awt-native-demo-1.0.0-runner CREATE_EXCEL_FILE
```