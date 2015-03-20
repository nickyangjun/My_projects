package com.boromax.bthandset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.widget.Toast;

public class BluetoothService extends Service{
	 private static final String TAG = "BTService";
	 private String PREFS = "bluetoothlauncher";
	 
	  private BluetoothAdapter mBluetoothAdapter;
	  final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	  UUID MY_UUID = UUID.fromString(SPP_UUID); 
	  
	  
	  private static final int mesg_connect = 40;     //已连接
	  private static final int mesg_disconnect = mesg_connect+1;   //连接断开
	  private static final int MESSAGE_READ = mesg_connect+2;     //有数据
	  private static final int mesg_connect_fail = mesg_connect+3; 
	  
	  
	  
	  private boolean connecting = false;
	  private boolean connected = false;
	  private int connetTime=0;
	  
	  onDataReceived monDataReceived=null;
	  private ConnectThread mConnectThread=null;       //蓝牙连接线程
	  private AcceptThread mAcceptThread=null;
	  private readDateThread mreadDateThread2=null;   //数据处理线程
	  private readDateThread mreadDateThread=null;   //数据处理线程
	  private OutputStream mOutStream=null;

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG,"-----onBind----");
		// TODO Auto-generated method stub
		 return new ConnectBinder();  
	}
	
    public class ConnectBinder extends Binder{  
	        /** 
	         * 获取当前Service的实例 
	         * @return 
	         */  
	        public BluetoothService getService(){  
	            return BluetoothService.this;  
	        }  
	}  
	
    @Override
	public void onCreate() {
    	Log.i(TAG,"----onCreate----");
    	  //Get the BluetoothAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
 			Log.i(TAG, "----onStartCommand -----");
           super.onStartCommand(intent, flags, startId);
           
           return Service.START_NOT_STICKY;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        Log.d(TAG, "onStart");
    }
 
    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
    	 Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);    
    }
 
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mConnectThread.cancel();
        mreadDateThread.cancel();
        Log.d(TAG, "onDestroy");
    }
 
    
    public void setonDataReceived(onDataReceived monDataReceived)
    {
    	this.monDataReceived = monDataReceived;
    }
    public interface  onDataReceived{
    	public void onReceived(String buff);
    };
 
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_READ:
                String readBuf = (String) msg.obj;  
                if(monDataReceived != null){
                	monDataReceived.onReceived(readBuf);
                }
                break;
            case mesg_connect:
          	  String str=msg.getData().getString("Btname");
          	  
          	  Log.i(TAG,"setText connected bt name: "+str);
          	
          	  break;
            case mesg_disconnect:
            	  Log.i(TAG,"setText connected bt name: null");
            	 
            	  mConnectThread=null;
            	  break;
            case mesg_connect_fail:
            	  Toast.makeText(getApplicationContext(), "连接失败！！！", 1).show();
            	  mConnectThread=null;
            	break;
            }
            }
    };
    
    public void ConnectDevice(BluetoothDevice device){
    	if(mConnectThread == null){
        	mConnectThread = new ConnectThread(device);
        	mConnectThread.start();
        }
    }
    public void AcceptConnectDevice(){
    	if(mAcceptThread == null){
    		mAcceptThread = new AcceptThread();
    		mAcceptThread.start();
    	}else{
    		Log.i(TAG,"---mAcceptThread is running ---");
    	}
    }
    
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
     
        public AcceptThread() {
        	Log.i(TAG,"----AcceptThread-----");
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("cneopv6", MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }
     
        public void run() {
        	
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
            	Log.i(TAG,"----AcceptThread wait accept-----");
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                } 
                if(mBluetoothAdapter.isDiscovering()){
            		mBluetoothAdapter.cancelDiscovery();
                }
                // If a connection was accepted
                if (socket != null) {
                	SharedPreferences preferences = getSharedPreferences(PREFS,MODE_WORLD_READABLE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(socket.getRemoteDevice().getAddress(), true);
                    editor.commit();
                    // Do work to manage the connection (in a separate thread)
                	Message message = new Message();   
                    message.what = mesg_connect;
                    Bundle bundle = new Bundle();
                    bundle.putString("Btname",socket.getRemoteDevice().getName());
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                    
                	 mreadDateThread2 = new readDateThread(socket);
                     mreadDateThread2.start();  
                    try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    break;
                }
            }
            Log.i(TAG,"----AcceptThread over!!!-----");
            mAcceptThread =null;
        }
     
        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }
    
    
    /**
     * 连接蓝牙socket的线程
    **/
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket=null;
        private final BluetoothDevice mmDevice;
     
        @SuppressLint("NewApi")
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.i(TAG,"---ConnectThread create!!----");
              
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                if (Build.VERSION.SDK_INT >= 10){
                    tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                }else{
                    tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
                }
            } catch (IOException e) { }
            		mmSocket = tmp;
        }
     
        public void run() {
        	connecting = true;
            connected = false;
            // Cancel discovery because it will slow down the connection
            if(mBluetoothAdapter.isDiscovering()){
            		mBluetoothAdapter.cancelDiscovery();
            }
            
            while (!connected && connetTime <= 1) {                
                connectDevice(mmDevice,mmSocket);
            }
            if(!connected){
            	connetTime=0;   //下次再连接才会继续连接
            	SharedPreferences preferences = getSharedPreferences(PREFS,MODE_WORLD_READABLE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(mmSocket.getRemoteDevice().getAddress(), false);
                editor.commit();
            	Message message = new Message();   
                message.what = mesg_connect_fail;
                mHandler.sendMessage(message);
                try {  
                	mmSocket.close();
                	mmSocket = null;
                } catch (IOException e2) {  
                    // TODO: handle exception  
                    Log.e(TAG, "Cannot close connection when connection failed");  
                }  
            }
       
        }
     
        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
            	if(mmSocket != null){
            		mmSocket.close();
            	}
            } catch (IOException e) { }
        }
    }
    
    @SuppressLint("NewApi")
	private void pairdevices(BluetoothDevice mBluetoothDevice){
    	 // Cancel discovery because it will slow down the connection
        if(mBluetoothAdapter.isDiscovering()){
        		mBluetoothAdapter.cancelDiscovery();
        }
/*        String strpin="0000";      //暂在指环上测试无作用
        mBluetoothDevice.setPin(strpin.getBytes());*/
    	try { 
    	if (mBluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {  
            Method creMethod = BluetoothDevice.class.getMethod("createBond");  
            creMethod.invoke(mBluetoothDevice);  
          
    	}
    	} catch (Exception e) {  
            // TODO: handle exception  
            //DisplayMessage("无法配对！");  
            e.printStackTrace();  
        }  
    	
    }
    
    protected void connectDevice(BluetoothDevice mBluetoothDevice,BluetoothSocket socket ) {  
    	try { 
            socket.connect();  
            
            SharedPreferences preferences = getSharedPreferences(PREFS,MODE_WORLD_READABLE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(socket.getRemoteDevice().getAddress(), true);
            editor.commit();
            
            connected = true;
            Message message = new Message();   
            message.what = mesg_connect;
            Bundle bundle = new Bundle();
            bundle.putString("Btname", mBluetoothDevice.getName());
            message.setData(bundle);
            mHandler.sendMessage(message);
            
            mreadDateThread = new readDateThread(socket);
            mreadDateThread.start();  
            
        } catch (IOException e) {  
            // TODO: handle exception  
           Log.i(TAG,"--------连接失败！,连接尝试次数： "+ connetTime);
            connetTime++;
            connected = false;
           
        } finally {
            connecting = false;
        }  
    }
    
    
    /**
     *  接收发送数据的线程
     * 
    **/
    private class readDateThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
     
        public readDateThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            Log.i(TAG,"----readDateThread----");
     
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
     
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mOutStream=tmpOut;
        }
     
        public void run() {
        	
        	String readdata="";
            int bytes; // bytes returned from read()
            Log.i(TAG,"----readDateThread  run----");
     
            // Keep listening to the InputStream until an exception occurs
            while (true) {
            	byte[] buffertmp = new byte[100]; 
                try {
                	
                    // Read from the InputStream
                    bytes = mmInStream.read(buffertmp);
                    
                    String readMessage = new String(buffertmp, 0, bytes);
                    Log.i(TAG,"readnum:"+bytes+" data:"+readMessage);
                    
                    if(readMessage.endsWith("\r")||readMessage.endsWith("\n")){
                    	readdata=readdata+readMessage;
                    	Log.i(TAG,"mesg:"+readdata.length()+" data:"+readdata);
                    	mHandler.obtainMessage(MESSAGE_READ, readdata.length(), -1, readdata).sendToTarget();
                    	readdata="";
                    }else{
                    	readdata=readdata+readMessage;
                    }
                } catch (IOException e) {
                	Log.e(TAG,"read error!!!!!!");
                	
                	SharedPreferences preferences = getSharedPreferences(PREFS,
                            MODE_WORLD_READABLE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(mmSocket.getRemoteDevice().getAddress(), false);
                    editor.commit();
                	
                	Message message = new Message();   
                    message.what = mesg_disconnect;
                    mHandler.sendMessage(message);
                    break;
                }
            }
            Log.i(TAG,"----readDateThread  run over !!!!----");
            if(mAcceptThread == null){
            	mAcceptThread = new AcceptThread();
            	mAcceptThread.start();
            }
        }
     
        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }     
     
        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
        	mOutStream=null;
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    
    public void sendDate(byte[] bytes){
    	if(mOutStream!=null){
    		try {
				mOutStream.write(bytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    
}
