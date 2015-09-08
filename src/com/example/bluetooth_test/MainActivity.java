package com.example.bluetooth_test;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.bluetooth.util.AcceptThread;
import com.bluetooth.util.Bluetooth;
import com.bluetooth.util.ConnectThread;
import com.bluetooth.util.ConnectedThread;
import com.bluetooth.util.DeviceListAdapter;
import com.bluetooth.util.DigitalTrans;
import com.bluetooth.util.TimeUtil;
import com.example.bluetooth_test.R.string;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.util.JsonParser;
import com.looip.util.Tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnClickListener{
    
	 //view
	 /** 蓝牙 连接状态  */
	 private TextView  bt_status;
	 /** 蓝牙 传回的数据  */
	 private TextView  bt_back_data_text;
	 /** 蓝牙  连接按钮和   断开按钮 */
	 private Button    bt_connect,bt_break;
	 /** 蓝牙 发送数据的输入框  */
	 private EditText  bt_send_data_text;
	 /** 蓝牙 发送按钮 */
	 private Button    bt_send;
	 /** 蓝牙 语音发送框  */
	 private TextView  voice_send_data;
	 /** 蓝牙 语音发送按钮 */
	 private Button    voice_send;
	 
	 //data
	 
	 
	 //
	 /** 与远程蓝牙连接成功时启动 */
	 private ConnectedThread connectedThread; 
	 /** 用户点击列表中某一项，要与远程蓝牙连接时启动 */
	 private ConnectThread connectThread; 
	 /** 用户选择蓝牙聊天时立即启动 */
	 private AcceptThread acceptThread;  
	 // 连接设备对话框相关控件
	 private Dialog blueToothDialog;
	 private ProgressBar discoveryPro;
	 private ListView foundList;
	 List<BluetoothDevice> foundDevices;
	 BluetoothSocket bluetoothSocket;
	 BluetoothAdapter bluetoothAdapter = null;
	 // 广播接收器，主要是接收蓝牙状态改变时发出的广播
	 private BroadcastReceiver mReceiver;
//	 private DeviceListAdapter deviceListAdapter;
	 private  boolean isChat;
	 

	// 消息处理器使用的常量
	private static final int FOUND_DEVICE = 1; // 发现设备
	private static final int START_DISCOVERY = 2; // 开始查找设备
	private static final int FINISH_DISCOVERY = 3; // 结束查找设备
	private static final int CONNECT_FAIL = 4; // 连接失败
	private static final int CONNECT_SUCCEED_P = 5; // 主动连接成功
	private static final int CONNECT_SUCCEED_N = 6; // 收到连接成功
	private static final int RECEIVE_MSG = 7; // 收到消息
	private static final int SEND_MSG = 8; // 发送消息
		
	
	
	
	 /** 讯飞模块view*/
	 /** 语音听写对象 */
	 private SpeechRecognizer mIat;
	 /** 语音听写UI */
	 private RecognizerDialog iatDialog;
	 Toast mToast;
	
	 
	 StringBuffer stringBuffer=new StringBuffer(100);
	 
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bt_main);
		
		findView();
		
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(!Bluetooth.isSupport(bluetoothAdapter)){
			Tools.showToast(getApplication(), "该设备没有蓝牙设备");
			return;
		}else{
			Bluetooth.isEnabled(this, bluetoothAdapter);
			chooseMode();
		}
		
		mIntentFilter();
	
		initSpeechRecognizer();
		
	}
	
	
	    // 消息处理器..日理万鸡的赶脚...
		private Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case FOUND_DEVICE:
					foundList.setAdapter(new DeviceListAdapter(MainActivity.this,foundDevices));
					break;
				case START_DISCOVERY:
					discoveryPro.setVisibility(View.VISIBLE);
					break;
				case FINISH_DISCOVERY:
					discoveryPro.setVisibility(View.GONE);
					break;
				case CONNECT_FAIL:
					Tools.showToast(getApplication(), "连接失败");
					break;
				case CONNECT_SUCCEED_P:
				case CONNECT_SUCCEED_N:
					System.out.println("连接成功-----");
					if (msg.what == CONNECT_SUCCEED_P) {
						if (acceptThread != null) {
							acceptThread.interrupt();
						}
						bluetoothSocket = connectThread.getSocket();
						connectedThread = new ConnectedThread(bluetoothSocket, mHandler);
						connectedThread.start();
					} else {
						if (connectThread != null) {
							connectThread.interrupt();
						}
						bluetoothSocket = acceptThread.getSocket();
						connectedThread = new ConnectedThread(bluetoothSocket, mHandler);
						connectedThread.start();
					}

					String stateStr = msg.getData().getString("name");
