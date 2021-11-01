package com.nooki.flutter_serial_port;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.x6.serialportlib.SerialPort;
import java.util.ArrayList;
import java.util.Arrays;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** FlutterSerialPortPlugin */
public class FlutterSerialPortPlugin implements FlutterPlugin, MethodCallHandler,EventChannel.StreamHandler {

  private MethodChannel channel;

  private static final String TAG = "FlutterSerialPortPlugin";
  private final SerialPortFinder serialPortFinder = new SerialPortFinder();
  protected SerialPort serialPort;
  private ReadThread readThread;
  private EventChannel.EventSink eventSink;
  private final Handler handler = new Handler(Looper.getMainLooper());

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_serial_port");
    channel.setMethodCallHandler(this);

    final EventChannel eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_serial_port/event");
    eventChannel.setStreamHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getPlatformVersion":
        result.success("Android " + Build.VERSION.RELEASE);
        break;
      case "open":
        final String devicePath = call.argument("devicePath");
        final int baudRate = call.argument("baudRate");
        final int parity = call.argument("parity");
        final int dataBits = call.argument("dataBits");
        final int stopBit = call.argument("stopBit");
        Boolean openResult = openDevice(devicePath, baudRate, parity, dataBits, stopBit);
        result.success(openResult);
        break;
      case "close":
        Boolean closeResult = closeDevice();
        result.success(closeResult);
        break;
      case "write":
        Boolean writeResult = writeData(call.argument("data"), call.argument("type"));
        result.success(writeResult);
        break;
      case "getAllDevices":
        ArrayList<String> devices = getAllDevices();
        Log.d(TAG, "AllDevices:" + devices.toString());
        result.success(devices);
        break;
      case "getAllDevicesPath":
        ArrayList<String> devicesPath = getAllDevicesPath();
        Log.d(TAG, "AllDevicesPath:" + devicesPath.toString());
        result.success(devicesPath);
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    eventSink = events;
  }

  @Override
  public void onCancel(Object arguments) {
    eventSink = null;
  }

  private ArrayList<String> getAllDevices() {
    ArrayList<String> devices = new ArrayList<>(Arrays.asList(serialPortFinder.getAllDevices()));
    return devices;
  }

  private ArrayList<String> getAllDevicesPath() {
    ArrayList<String> devicesPath = new ArrayList<>(Arrays.asList(serialPortFinder.getAllDevicesPath()));
    return devicesPath;
  }

  private Boolean openDevice(String devicePath, int baudRate, int parity, int dataBits, int stopBit) {
    if (serialPort == null) {
      if ((devicePath.length() == 0) || (baudRate == -1)) {
        return false;
      }
      try {
        serialPort = new SerialPort(devicePath, baudRate, parity, dataBits, stopBit);
        readThread = new ReadThread();
        readThread.start();
        return true;
      } catch (Exception e) {
        Log.e(TAG, e.toString());
        return false;
      }
    }
    return false;
  }

  private Boolean closeDevice() {
    try {
      if (null != readThread) {
        readThread.interrupt();
        readThread = null;
      }
      if (serialPort != null) {
        serialPort.closeSerial();
        serialPort = null;
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  private Boolean writeData(String data, String type) {
    try {
      if ("byte".equals(type)) {
        serialPort.sendData(SerializeUtil.hexStringToByteArray(data));
      } else {
        serialPort.sendData(data, type);
      }
      return true;
    } catch (Exception e) {
      Log.e(TAG, e.toString());
      return false;
    }
  }

  protected void onDataReceived(final byte[] buffer, final int size) {
    if (eventSink != null) {
      handler.post(new Runnable() {
        @Override
        public void run() {
          int tmpSize = size * 2;
          String serialData = SerializeUtil.byteArrayToHexString(buffer).substring(0, tmpSize);
          eventSink.success(serialData);
        }
      });
    }
  }

  private class ReadThread extends Thread{

    @Override
    public void run() {
      super.run();
      while (!isInterrupted()) {
        int size;
        try {
          byte[] buffer = new byte[1024];
          size = serialPort.receiveData(buffer);
          if (size > 0) {
            onDataReceived(buffer, size);
          }
          try {
            Thread.sleep(10);//延时10ms
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        } catch (Exception e) {
          e.printStackTrace();
          return;
        }
      }
    }
  }
}
