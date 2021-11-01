import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_serial_port/flutter_serial_port.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:stream_transform/stream_transform.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  bool isPortOpened = false;
  late SerialPort _serialPort;
  late StreamSubscription _subscription;
  bool isHexMode = false;
  String readData = '';

  List<String> pathList = <String>[];
  String sPortPath = '';
  List<int> baudrateList = <int>[
    0,
    50,
    75,
    110,
    134,
    150,
    200,
    300,
    600,
    1200,
    1800,
    2400,
    4800,
    9600,
    19200,
    38400,
    57600,
    115200,
    230400,
    460800,
    500000,
    576000,
    921600,
    1000000,
    1152000,
    1500000,
    2000000,
    2500000,
    3000000,
    3500000,
    4000000
  ];
  int baudrate = 115200;
  List<int> parityList = <int>[0, 1, 2];
  int parity = 0;
  List<int> dataBitsList = <int>[5, 6, 7, 8];
  int dataBits = 8;
  List<int> stopBitList = <int>[1, 2];
  int stopBit = 1;
  String writeData = '';

  _initPortList() async {
    List<Device> deviceList = await FlutterSerialPort.listDevices();
    if (deviceList.isNotEmpty) {
      sPortPath = deviceList[0].path;
      for (Device device in deviceList) {
        pathList.add(device.path);
      }
    }
  }

  @override
  void initState() {
    super.initState();
    _initPortList();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Container(
//          Text('Running on: $_platformVersion\n')
          child: Column(
            children: [
              Container(
                color: Colors.black,
                width: double.infinity,
                height: 300,
                child: Text(readData, style: const TextStyle(color: Colors.white)),
              ),
              Row(
                children: [
                  DropdownButton<String>(
                    value: sPortPath,
                    style: const TextStyle(color: Colors.deepPurple),
                    underline: Container(
                      height: 2,
                      color: Colors.deepPurpleAccent,
                    ),
                    onChanged: (newValue) {
                      setState(() {
                        sPortPath = newValue!;
                      });
                    },
                    items:
                    pathList.map<DropdownMenuItem<String>>((String value) {
                      return DropdownMenuItem<String>(
                        value: value,
                        child: Text(value),
                      );
                    }).toList(),
                  ),
                  DropdownButton<int>(
                    value: baudrate,
                    style: const TextStyle(color: Colors.deepPurple),
                    underline: Container(
                      height: 2,
                      color: Colors.deepPurpleAccent,
                    ),
                    onChanged: (newValue) {
                      setState(() {
                        baudrate = newValue!;
                      });
                    },
                    items: baudrateList.map<DropdownMenuItem<int>>((int value) {
                      return DropdownMenuItem<int>(
                        value: value,
                        child: Text(value.toString()),
                      );
                    }).toList(),
                  ),
                  const Text('奇偶校验: '),
                  DropdownButton<int>(
                    value: parity,
                    style: const TextStyle(color: Colors.deepPurple),
                    underline: Container(
                      height: 2,
                      color: Colors.deepPurpleAccent,
                    ),
                    onChanged: (newValue) {
                      setState(() {
                        parity = newValue!;
                      });
                    },
                    items: parityList.map<DropdownMenuItem<int>>((int value) {
                      return DropdownMenuItem<int>(
                        value: value,
                        child: Text(value.toString()),
                      );
                    }).toList(),
                  ),
                ],
              ),
              Row(
                children: [
                  const Text('数据位: '),
                  DropdownButton<int>(
                    value: dataBits,
                    style: const TextStyle(color: Colors.deepPurple),
                    underline: Container(
                      height: 2,
                      color: Colors.deepPurpleAccent,
                    ),
                    onChanged: (newValue) {
                      setState(() {
                        dataBits = newValue!;
                      });
                    },
                    items: dataBitsList.map<DropdownMenuItem<int>>((int value) {
                      return DropdownMenuItem<int>(
                        value: value,
                        child: Text(value.toString()),
                      );
                    }).toList(),
                  ),
                  const Text('停止位: '),
                  DropdownButton<int>(
                    value: stopBit,
                    style: const TextStyle(color: Colors.deepPurple),
                    underline: Container(
                      height: 2,
                      color: Colors.deepPurpleAccent,
                    ),
                    onChanged: (newValue) {
                      setState(() {
                        stopBit = newValue!;
                      });
                    },
                    items: stopBitList.map<DropdownMenuItem<int>>((int value) {
                      return DropdownMenuItem<int>(
                        value: value,
                        child: Text(value.toString()),
                      );
                    }).toList(),
                  ),
                  ElevatedButton(
                      onPressed: () async {
                        final debounceTransformer =
                        StreamTransformer<dynamic, dynamic>.fromBind(
                                (s) => s.debounce(const Duration(milliseconds: 500)));
                        if (!isPortOpened) {
                          if (sPortPath == '') {
                            Fluttertoast.showToast(
                                msg: '请选择串口！', toastLength: Toast.LENGTH_SHORT);
                            return;
                          }
                          Device theDevice = Device("S4", "S4");
                          var serialPort =
                          await FlutterSerialPort.createSerialPort(
                              theDevice, baudrate,
                              parity: parity,
                              dataBits: dataBits,
                              stopBit: stopBit);
                          bool openResult = await serialPort.open();
                          setState(() {
                            _serialPort = serialPort;
                            isPortOpened = openResult;
                          });
                          _subscription = _serialPort.receiveStream
                              .transform(debounceTransformer)
                              .listen((recv) {
                            print("Receive: $recv");

                            // final temp = Uint8List.fromList(recv);
                            debugPrint(recv);
                            setState(() {
                              readData += '\n\r>>>[$sPortPath]>>> $recv';
                            });
                          });
                        } else {
                          bool closeResult = await _serialPort.close();
                          setState(() {
                            isPortOpened = !closeResult;
                          });
                          print("closeResult: $closeResult");
                        }
                      },
                      child: !isPortOpened ? const Text("开启") : const Text("关闭")),
                ],
              ),
              Row(
                children: [
                  SizedBox(
                    width: 200,
                    child: TextField(
                      decoration: const InputDecoration(
                        contentPadding: EdgeInsets.all(10.0),
                      ),
                      onChanged: _textFieldChanged,
                      autofocus: false,
                    ),
                  ),
                  ElevatedButton(
                      onPressed: () {
                        // _serialPort
                        // .write(Uint8List.fromList(hexToUnits(writeData)));
                        _serialPort.write("AA550403BB66", "HEX");
                      },
                      child: const Text('发送'))
                ],
              )
            ],
          ),
        ),
      ),
    );
  }

  void _textFieldChanged(String str) {
    writeData = str;
  }

  String intToHex(int i, {int pad = 2}) {
    return i.toRadixString(16).padLeft(pad, '0').toUpperCase();
  }

  List<int> hexToUnits(String hexStr, {int combine = 2}) {
    hexStr = hexStr.replaceAll(" ", "");
    List<int> hexUnits = [];
    for (int i = 0; i < hexStr.length; i += combine) {
      hexUnits.add(hexToInt(hexStr.substring(i, i + combine)));
    }
    return hexUnits;
  }

  int hexToInt(String hex) {
    return int.parse(hex, radix: 16);
  }

  String formatReceivedData(recv) {
    if (isHexMode) {
      return recv
          .map((List<int> char) => char.map((c) => intToHex(c)).join())
          .join();
    } else {
      return recv.map((List<int> char) => String.fromCharCodes(char)).join();
    }
  }

}
