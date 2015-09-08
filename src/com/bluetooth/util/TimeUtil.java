package com.bluetooth.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;





/**
 * 
 * 创建时间：2014-6-12 下午3:16:15  
 * 项目名称：W
 * 文件名称：TimeUtil.java    
 * @author ShuiTong
 *
 * 类说明：  时间工具
 * 
 */

public class TimeUtil{

	
	/**
	 * 用Calendar.getInstance()获取时间方式
	 */
	public static String getCurrentTime(){
		Calendar ca = Calendar.getInstance();
		int year = ca.get(Calendar.YEAR);//获取年份
		int month=ca.get(Calendar.MONTH);//获取月份
		int day=ca.get(Calendar.DATE);//获取日
		int hour=ca.get(Calendar.HOUR);//小时
		int minute=ca.get(Calendar.MINUTE);//分
		int second=ca.get(Calendar.SECOND);//秒
		int WeekOfYear = ca.get(Calendar.DAY_OF_WEEK);
		System.out.println("用Calendar.getInstance().getTime()方式显示时间: " + ca.getTime());
		System.out.println("用Calendar获得日期是：" + year +"年"+ month +"月"+ day + "日"+hour+"时"+minute+"分"+second+"秒");
		String  currentTime="日期是：" + year +"年"+ month +"月"+ day + "日"+hour+"时"+minute+"分"+second+"秒";
		return currentTime;
	}
	
	
	/**
	 * 比较两个字符串大小
	 * @param timeBef
	 * @param timeCur
	 * @return 1  -1  0 三种状态
	 */
	public static int compareTime(String timeBef,String timeCur){
		return timeBef.compareToIgnoreCase(timeCur);
	}
	
	


}
