package com.example.ringscan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
//import android.os.INetworkManagementService;

public class MainActivity extends Activity {

	private boolean install_allapk = false;

	public static String updating_apk_version;// = "1";

	// public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

	ArrayList<String> strArray_homepkg;

	String INTERCEPT_HOME_KEYS_ENABLE = "INTERCEPT_HOME_KEYS_ENABLE";
	String ACTION_INTERCEPT_HOME_KEYS = "ACTION_INTERCEPT_HOME_KEYS";

	private Context mContext;

	String filepath = "/data/apkins/package_ins_cfg";
	InputMethodManager mImm;

	private EditText edittext;
	boolean isrunning = true;

	ListView mainlist;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED,FLAG_HOMEKEY_DISPATCHED);
		setContentView(R.layout.activity_main);

//		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//					.detectCustomSlowCalls()
//					.detectDiskReads()
//					.detectDiskWrites()
//					.penaltyLog()
//					.penaltyDialog()
//					.build());
//		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//					.detectAll()
//					.penaltyLog()
//					.build());
//		 CommandResult result = ShellUtils.execCommand("rm -rf /data/aee_exp",
//		 true,true);
//		 Log.d("nick","result:"+result.result);
//		 Log.d("nick","successMsg:"+result.successMsg);
//		 Log.d("nick","errorMsg:"+result.errorMsg);
//		 if(RootCmd.haveRoot()){
//		 RootCmd.execRootCmd("ls");
//		 RootCmd.execRootCmd("rm -rf /data/aee_exp");
//		 }
		// 绑定Layout里面的ListView
		mainlist = (ListView) findViewById(R.id.mainlist_content);

		mContext = this;
		Message mMessage = new Message();
		mMessage.what = 0;

		// 生成动态数组，加入数据
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		String[] ActivityTitle = { "DownloadManagerActivity", "DBtestActivity",
				"HttpURLConnectionActivity", "HttpClientActivity",
				"SaveInstanceStateActivity", "ShellCmdActivity"};
		for (int i = 0; i < ActivityTitle.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemImage", R.drawable.ic_launcher);// 图像资源的ID
			map.put("ItemTitle", "Pore " + i);
			map.put("ItemText", ActivityTitle[i]);
			listItem.add(map);
		}
		
		

		// 生成适配器的Item和动态数组对应的元素
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,// 数据源
				R.layout.mainlist_items,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "ItemImage", "ItemTitle", "ItemText" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.ItemImage, R.id.ItemTitle, R.id.ItemText });

		// 添加并且显示
		mainlist.setAdapter(listItemAdapter);

		// 添加点击
		mainlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				HashMap<String, Object> map = (HashMap<String, Object>) arg0
						.getItemAtPosition(arg2);
				setTitle(map.get("ItemText").toString());
				Intent intent = new Intent();
				intent.setAction("example.ringscan."
						+ map.get("ItemText").toString());
				startActivity(intent);
			}
		});

	}

	private class clearThreak extends Thread {

		public void run() {

			while (isrunning) {

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (edittext.getText().length() > 200) {
					Message message = new Message();
					message.what = 1;
					MainActivity.this.mHandler.sendMessage(message);
				}

			}
			Log.i("nick", "---------clear thread over!!!!!!!");
			
		}
	}

	/**
	 * 鏇存柊UI
	 **/
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				edittext.setText(" ");

				break;
			}
		}
	};

	/**
	 * 鏍规嵁璺緞鍒犻櫎鎸囧畾鐨勭洰褰曟垨鏂囦欢锛屾棤璁哄瓨鍦ㄤ笌鍚�
	 * 
	 * @param sPath
	 *            瑕佸垹闄ょ殑鐩綍鎴栨枃浠�
	 * @return 鍒犻櫎鎴愬姛杩斿洖 true锛屽惁鍒欒繑鍥�false銆�
	 */
	public boolean DeleteFolder(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 鍒ゆ柇鐩綍鎴栨枃浠舵槸鍚﹀瓨鍦�
		if (!file.exists()) { // 涓嶅瓨鍦ㄨ繑鍥�false
			return flag;
		} else {
			// 鍒ゆ柇鏄惁涓烘枃浠�
			if (file.isFile()) { // 涓烘枃浠舵椂璋冪敤鍒犻櫎鏂囦欢鏂规硶
				return deleteFile(sPath);
			} else { // 涓虹洰褰曟椂璋冪敤鍒犻櫎鐩綍鏂规硶
				return deleteDirectory(sPath);
			}
		}
	}

	/**
	 * 鍒犻櫎鍗曚釜鏂囦欢
	 * 
	 * @param sPath
	 *            琚垹闄ゆ枃浠剁殑鏂囦欢鍚�
	 * @return 鍗曚釜鏂囦欢鍒犻櫎鎴愬姛杩斿洖true锛屽惁鍒欒繑鍥瀎alse
	 */
	public boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 璺緞涓烘枃浠朵笖涓嶄负绌哄垯杩涜鍒犻櫎
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 鍒犻櫎鐩綍锛堟枃浠跺す锛変互鍙婄洰褰曚笅鐨勬枃浠�
	 * 
	 * @param sPath
	 *            琚垹闄ょ洰褰曠殑鏂囦欢璺緞
	 * @return 鐩綍鍒犻櫎鎴愬姛杩斿洖true锛屽惁鍒欒繑鍥瀎alse
	 */
	public boolean deleteDirectory(String sPath) {
		boolean flag;
		// 濡傛灉sPath涓嶄互鏂囦欢鍒嗛殧绗︾粨灏撅紝鑷姩娣诲姞鏂囦欢鍒嗛殧绗�
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 濡傛灉dir瀵瑰簲鐨勬枃浠朵笉瀛樺湪锛屾垨鑰呬笉鏄竴涓洰褰曪紝鍒欓�鍑�
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		// 鍒犻櫎鏂囦欢澶逛笅鐨勬墍鏈夋枃浠�鍖呮嫭瀛愮洰褰�
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 鍒犻櫎瀛愭枃浠�
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 鍒犻櫎瀛愮洰褰�
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 鍒犻櫎褰撳墠鐩綍
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		Log.i("nick", "------keyCode-----" + keyCode);
		// Log.d("nick",Log.getStackTraceString(new Throwable()));
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_MUTE:
		case KeyEvent.KEYCODE_CTRL_LEFT:
		case KeyEvent.KEYCODE_DPAD_CENTER:

			if (event.getRepeatCount() == 0) {
				// getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				/*
				 * mImm = (InputMethodManager) getSystemService(
				 * Context.INPUT_METHOD_SERVICE);
				 * 
				 * mImm.hideSoftInputFromWindow(getWindowToken(), 0);
				 */
				// this.myHandler.sendMessage(message);

			}

			return true;
		case KeyEvent.KEYCODE_HOME:
			Toast.makeText(this, "---home---", 1).show();
			return true;

		case KeyEvent.KEYCODE_F8:
			Toast.makeText(this, "---f8---", 1).show();
			return true;
		}

		return super.onKeyDown(keyCode, event);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intentSend = new Intent();
		intentSend.setAction(ACTION_INTERCEPT_HOME_KEYS);
		intentSend.putExtra(INTERCEPT_HOME_KEYS_ENABLE, false);
		this.sendBroadcast(intentSend);

		isrunning = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
