package com.example.bluetooth_test;

import com.iflytek.cloud.SpeechUtility;

import android.app.Application;

public class SpeechApp extends Application{

	
	 /** 这个appid  是到官网申请得到 */
	 final String appid ="53aa76d4";
			
	@Override
	public void onCreate() {
		// 应用程序入口处调�?避免手机内存过小，杀死后台进�?造成SpeechUtility对象为null
		// 设置你申请的应用appid
		SpeechUtility.createUtility(SpeechApp.this, "appid="+appid);
		super.onCreate();
	}

}
