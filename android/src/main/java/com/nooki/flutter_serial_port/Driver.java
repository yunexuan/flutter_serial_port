package com.nooki.flutter_serial_port;

import java.io.File;
import java.util.Vector;

public class Driver {
    private final String mDriverName;
    private final String mDriverRoot;

    public Driver(String name, String root) {
        mDriverName = name;
        mDriverRoot = root;
    }

    Vector<File> mDevices = null;

    public Vector<File> getDevices() {
        if (mDevices == null) {
            mDevices = new Vector<>();
            File dev = new File("/dev");
            File[] files = dev.listFiles();
            if (files != null) {
                int i;
                for (i = 0; i < files.length; i++) {
                    if (files[i].getAbsolutePath().startsWith(mDriverRoot)) {
                        mDevices.add(files[i]);
                    }
                }
            }
        }
        return mDevices;
    }

    public String getName() {
        return mDriverName;
    }
}
