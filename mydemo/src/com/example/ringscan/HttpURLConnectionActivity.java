package com.example.ringscan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HttpURLConnectionActivity extends Activity {

	private static final String DOWNLOAD_FOLDER_NAME = "Nick/HttpURLConnection";
	private static final String DOWNLOAD_FILE_NAME = "cookie.bmp";

	private Button downloadButton;
	private Button downloadFile;
	private ProgressBar downloadProgress;
	private TextView downloadTip;
	private TextView downloadSize;
	private TextView downloadPrecent;
	private TextView downloadContent;
	private Button downloadCancel;
	 private MyHandler handler;
	  long fileSize = 0;
	 long totalSize = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.httpurlconnection);

		initView();
		initData();
		handler = new MyHandler();
	}
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                	downloadContent.setText((String)msg.obj);
                	break;
                case 1:
                	downloadProgress.setVisibility(View.VISIBLE);
                	downloadSize.setVisibility(View.VISIBLE);
                    downloadPrecent.setVisibility(View.VISIBLE);
                	downloadProgress.setMax(100);
                	downloadProgress.setProgress(0);
                	break;
                case 2:
                	
                	 totalSize += msg.arg1;
                	 float pro = ((float)totalSize/fileSize)*100;
                	 downloadSize.setText(getFileSize(totalSize)+"/"+getFileSize(fileSize));
                	 downloadPrecent.setText((int)pro+"%");
                	 if(fileSize ==totalSize){
                         downloadProgress.setProgress(100);
                         downloadFile.setClickable(true);
                         downloadProgress.setVisibility(View.GONE);
                         downloadSize.setVisibility(View.GONE);
                         downloadPrecent.setVisibility(View.GONE);
                         totalSize=0;
                         downloadSize.setText("");
                    	 downloadPrecent.setText("");
                	 }else{
                		
                		
                		 downloadProgress.setProgress((int)pro);
                	 }
                	break;
                	
            }
        }
    }

	private void initView() {
		downloadButton = (Button) findViewById(R.id.download_button);
		downloadFile = (Button) findViewById(R.id.download_file);
		downloadCancel = (Button) findViewById(R.id.download_cancel);
		downloadProgress = (ProgressBar) findViewById(R.id.download_progress);
		downloadTip = (TextView) findViewById(R.id.download_tip);
		downloadTip.setText(getString(R.string.tip_download_file)
						+ Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME));
		downloadSize = (TextView) findViewById(R.id.download_size);
		downloadPrecent = (TextView) findViewById(R.id.download_precent);
		downloadContent = (TextView) findViewById(R.id.download_content);
	}

	private void initData() {
		downloadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				downloadContent.setText("");
				// TODO Auto-generated method stub
				// 示例代码：通过Post方式发送请求
              Thread myThread = new Thread(){
            	  public void run(){
            		  URL url;
      				try {
      					//url = new URL("http://www.baidu.com");
      					url = new URL("http://192.168.6.215");

      					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      					conn.setConnectTimeout(6 * 1000);// 设置连接超时
      					if (conn.getResponseCode() != 200)
      						throw new RuntimeException("请求url失败");
      					InputStream is = conn.getInputStream();//得到网络返回的输入流  
      					String result = readData(is, "GBK");  
      					conn.disconnect();  
      					System.out.println(result);
      					Log.i("nick",result);
      					handler.sendMessage(handler.obtainMessage(0, result));

      				} catch (MalformedURLException e) {
      					// TODO Auto-generated catch block
      					e.printStackTrace();
      				} catch (IOException e) {
      					// TODO Auto-generated catch block
      					e.printStackTrace();
      				} catch (Exception e) {
      					// TODO Auto-generated catch block
      					e.printStackTrace();
      				}
            		  
            	  }
            	   
               };              
               myThread.start();
			}
		});
		
		
		downloadFile.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				  final File folder = Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
	                if (!folder.exists() || !folder.isDirectory()) {
	                    folder.mkdirs();
	                }
	                Thread myThread = new Thread(){
	              	  public void run(){
	                
	            try {  
	          
				// TODO Auto-generated method stub
				//URL url = new URL("http://farm4.staticflickr.com/3755/9148527824_6c156185ea.jpg");
				URL url = new URL("http://192.168.6.215/down.php?args=myphp");
				//URL url = new URL("http://192.168.6.215/down/img/cookie.bmp");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
				conn.setConnectTimeout(6* 1000);  
				conn.setReadTimeout(5*1000);
				if (conn.getResponseCode() != 200){   
				    throw new RuntimeException("请求url失败");  
				}else{
					fileSize = conn.getContentLength();
					Log.i("nick","download size:"+fileSize+"  "+getFileSize(fileSize));
				}
				InputStream is = conn.getInputStream();  
				File outfile = new File(folder.toString()+"/"+DOWNLOAD_FILE_NAME);
				 if(!outfile.exists()){//判断文件是否存在  
			            try {  
			            	outfile.createNewFile();  //创建文件  
			                  
			            } catch (IOException e) {  
			                // TODO Auto-generated catch block  
			                e.printStackTrace();  
			            }  
			        }  
				 	readAsFile(is, outfile);
				 	conn.disconnect();
	          	} catch (MalformedURLException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  					Log.e("nick",e.toString());
  				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("nick",e.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("nick",e.toString());
				} 
			}
	                };
	                
	                myThread.start();  
	                downloadFile.setClickable(false);
	            	handler.sendMessage(handler.obtainMessage(1, null));
	                
			}
			
		});
	}
	//第一个参数为输入流,第二个参数为字符集编码  
	public static String readData(InputStream inSream, String charsetName) throws Exception{  
	       ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
	       byte[] buffer = new byte[1024];  
	       int len = -1;  
	       while( (len = inSream.read(buffer)) != -1 ){  
	           outStream.write(buffer, 0, len);  
	       }  
	       byte[] data = outStream.toByteArray();  
	       outStream.close();  
	       inSream.close();  
	       return new String(data, charsetName);  
	   }  
	
	public  void readAsFile(InputStream inSream, File file) throws Exception{  
	    FileOutputStream outStream = new FileOutputStream(file);  
	    byte[] buffer = new byte[1024];  
	    int len = -1;  
	    while( (len = inSream.read(buffer)) != -1 ){  
	        outStream.write(buffer, 0, len); 
	        updateView(len);
	    }  
	     outStream.close();  
	    inSream.close();  
	} 
	
	   public void updateView(int len) {
		  
	        handler.sendMessage(handler.obtainMessage(2,len,0,null));
	    }
	
	  static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

	    public static final int    MB_2_BYTE             = 1024 * 1024;
	    public static final int    KB_2_BYTE             = 1024;

	    /**
	     * @param size
	     * @return
	     */
	    public static CharSequence getFileSize(long size) {
	        if (size <= 0) {
	            return "0M";
	        }

	        if (size >= MB_2_BYTE) {
	            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / MB_2_BYTE)).append("M");
	        } else if (size >= KB_2_BYTE) {
	            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / KB_2_BYTE)).append("K");
	        } else {
	            return size + "B";
	        }
	    }
	    
	   
}


