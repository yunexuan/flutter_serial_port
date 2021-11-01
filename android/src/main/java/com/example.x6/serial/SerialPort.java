package com.example.x6.serial;

/**
 * Created by X6 on 2017/5/4.
 */

import android.util.Log;
import android.widget.Toast;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

    private static final String TAG = "SerialPort";
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;


    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

//        检查访问权限，如果没有读写权限，进行文件操作，修改文件访问权限
        if (!device.canRead() || !device.canWrite()) {
            try {
                File suFile = new File("/system/xbin/su");
                Process su=null;
                if(suFile.exists()) {
                    //通过挂在到linux的方式，修改文件的操作权限
                    su = Runtime.getRuntime().exec("/system/xbin/su");
                }else{
                    su = Runtime.getRuntime().exec("/system/bin/su");
                }
                //一般的都是/system/bin/su路径，有的也是/system/xbin/su
                String cmd = "chmod 777 " + device.getAbsolutePath() + "\n" + "exit\n";
                Log.e("cmd :",cmd);
                su.getOutputStream().write(cmd.getBytes());

                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
//                ToastUtil.showLong("串口初始化有问题，请确保串口配置选择正确");
//                close();
                throw new SecurityException();
            }
        }

        mFd = open(device.getAbsolutePath(), baudrate, flags);

        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }

        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);

    }


    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI(调用java本地接口，实现串口的打开和关闭)
    /**
     * @param path     串口设备的据对路径
     * @param baudrate 波特率
     * @param flags    校验位
     */
    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    static {//加载jni下的C文件库
        System.loadLibrary("serial_port");
    }
}