//					if (isChat) {
//						stateStr = "蓝牙聊天：" + "与" + stateStr + "聊天中";
//					} else {
						stateStr = "串口工具：" + "与" + stateStr + "连接中";
//					}
					bt_status.setText("## "+stateStr+" ##");
					Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
					break;
				case RECEIVE_MSG:
				case SEND_MSG:
					String chatStr = msg.getData().getString("str");
					System.out.println("******chatStr: "+chatStr);
					if(stringBuffer.length()>150){
						stringBuffer.delete(0,150);
					}
					stringBuffer.append(TimeUtil.getCurrentTime()+": "+chatStr+"℃\n");
					bt_back_data_text.setText(stringBuffer.toString());
					
////				byte[] bs =DigitalTrans.hex2byte(chatStr);
////				chatStr=bs[0]+"";
//					TextView text = new TextView(MainActivity.this);
//					text.setText(TimeUtil.getCurrentTime()+": "+chatStr+"℃");
//					if (msg.what == SEND_MSG) {
//						text.setBackgroundResource(R.drawable.chat_i);
//						text.setPadding(40, 10, 30, 10);
//					} else {
//						text.setBackgroundResource(R.drawable.chat_u);
//						text.setPadding(80, 10, 30, 10);
//					}
//					text.setTextSize(15);
//					text.setTextColor(Color.BLACK);
//					chatContent.addView(text);
//					scrollView.scrollTo(0, chatContent.getHeight());
					break;
				}
			}

		};
	
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_connect:
			linkBtn();
			break;
		case R.id.bt_break:
			
			break;
		case R.id.bt_send:
			//发送
			if (bluetoothSocket == null) {
				Toast.makeText(MainActivity.this, "请先连接蓝牙设备",
						Toast.LENGTH_SHORT).show();
			}else if(bt_send_data_text.getText().toString().equals("")){
				Toast.makeText(MainActivity.this, "发送内容不能为空",
						Toast.LENGTH_SHORT).show();
			}else {
				String sendStr = bt_send_data_text.getText().toString();
//				DigitalTrans.StringToAsciiString(sendStr);
//				str2HexStr(sendStr);
				System.out.println("str2HexStr(sendStr): "+str2HexStr(sendStr));
//				System.out.println("DigitalTrans.str2HexStr(sendStr)： "+DigitalTrans.str2HexStr(sendStr));
//				System.out.println("sendStr.getBytes()： "+sendStr.getBytes().toString());				
				connectedThread.write(DigitalTrans.hex2byte(str2HexStr(sendStr)));
//				connectedThread.write(sendStr.getBytes());
//				bt_send_data_text.setText("");
			}
			break;
		case R.id.voice_send:
			voice_send_data.setText(null);
			setParam();
			iatDialog.setListener(recognizerDialogListener);
			iatDialog.show();
			break;
		default:
			break;
		}
	}
	
	/**   
	 * 字符串转换成十六进制字符串  
	 * @param String str 待转换的ASCII字符串  
	 * @return String 每个Byte之间空格分隔，如: [61 6C 6B]  
	 */      
	public static String str2HexStr(String str)    
	{      	  
	    char[] chars = "0123456789ABCDEF".toCharArray();      
	    StringBuilder sb = new StringBuilder("");    
	    byte[] bs = str.getBytes();      
	    int bit;      
	        
	    for (int i = 0; i < bs.length; i++)    
	    {      
	        bit = (bs[i] & 0x0f0) >> 4;      
	        sb.append(chars[bit]);      
	        bit = bs[i] & 0x0f;      
	        sb.append(chars[bit]);    
	        sb.append(' ');    
	    }      
	    return sb.toString().trim();      
	}    
	
	/**
	 *  请求打开蓝牙的回调函数，若无法打开，则关闭程序
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (requestCode) {
		case 1:
			if (resultCode != Activity.RESULT_OK) {
				Tools.showToast(getApplication(), "无法打开蓝牙，程序关闭");
			} else {
				chooseMode();
			}
			break;
			}
	 }
		
	
	/**
	 * 开始连接
	 * @param device
	 */
	public void connect(BluetoothDevice device) {
		bluetoothAdapter.cancelDiscovery();
		blueToothDialog.dismiss();
		Tools.showToast(getApplication(), "正在与 " + device.getName() + " 连接 .... ");
		connectThread = new ConnectThread(device, mHandler,isChat);
		connectThread.start();
	}
	
	
	
	
	
	/**
	 * 蓝牙连接按钮
	 */
	public void linkBtn(){
		bluetoothAdapter.cancelDiscovery();
		bluetoothAdapter.startDiscovery();
		/*
		 * 通过LayoutInflater得到对话框中的三个控件
		 * 第一个ListView为局部变量，因为它显示的是已配对的蓝牙设备，不需随时改变
		 * 第二个ListView和ProgressBar为全局变量
		 */
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.bt_dialog, null);
		discoveryPro = (ProgressBar)view.findViewById(R.id.discoveryPro);
		ListView bondedList = (ListView)view.findViewById(R.id.bondedList);
		foundList = (ListView) view.findViewById(R.id.foundList);
		// 将已配对的蓝牙设备显示到第一个ListView中
		Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
		final List<BluetoothDevice> bondedDevices = new ArrayList<BluetoothDevice>();
		if (deviceSet.size() > 0) {
			for (Iterator<BluetoothDevice> it = deviceSet.iterator(); it.hasNext();) {
				BluetoothDevice device = (BluetoothDevice) it.next();
				bondedDevices.add(device);
			}
		}
		bondedList.setAdapter(new DeviceListAdapter(MainActivity.this,bondedDevices));
		// 将找到的蓝牙设备显示到第二个ListView中
		foundDevices = new ArrayList<BluetoothDevice>();
		foundList.setAdapter(new DeviceListAdapter(MainActivity.this,foundDevices));
		//两个ListView绑定监听器
		bondedList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				BluetoothDevice device = bondedDevices.get(arg2);
				connect(device);
			}
		});
		foundList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				BluetoothDevice device = foundDevices.get(arg2);
				connect(device);
			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage("请选择要连接的蓝牙设备").setPositiveButton("取消",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						// TODO Auto-generated method stub
						bluetoothAdapter.cancelDiscovery();
					}
				});
		builder.setView(view);
		builder.create();
		blueToothDialog = builder.show();
	}
	
	
	/**
	 * 注册信息
	 */
	private void mIntentFilter(){
		
		// 注册广播接收器
		mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					// TODO Auto-generated method stub
					String actionStr = arg1.getAction();
					if (actionStr.equals(BluetoothDevice.ACTION_FOUND)) {
						BluetoothDevice device = arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						foundDevices.add(device);
						Toast.makeText(MainActivity.this,"找到蓝牙设备：" + device.getName(), Toast.LENGTH_SHORT).show();
						mHandler.sendEmptyMessage(FOUND_DEVICE);
					} else if (actionStr.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
						mHandler.sendEmptyMessage(START_DISCOVERY);
					} else if (actionStr.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
						mHandler.sendEmptyMessage(FINISH_DISCOVERY);
					} 
				}
			};
			IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			registerReceiver(mReceiver, filter1);
			registerReceiver(mReceiver, filter2);
			registerReceiver(mReceiver, filter3);
	}
	
	
	
	/**
	 * 退出要做的一些步骤
	 * @param device
	 */
	public void exitDo(BluetoothDevice device) {
		unregisterReceiver(mReceiver);
		if (bluetoothSocket != null) {
			try {
				bluetoothSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (connectThread != null) {
			connectThread.interrupt();
		}
		if (connectedThread != null) {
			connectedThread.interrupt();
		}
		if (acceptThread != null) {
			acceptThread.interrupt();
		}
		if(bluetoothAdapter.isEnabled()){
			Tools.showToast(getApplication(), "请手动关闭蓝牙");
		}
	}
	
	
	
	
	
	
	/*
	 * 显示对话框，选择使用蓝牙聊天还是串口工具 两都的区别在于连接时使用的UUID不同
	 * 而且作为串口工具时无须启动监听远程连接的acceptThread
	 */
	public void chooseMode() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("请选择使用蓝牙聊天还是串口工具");
		builder.setPositiveButton("蓝牙聊天",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Tools.showToast(getApplication(), "你选择了使用蓝牙聊天");
						isChat = true;
						// 启动监听蓝牙连接的线程
						acceptThread = new AcceptThread(mHandler,bluetoothAdapter);
						acceptThread.start();
					}
				});
		builder.setNegativeButton("串口工具",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Tools.showToast(getApplication(), "你选择了使用串口工具");
						isChat = false;
					}
				});
		builder.create().show();
	}	
	
	
	
	
	
	private void findView(){
		bt_status =(TextView) findViewById(R.id.bt_status);
		bt_back_data_text =(TextView) findViewById(R.id.bt_back_data);
		bt_connect =(Button) findViewById(R.id.bt_connect);
		bt_connect.setOnClickListener(this);
		bt_break =(Button) findViewById(R.id.bt_break);
		bt_break.setOnClickListener(this);
		bt_send=(Button) findViewById(R.id.bt_send);
		bt_send.setOnClickListener(this);
		bt_send_data_text =(EditText) findViewById(R.id.bt_send_data);
		voice_send_data =(TextView) findViewById(R.id.voice_send_data);
		voice_send =(Button) findViewById(R.id.voice_send);
		voice_send.setOnClickListener(this);
	}

	
	
	/**
	 * 初始化监听器。
	 */
	private void initSpeechRecognizer(){
		// 初始化识别对象
		mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
		// 初始化听写Dialog,如果只使用有UI听写功能,无需创建SpeechRecognizer
		iatDialog = new RecognizerDialog(this,mInitListener);
	}
	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d("mInitListener", "SpeechRecognizer init() code = " + code);
			if (code == ErrorCode.SUCCESS) {
				findViewById(R.id.voice_send).setEnabled(true);
			}
		}
	};
	/**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener recognizerDialogListener=new RecognizerDialogListener(){
		public void onResult(RecognizerResult results, boolean isLast) {
			String text = JsonParser.parseIatResult(results.getResultString());
			Log.e("听写UI监听器", "onResult: "+text);
			voice_send_data.append(text);
//			voice_send_data.setSelection(voice_send_data.length());
		}
		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			Tools.showToast(getApplication(),error.getPlainDescription(true));
		}
	};
	/**
	 * 参数设置
	 * @param param
	 * @return 
	 */
	private void setParam(){
		// 设置语言
		//mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		// 设置语言
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语言
		//mIat.setParameter(SpeechConstant.ACCENT,lag);
		// 设置语音前端点
		mIat.setParameter(SpeechConstant.VAD_BOS,"4000");
		// 设置语音后端点
		mIat.setParameter(SpeechConstant.VAD_EOS,"1000");
		// 设置标点符号
		mIat.setParameter(SpeechConstant.ASR_PTT,"1");
		// 设置音频保存路径
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, "/sdcard/iflytek/wavaudio.pcm");
	}
	
	
	
	
	
	
	
	
