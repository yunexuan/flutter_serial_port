
import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class FlutterSerialPort {
  static const MethodChannel _channel = MethodChannel('flutter_serial_port');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> strToHexStr (String str) async {
    final String? hexStr = await _channel.invokeMethod("strToHexStr", {'str': str});
    return hexStr;
  }

  /// List all devices
  static Future<List<Device>> listDevices() async {
    List devices = await _channel.invokeMethod("getAllDevices");
    List devicesPath = await _channel.invokeMethod("getAllDevicesPath");

    List<Device> deviceList = [];
    devices.asMap().forEach((index, deviceName) {
      deviceList.add(Device(deviceName, devicesPath[index]));
    });
    return deviceList;
  }

  /// Create an [SerialPort] instance
  static Future createSerialPort(Device device, int baudrate, {int parity = 0, int dataBits = 8, int stopBit = 1}) async {
    return SerialPort(_channel.name, device, baudrate, parity, dataBits, stopBit);
  }
}


class SerialPort {
  late MethodChannel _channel;
  late EventChannel _eventChannel;
  late Stream<dynamic> _eventStream;
  late Device device;
  late int baudrate;
  late int parity;
  late int dataBits;
  late int stopBit;
  late bool _deviceConnected;

  SerialPort(String methodChannelName, this.device, this.baudrate, this.parity, this.dataBits, this.stopBit) {
    _channel = MethodChannel(methodChannelName);
    _eventChannel = EventChannel("$methodChannelName/event");
    _deviceConnected = false;
  }

  bool get isConnected => _deviceConnected;

  /// Stream(Event) coming from Android
  Stream get receiveStream {
    _eventStream = _eventChannel.receiveBroadcastStream();
    return _eventStream;
  }

  @override
  String toString() {
    return "SerialPort($device, $baudrate, $parity, $dataBits, $stopBit)";
  }

  /// Open device
  Future<bool> open() async {
    bool openResult = await _channel.invokeMethod(
        "open", {'devicePath': device.path, 'baudRate': baudrate, 'parity': parity, 'dataBits': dataBits, 'stopBit': stopBit});

    if (openResult) {
      _deviceConnected = true;
    }

    return openResult;
  }

  /// Close device
  Future<bool> close() async {
    bool closeResult = await _channel.invokeMethod("close");

    if (closeResult) {
      _deviceConnected = false;
    }

    return closeResult;
  }

  /// Write data to device
  Future<void> write(String data, String type) async {
    return await _channel.invokeMethod("write", {"data": data, "type": type});
  }
}

/// [Device] contains device information(name and path).
class Device {
  String name;
  String path;

  Device(this.name, this.path);

  @override
  String toString() {
    return "Device($name, $path)";
  }
}
