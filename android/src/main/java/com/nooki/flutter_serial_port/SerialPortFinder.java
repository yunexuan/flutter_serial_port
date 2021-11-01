package com.nooki.flutter_serial_port;

import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

public class SerialPortFinder {

    private static final String TAG = "SerialPort";
    private Vector<Driver> mDrivers = null;

    Vector<Driver> getDrivers() throws IOException {
        if (mDrivers == null) {
            mDrivers = new Vector<>();
            LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
            String l;
            while((l = r.readLine()) != null) {
                String driverName = l.substring(0, 0x15).trim();
                String[] w = l.split(" +");
                if ((w.length >= 5) && (w[w.length-1].equals("serial"))) {
                    Log.d(TAG, "Found new driver " + driverName + " on " + w[w.length-4]);
                    mDrivers.add(new Driver(driverName, w[w.length-4]));
                }
            }
            r.close();
        }
        return mDrivers;
    }

    public String[] getAllDevices() {
        Vector<String> devices = new Vector<>();
        Iterator<Driver> driverIterator;
        try {
            driverIterator = getDrivers().iterator();
            while(driverIterator.hasNext()) {
                Driver driver = driverIterator.next();
                for (File file : driver.getDevices()) {
                    String device = file.getName();
                    String value = String.format("%s (%s)", device, driver.getName());
                    devices.add(value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return devices.toArray(new String[devices.size()]);
    }

    public String[] getAllDevicesPath() {
        Vector<String> devices = new Vector<>();
        Iterator<Driver> driverIterator;
        try {
            driverIterator = getDrivers().iterator();
            while(driverIterator.hasNext()) {
                Driver driver = driverIterator.next();
                for (File file : driver.getDevices()) {
                    String device = file.getAbsolutePath();
                    devices.add(device);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return devices.toArray(new String[devices.size()]);
    }
}
