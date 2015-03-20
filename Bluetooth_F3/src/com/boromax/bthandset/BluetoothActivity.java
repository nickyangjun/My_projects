
package com.boromax.bthandset;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.boromax.bthandset.BluetoothService.onDataReceived;

/*
 *   测试发现蓝牙需要有server端等待连接，因为指环开机后会主动连接android端
 *   如果缺少sever端，android机主动去连接指环的话，就必须每次重新配对，不然
 *   连接上里也收不到数据。暂不明原因。
 *   
 */
public class BluetoothActivity extends Activity {
    
    private static final String TAG = "nick";
    
    private Button btvisible,btsearch,btclear,btsend;
    private ListView btnewlist,bpairlist;
    private static EditText btreceive;
    private ToggleButton btonoff;
    private TextView connectdev,senddev;
    private ArrayAdapter<String> mArrayAdapter,mPairDevices;
     
    private List<String> lstDevices = new ArrayList<String>();  
    private List<String> lstPairDev = new ArrayList<String>();
 

    private Context mContext;
    
    private BluetoothService mBluetoothService;
    
    //蓝牙SPP的uuid
    //String macAddr = "00:1F:B7:03:E6:92";
    String btMac;
    final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    						
    UUID MY_UUID = UUID.fromString(SPP_UUID); 

    BluetoothAdapter mBluetoothAdapter;
    
    private String PREFS = "bluetoothlauncher";
    