/*	
	
	
	  public byte[] getStringhex(String paramString)
	  {
	    String str = paramString.replaceAll(" ", "");
	    Log.v("getStringhex", str);
	    char[] arrayOfChar = str.toCharArray();
	    byte[] arrayOfByte = new byte[arrayOfChar.length / 2];
	    int j = 0;
	    int k = 0;
	    int m = 0;
	    if (m >= arrayOfChar.length)
	      return arrayOfByte;
	    int n = 0xFF & arrayOfChar[m];
	    if (((n > 47) && (n < 58)) || ((n > 64) && (n < 71)) || ((n > 96) && (n < 103)))
	      if (k == 0)
	      {
	        Log.v("getStringhex", "F True");
	        arrayOfByte[j] = (byte)(arrayOfByte[j] | 16 * getASCvalue(arrayOfChar[m]));
	        Log.v("getStringhex", String.valueOf(arrayOfByte[j]));
	        k = 1;
	      }
	    while (true)
	    {
	      m++;
	      break;
	      arrayOfByte[j] = (byte)(arrayOfByte[j] | getASCvalue(arrayOfChar[m]));
	      Log.v("getStringhex", "F false");
	      Log.v("getStringhex", String.valueOf(arrayOfByte[j]));
	      j++;
	      k = 0;
	      continue;
	      if (n != 32)
	        continue;
	      Log.v("getStringhex", "spance");
	      if (k == 0)
	        continue;
	      j++;
	      k = 0;
	    }
	  }

	  public static byte getASCvalue(char paramChar)
	  {
	    switch (paramChar)
	    {
	    default:
	      return 0;
	    case '0':
	      return 0;
	    case '1':
	      return 1;
	    case '2':
	      return 2;
	    case '3':
	      return 3;
	    case '4':
	      return 4;
	    case '5':
	      return 5;
	    case '6':
	      return 6;
	    case '7':
	      return 7;
	    case '8':
	      return 8;
	    case '9':
	      return 9;
	    case 'a':
	      return 10;
	    case 'b':
	      return 11;
	    case 'c':
	      return 12;
	    case 'd':
	      return 13;
	    case 'e':
	      return 14;
	    case 'f':
	      return 15;
	    case 'A':
	      return 10;
	    case 'B':
	      return 11;
	    case 'C':
	      return 12;
	    case 'D':
	      return 13;
	    case 'E':
	      return 14;
	    case 'F':
	    }
	    return 15;
	  }
*/
	
	
	
	
	
	
	
	
	
	
	
}
