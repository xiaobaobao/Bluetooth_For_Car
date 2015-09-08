package com.bluetooth.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
/*
已建立连接后启动的线程，需要传进来两个参数
socket用来获取输入流，读取远程蓝牙发送过来的消息
handler用来在收到数据时发送消息
*/
public class ConnectedThread extends Thread {
	
	private static final int RECEIVE_MSG = 7;
	private static final int SEND_MSG=8;
	private boolean isStop;
	private BluetoothSocket socket;
	private Handler handler;
	private InputStream is;
	private OutputStream os;
	
	public ConnectedThread(BluetoothSocket s,Handler h){
		socket=s;
		handler=h;
		isStop=false;
	}
	public void run(){
		StringBuffer sizeBuf=new StringBuffer(3);
		System.out.println("connectedThread.run()");
		byte[] buf;
		int size;
		while(!isStop){
			size=0;
			buf=new byte[1024];
//			buf=new byte[2];
			try {
				is=socket.getInputStream();
//				System.out.println("等待数据");
				size=is.read(buf);
//				System.out.println("读取了一次数据");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isStop=true;
			}
			if(size>0){
				//把读取到的数据放进Bundle再放进Message，然后发送出去
				System.out.println("buf.length:"+buf.length);
				System.out.println("size:"+size);
				for (int i = 0; i < size; i++) {
//					System.out.println("buf["+i+"]: "+buf[i]);
				}
				/*try {
					System.out.println("getHexString: "+DigitalTrans.getHexString(buf, 0, 2));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				if(size==1){
					sizeBuf.append(buf[0]+"");
					if(sizeBuf.toString().length()==2){
//						System.out.println("发送-------size==1");
						sendMessageToHandler(sizeBuf.toString(), 8);
						sizeBuf.delete(0, 2);
					}
				}
				if(size==2){
					String v=buf[0]+""+buf[1];
//					handler.obtainMessage(8, size, -1, v).sendToTarget();   //传递给UI线程
//					System.out.println("发送-------size==2");
					sendMessageToHandler(v, 8);
				}
//				String v= buf[0]+""+buf[1];
//				try {
//					System.out.println("-------------v:  "+DigitalTrans.AsciiStringToString(new String(buf,"ISO-8859-1")));
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//				handler.obtainMessage(8, size, -1, buf).sendToTarget();   //传递给UI线程
//				sendMessageToHandler(DigitalTrans.bytetoString(buf), RECEIVE_MSG);
				
				//sendMessageToHandler(buf, RECEIVE_MSG);//源码
			}
		}
	}
	
	public void write(byte[] buf){
		try {
			os=socket.getOutputStream();
			os.write(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(buf.length+"--writewritewritewritewritewrite-");
		for (int i = 0; i < buf.length; i++) {
			System.out.println("buf["+i+"]: "+buf[i]);
		}
		sendMessageToHandler(buf, SEND_MSG);//源码
	}
	
	private void sendMessageToHandler(byte[] buf,int mode){
		String msgStr=null;
		try {
			msgStr = new String(buf,"GBK");
			System.out.println("gbk msgStr: "+msgStr);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Bundle bundle=new Bundle();
		bundle.putString("str", msgStr);
		Message msg=new Message();
		msg.setData(bundle);
		msg.what=mode;
		handler.sendMessage(msg);
	}
	
	/**
	 * 自己修改的
	 * @param buf
	 * @param mode
	 */
	private void sendMessageToHandler(String v,int mode){
		Bundle bundle=new Bundle();
		bundle.putString("str", v);
		Message msg=new Message();
		msg.setData(bundle);
		msg.what=mode;
		handler.sendMessage(msg);
	}
	
	
	
	
}
