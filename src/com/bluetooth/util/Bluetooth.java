package com.bluetooth.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Bluetooth {

	private final static String TAG="Bluetooth";
	/**
	 * mode 两种模式/串口-聊天
	 */
	public final static String mode="串口"; 
	
	/**
	 * 检查设备是否支持蓝牙   
	 * @return
	 */
	public static boolean isSupport(BluetoothAdapter bluetoothAdapter){
		if (bluetoothAdapter == null) {  
			return false;
		}else{
			return true;
		}  
    }
	
	
	/**
	 * 蓝牙设备是否可以使用
	 * @return
	 */
	public static void isEnabled(Activity activity,BluetoothAdapter bluetoothAdapter){
		if (!bluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			activity.startActivityForResult(enableIntent, 1);
		} else {
			Log.d(TAG,"蓝牙设备可以使用");
		}
    }
	
	
	/**
	 * 得到已经配对的list
	 * @return
	 */
	public static void getBondedDevicesList(BluetoothAdapter bluetoothAdapter){
		//将已配对的蓝牙设备显示到第一个ListView中
		Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
		final List<BluetoothDevice> bondedDevices = new ArrayList<BluetoothDevice>();
		if (deviceSet.size() > 0) {
			for (Iterator<BluetoothDevice> it = deviceSet.iterator(); it.hasNext();) {
				BluetoothDevice device = (BluetoothDevice) it.next();
				bondedDevices.add(device);
			}
		}
    }
	
	
	
	/**
	 * 得到新发现设备的list
	 * @return
	 */
	public static void getFoundDevicesList(BluetoothAdapter bluetoothAdapter){
		//将已配对的蓝牙设备显示到第一个ListView中
		Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
		final List<BluetoothDevice> bondedDevices = new ArrayList<BluetoothDevice>();
		if (deviceSet.size() > 0) {
			for (Iterator<BluetoothDevice> it = deviceSet.iterator(); it.hasNext();) {
				BluetoothDevice device = (BluetoothDevice) it.next();
				bondedDevices.add(device);
			}
		}
    }
	
	

	
	
	
	
	


}
