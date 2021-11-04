package com.example.x6.serialportlib;

import android.util.Log;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


public class SerialPort {

	private SerailPortOpt serialportopt;
	private InputStream mInputStream;
	public boolean isOpen = false;
	

	String data;


	public SerialPort(String devNum, int speed, int dataBits, int stopBits,
                      int parity) {
		serialportopt = new SerailPortOpt();
		openSerial(devNum, speed, dataBits, stopBits, parity);
	}


	private boolean openSerial(String devNum, int speed, int dataBits,
                               int stopBits, int parity) {
		serialportopt.mDevNum = devNum;
		serialportopt.mDataBits = dataBits;
		serialportopt.mSpeed = speed;
		serialportopt.mStopBits = stopBits;
		serialportopt.mParity = parity;


		FileDescriptor fd = serialportopt.openDev(serialportopt.mDevNum);
		if (fd == null) {
			return false;
		} else {

			serialportopt.setSpeed(fd, speed);
			serialportopt.setParity(fd, dataBits, stopBits, parity);
			mInputStream = serialportopt.getInputStream();
			isOpen = true;
			return true;
		}
	}


	public void closeSerial() {
		if (serialportopt.mFd != null) {
			serialportopt.closeDev(serialportopt.mFd);
			isOpen = false;
		}
	}


	public int sendData(byte[] bytes){
		serialportopt.writeBytes(bytes);
		return bytes.length;
	}

	public int receiveData(byte[] bytes){
		int length = 0;
		if(mInputStream == null){
			return length;
		}
		length = serialportopt.readBytes(bytes);
		return length;
	}

	public byte[] receiveData(){
		byte[] buf = new byte[1024];
		int size;
		if(mInputStream == null){
			return  null;
		}
		size = serialportopt.readBytes(buf);
		if(size > 0)
			return buf;
		else
			return null;
	}

	public byte[] receiveData(int length){
		byte[] bytes = new byte[length];
		int size = 0;
		if(mInputStream == null)
			return null;
		long startTime = System.currentTimeMillis();
		while (true){
			size += serialportopt.readBytes(bytes);
			if (size >= length)
				break;
			else if(System.currentTimeMillis() - startTime >= 1*1000){
				break;
			}
		}
		return bytes;
	}

	public void sendData(String data, String type) {
		try {
			Log.d("serialPort", "receive data:" + data);
			serialportopt.writeBytes(type.equals("HEX") ? HexString2Bytes(data
					.length() % 2 == 1 ? data += "0" : data.replace(" ", ""))
					: HexString2Bytes(toHexString(data)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public String receiveData(String type) {
		byte[] buf = new byte[1024];
		int size;
		if (mInputStream == null) {
			return null;
		}
		size = serialportopt.readBytes(buf);
		if (size > 0) {
			try {
				data = type.equals("HEX") ? bytesToHexString(buf, size)
						: new String(buf, 0, size, "gb2312").trim().toString();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return data;
		} else {
			return null;
		}
	}


	private String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;
	}


	public static byte[] HexString2Bytes(String src) {
		byte[] ret = new byte[src.length() / 2];
		byte[] tmp = src.getBytes();
		for (int i = 0; i < tmp.length / 2; i++) {
			ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
		}
		return ret;
	}


	public static String bytesToHexString(byte[] src, int size) {
		String ret = "";
		if (src == null || size <= 0) {
			return null;
		}
		for (int i = 0; i < size; i++) {
			String hex = Integer.toHexString(src[i] & 0xFF);
			if (hex.length() < 2) {
				hex = "0" + hex;
			}
			hex += " ";
			ret += hex;
		}
		return ret.toUpperCase();
	}


	private static byte uniteBytes(byte src0, byte src1) {
		byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
				.byteValue();
		_b0 = (byte) (_b0 << 4);
		byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
				.byteValue();
		byte ret = (byte) (_b0 ^ _b1);
		return ret;
	}

}
