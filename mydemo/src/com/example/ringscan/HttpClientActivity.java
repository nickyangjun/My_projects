package com.example.ringscan;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
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

public class HttpClientActivity extends Activity {
	
	private static final String DOWNLOAD_FOLDER_NAME = "Nick/HttpClient";
	private static final String DOWNLOAD_FILE_NAME = "cookie.bmp";
	
	private static final int REQUEST_TIMEOUT = 6*1000;//设置请求超时10秒钟
	private static final int SO_TIMEOUT = 5*1000;  //设置等待数据超时时间10秒钟
	
	private Button downloadButton;
	private Button downloadFile;
	private Button downloadFile_post;
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
		downloadFile_post = (Button)findViewById(R.id.download_file_post);
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
            		  HttpGet httpGet = new HttpGet("http://192.168.6.215/ring.php?barcode=12345");
            		  HttpClient httpClient = new DefaultHttpClient();
            		  InputStream inputStream = null;
            		  try {
						HttpResponse httpResponse = httpClient.execute(httpGet);
						HttpEntity httpEntity = httpResponse.getEntity();
						if(httpResponse.getStatusLine().getStatusCode() == 200){
							inputStream = httpEntity.getContent();
							BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
							String result = "";
							String line = "";
							while ((line = reader.readLine()) != null)
							{
								result = result + line;
							}
							Log.i("nick",result);
							handler.sendMessage(handler.obtainMessage(0, result));
						}
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
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
	              		 HttpGet httpGet = new HttpGet("http://192.168.6.215/down.php?args=myphp");
	              		BasicHttpParams httpParams = new BasicHttpParams();
	              	    HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
	              	    HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
	            		 HttpClient httpClient = new DefaultHttpClient(httpParams);
	            		 InputStream inputStream = null;
	            		
							HttpResponse httpResponse;
							try {
								httpResponse = httpClient.execute(httpGet);
								HttpEntity httpEntity = httpResponse.getEntity();
								if(httpResponse.getStatusLine().getStatusCode() == 200){
									
									inputStream = httpEntity.getContent();
									fileSize = httpEntity.getContentLength();
									Log.i("nick","download size:"+fileSize+"  "+getFileSize(fileSize));
									File outfile = new File(folder.toString()+"/"+DOWNLOAD_FILE_NAME);
									 if(!outfile.exists()){//判断文件是否存在  
								            try {  
								            	outfile.createNewFile();  //创建文件  
								                  
								            } catch (IOException e) {  
								                // TODO Auto-generated catch block  
								                e.printStackTrace();  
								            }  
								        }  
									 	readAsFile(inputStream, outfile);
								}
							} catch (ClientProtocolException e) {
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
		
		downloadFile_post.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				downloadContent.setText("");
				 Thread myThread = new Thread(){
	            	  public void run(){
	            		  HttpPost httpPost = new HttpPost("http://192.168.6.215/ring.php");
	            		  List <NameValuePair> params=new ArrayList<NameValuePair>();
	            		  params.add(new BasicNameValuePair("barcode","mev123456"));
	            		  	try{    
	            			     //发出HTTP request
	            		  		httpPost.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
	            			     //取得HTTP response
	            			     HttpResponse httpResponse=new DefaultHttpClient().execute(httpPost);
	            			    
	            			     //若状态码为200 ok
	            			     if(httpResponse.getStatusLine().getStatusCode()==200){
	            			      //取出回应字串
	            			      String strResult=EntityUtils.toString(httpResponse.getEntity());
	            			      Log.i("nick",strResult);
	  							handler.sendMessage(handler.obtainMessage(0, strResult));
	            			     
	            			     }else{
	            			    	 String strResult = "Error Response"+httpResponse.getStatusLine().toString();
	            			  
	            			    	 handler.sendMessage(handler.obtainMessage(0, strResult));
	            			     }
	            			    }catch(ClientProtocolException e){
	            			    	
	            			     e.printStackTrace();
	            			    } catch (UnsupportedEncodingException e) {
	            			    	
	            			     e.printStackTrace();
	            			    } catch (IOException e) {
	            			    
	            			     e.printStackTrace();
	            			    }
	            	  }
				 };
				
				 myThread.start();
				
			}
			
		});
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