    SharedPreferences preferences;
   
    
    private BluetoothHeadset mBluetoothHeadset;
    BluetoothProfile.ServiceListener mProfileListener;
    Intent serviceIntent;
    BluetoothEventManager mBluetoothEventManager;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetoothspp);      
        mContext=this; 
        
        serviceIntent = new Intent();
		serviceIntent.setClass(BluetoothActivity.this, BluetoothService.class);
        startService(serviceIntent);
        
        Intent btServiceIntent = new Intent(BluetoothActivity.this,BluetoothService.class);  
        bindService(btServiceIntent, conn, Context.BIND_AUTO_CREATE); 
        
        initWidget();
        preferences = getSharedPreferences(PREFS,MODE_WORLD_READABLE); 
        SharedPreferences.Editor editor = preferences.edit();
        //editor.clear();
        //editor.commit();
        
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        
        //Get the BluetoothAdapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            System.out.print("No BlueTooth Device!");
            this.finish();
        }else{
            if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF)
            {
                btonoff.setChecked(true);         
            }else if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){      
                btonoff.setChecked(false);
                setPairedDevices();
                if(mBluetoothAdapter.enable()){
                    mBluetoothAdapter.startDiscovery();
                }
               
            }     
        }        
 
        //绑定Service  
       
        
        mBluetoothEventManager = new BluetoothEventManager(mContext);
        mBluetoothEventManager.addHandler(BluetoothDevice.ACTION_ACL_CONNECTED, new BluetoothEventManager.Handler(){

			@Override
			public void onReceive(Context context, Intent intent,
					BluetoothDevice device) {
				// TODO Auto-generated method stub
				 Log.i(TAG,"setText connected bt name: "+device.getName());
	             connectdev.setText(getString(R.string.connectDev)+device.getName());
	             SharedPreferences.Editor editor = preferences.edit();
	             editor.putString("connected",device.getName());
	             editor.commit();
			}
        	
        });
        mBluetoothEventManager.addHandler(BluetoothDevice.ACTION_ACL_DISCONNECTED, new BluetoothEventManager.Handler(){

			@Override
			public void onReceive(Context context, Intent intent,
					BluetoothDevice device) {
				// TODO Auto-generated method stub
				 Log.i(TAG,"setText disconnect bt name: "+device.getName());
	             connectdev.setText(getString(R.string.connectDev));
	             SharedPreferences.Editor editor = preferences.edit();
	             editor.remove("connected");
	             editor.commit();
			}
        	
        });
        mBluetoothEventManager.addHandlerEnd();
        
    }
    
    ServiceConnection conn = new ServiceConnection() {  
        
    	@Override  
        public void onServiceDisconnected(ComponentName name) {  
              
        }  
          
        @Override  
        public void onServiceConnected(ComponentName name, IBinder service) {  
            //返回一个MsgService对象  
        	Log.i(TAG,"--onServiceConnected--");
        	mBluetoothService = ((BluetoothService.ConnectBinder)service).getService();  
            
        	mBluetoothService.setonDataReceived(new onDataReceived(){

				@Override
				public void onReceived(String buff) {
					Log.i(TAG,"onRec: "+buff);
					// TODO Auto-generated method stub
					 btreceive.setText(btreceive.getText()+buff);
	                    
                     CharSequence text = btreceive.getText();    //设置光标在文件尾    
                    if (text instanceof Spannable) {
                        Spannable spanText = (Spannable)text;
                         Selection.setSelection(spanText, text.length());
                    }
				}
        		
        	});
        }  
    };  
    
    private void setPairedDevices(){
    	
    	//Querying paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        mPairDevices.clear();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
            	 //防止重复添加
                String str = device.getName() + "\n" + device.getAddress(); 
                Log.i(TAG,"paired device: "+str);
                if (lstPairDev.indexOf(str) == -1){
                	mPairDevices.add(device.getName() + "\n" + device.getAddress());
                    //将以配对的蓝牙设备的MAC地址保存到xml
                    SharedPreferences.Editor editor2 = preferences.edit();
                    editor2.putBoolean(device.getAddress()+"paired",true);
                    editor2.commit();
                }
               
            }
       
        }
        mPairDevices.notifyDataSetChanged();
    }
    
    
    public void initWidget(){
        btreceive = (EditText) findViewById(R.id.bt_receive);
        btvisible = (Button)findViewById(R.id.bt_visible);
        btsearch = (Button)findViewById(R.id.bt_search); 
        btonoff = (ToggleButton)findViewById(R.id.bt_onoff);
        btclear = (Button)findViewById(R.id.bt_clear);
        btsend = (Button)findViewById(R.id.bt_write);
        senddev = (TextView)findViewById(R.id.et_writezone);
        connectdev = (TextView)findViewById(R.id.title_connect);
        
        
        btreceive.setKeyListener(null);            
        btvisible.setOnClickListener(new ClickEvent());                 
        btsearch.setOnClickListener(new ClickEvent());       
        btonoff.setOnClickListener(new ClickEvent()); 
        btclear.setOnClickListener(new ClickEvent());
        btsend.setOnClickListener(new ClickEvent());
        
        bpairlist = (ListView)findViewById(R.id.bt_pair_dev);
        mPairDevices = new ArrayAdapter<String>(BluetoothActivity.this,
                android.R.layout.simple_list_item_1, lstPairDev);
        bpairlist.setAdapter(mPairDevices);
        bpairlist.setOnItemClickListener(new pairItemClickEvent());
        
        bpairlist.setOnItemLongClickListener(new pairItemLongClikEvent());
        
        btnewlist = (ListView)findViewById(R.id.bt_dev_list);
        mArrayAdapter = new ArrayAdapter<String>(BluetoothActivity.this,
                android.R.layout.simple_list_item_1, lstDevices);
        btnewlist.setAdapter(mArrayAdapter);
        btnewlist.setOnItemClickListener(new newdevItemClickEvent()); 
        
   
    }

    public boolean isDevicePaired(String MacAddr){
    	Log.i(TAG,"--Paired"+ MacAddr+":"+preferences.getBoolean(MacAddr+"paired", false));
    	return preferences.getBoolean(MacAddr+"paired", false);
    }
    
    /**
     *  发现设备的广播监听 
     **/
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

       
		public void onReceive(Context context, Intent intent) {         
            
            String action = intent.getAction();
            //mArrayAdapter.clear(); 
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView                           
                //防止重复添加
                String str = device.getName() + "\n" + device.getAddress(); 
                if (lstDevices.indexOf(str) == -1 && !isDevicePaired(device.getAddress()))
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mArrayAdapter.notifyDataSetChanged();
            }else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
            	Log.i(TAG,"-----ACTION_ACL_CONNECTED----");	
            }else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
            	Log.i(TAG,"-----ACTION_ACL_DISCONNECTED----");	
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
            	Log.i(TAG,"-----ACTION_BOND_STATE_CHANGED----");
            	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            	String str = device.getName() + "\n" + device.getAddress();
            	 switch (device.getBondState()) { 
                 case BluetoothDevice.BOND_BONDING: 
                     Log.d(TAG, "正在配对......"); 
                     break; 
                 case BluetoothDevice.BOND_BONDED: 
                     Log.d(TAG, "完成配对");
                     setPairedDevices();
                    // Log.d(TAG,"----"+device.getUuids().toString());
                     if(lstDevices.indexOf(str) != -1){   //delete the paired device
                    	 mArrayAdapter.remove(str); 
                    	 mArrayAdapter.notifyDataSetChanged();
                     }
                     break; 
                 case BluetoothDevice.BOND_NONE: 
                     Log.d(TAG, "取消配对："+device.getAddress()); 
                     setPairedDevices();
                     SharedPreferences.Editor editor = preferences.edit();
                     editor.putBoolean(device.getAddress()+"paired",false);
                     editor.commit();
                              
                    
                 default: 
                     break; 
                 } 
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                setTitle("搜索完成"); 
                btsearch.setText("搜索完成");
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                setTitle("正在搜索..."); 
                btsearch.setText("搜索...");
            }
            
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
            	Log.i(TAG,"-----ACTION_STATE_CHANGED----");
            }
        }
    };


    /**
     *  按键处理
     * */
    private class ClickEvent implements View.OnClickListener {

        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            
            switch(v.getId()){
                case R.id.bt_onoff: //Enable Bluetooth
                    if (btonoff.isChecked() == false)                
                    {
                        //打开方式一，有提示框
                       Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                       startActivityForResult(enableBtIntent, 1);
                        //打开方式二，直接打开
                       // mBluetoothAdapter.enable();
                    }else{
                        mBluetoothAdapter.disable();
                        mPairDevices.clear();    //清空数据
                        mArrayAdapter.clear(); 
                    }
                    break;
     
                case R.id.bt_visible: //Enabling discoverability
                    Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                    break;
     
                case R.id.bt_search: //Discovering devices
                    if(mBluetoothAdapter.enable()){
                    	if(!mBluetoothAdapter.isDiscovering()){
                    		mArrayAdapter.clear(); 
                    		mBluetoothAdapter.startDiscovery();
                    	}else{
                    		mBluetoothAdapter.cancelDiscovery();
                    		mArrayAdapter.clear(); 
                    		mBluetoothAdapter.startDiscovery();
                    	}
                    }else{
                        mBluetoothAdapter.cancelDiscovery();
                        btsearch.setText("搜索设备");
                    }
                    break; 
                case R.id.bt_clear:
                	btreceive.setText("");
                    break;
                case R.id.bt_write:
                
                	if(mBluetoothService != null && senddev.getText().length()>0){
                		Log.i(TAG,"SEND:"+senddev.getText().toString().getBytes());
                		mBluetoothService.sendDate(senddev.getText().toString().getBytes());
                	}   	
                	break;
                    
                default :
                    break;
                
            }
            
        }
     
    }

    @Override 
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
        // TODO Auto-generated method stub  
        super.onActivityResult(requestCode, resultCode, data); 
        if (requestCode == 1) { 
            if (resultCode == RESULT_OK) { 
                Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
                setPairedDevices();
                mBluetoothService.AcceptConnectDevice();
            } else if (resultCode == RESULT_CANCELED) { 
                Toast.makeText(this, "不允许蓝牙开启", Toast.LENGTH_SHORT).show(); 
                finish(); 
            } 
        } 
 
    } 
    class pairItemLongClikEvent implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int position, long arg3) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(), "取消"+lstPairDev.get(position)+"设备配对",    
                    Toast.LENGTH_SHORT).show();
			//获取每个条目的MAC地址         
            String[] listItem = lstPairDev.get(position).split("\\n");
            btMac = listItem[1];
            BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(btMac); 
            int state = bluetoothDevice.getBondState();
           
            if (state == BluetoothDevice.BOND_BONDING) {
            	
				try {
					Method creMethod;	
					creMethod = BluetoothDevice.class.getMethod("cancelBondProcess");
					creMethod.invoke(bluetoothDevice);
		        } catch (Exception e) {  
		            // TODO: handle exception  
		            
		            e.printStackTrace();  
		        }
            }

            if (state != BluetoothDevice.BOND_NONE) {
                final BluetoothDevice dev = bluetoothDevice;
                if (dev != null) {
                
                	try {
                	 Method pairMethod = BluetoothDevice.class.getMethod("removeBond");
                	 pairMethod.invoke(bluetoothDevice); 
                	} catch (Exception e) {  
    		            // TODO: handle exception  
    		            //DisplayMessage("无法取消配对！");  
    		            e.printStackTrace();  
    		        }
                
                }
            }           
			return true;
		}
    
    }
    
    class pairItemClickEvent implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
           
        	 //获取每个条目的MAC地址         
            String[] listItem = lstPairDev.get(position).split("\\n");
            btMac = listItem[1];
            
            SharedPreferences preferences = getSharedPreferences(PREFS,MODE_WORLD_READABLE);           
            if(!preferences.getBoolean(btMac, false)){ //如果这个地址没有被链接
            	Toast.makeText(getApplicationContext(), "开始连接 "+listItem[0]+"设备",    
                         Toast.LENGTH_SHORT).show();
            	//启动蓝牙SPP的连接线程
            	BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(btMac);
            	/*if(mConnectThread == null){
            	mConnectThread = new ConnectThread(bluetoothDevice);
            	mConnectThread.start();
            	}*/
            	if(mBluetoothService != null){
            		mBluetoothService.ConnectDevice(bluetoothDevice);
            	}
            }else{
            	Toast.makeText(getApplicationContext(), "此设备已连接,长按可以取消配对连接！", 1).show();
            }
           
        }
    
    }
    class newdevItemClickEvent implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
           
          
            //获取每个条目的MAC地址         
            String[] listItem = lstDevices.get(position).split("\\n");
            btMac = listItem[1];
           
            SharedPreferences preferences = getSharedPreferences(PREFS, MODE_WORLD_READABLE);
            if(!preferences.getBoolean(btMac, false)){ //如果这个地址没有被链接
            	Toast.makeText(getApplicationContext(), "开始配对连接 "+lstDevices.get(position)+"设备",    
                         Toast.LENGTH_SHORT).show();
                 
            	//启动蓝牙SPP的连接线程
            	BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(btMac);                         
            	pairdevices(bluetoothDevice);
            
            }else{
            	Toast.makeText(getApplicationContext(), "此设备已连接！", 1).show();
            }
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
    
    
    @Override
    public void finish() {
    	Log.i(TAG,"-------finish---------");
        // TODO Auto-generated method stub
        unregisterReceiver(mReceiver);
      
        if(mBluetoothAdapter.isDiscovering()){
        	mBluetoothAdapter.cancelDiscovery();
        }
        
        super.finish();
    }
    @Override
    protected void onResume(){
    	super.onResume();
    	connectdev.setText(getString(R.string.connectDev)+preferences.getString("connected",""));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"-------onDestroy---------");
       // stopService(serviceIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return true;
    }
    

}
