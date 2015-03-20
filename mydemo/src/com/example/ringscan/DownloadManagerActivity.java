package com.example.ringscan;

import java.io.File;
import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadManagerActivity extends Activity{
	 public static final Uri    CONTENT_URI = Uri.parse("content://downloads/my_downloads");
	 public static final String     DOWNLOAD_FOLDER_NAME = "Nick/DownloadManager";
	 public static final String     DOWNLOAD_FILE_NAME   = "cookie.bmp";
     public static final String     APK_URL              = "http://192.168.6.215/down.php?args=myphp";
     //public static final String     APK_URL              = "http://farm4.staticflickr.com/3755/9148527824_6c156185ea.jpg";
     private DownloadManager        downloadManager;
	 private MyHandler              handler;
	 private DownloadChangeObserver downloadObserver;
	 private CompleteReceiver       completeReceiver;
	 private long                   downloadId           = 0;
	 
	  private Button                 downloadButton;
	  private ProgressBar            downloadProgress;
	  private TextView               downloadTip;
	  private TextView               downloadSize;
	  private TextView               downloadPrecent;
	  private Button                 downloadCancel;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_manager_demo);

        handler = new MyHandler();
        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        initView();
        initData();

        downloadObserver = new DownloadChangeObserver();
        completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}
	
	  @Override
	    protected void onResume() {
	        super.onResume();
	        /** observer download change **/
	        getContentResolver().registerContentObserver(CONTENT_URI, true, downloadObserver);
	        updateView();
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        getContentResolver().unregisterContentObserver(downloadObserver);
	    }

	    @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        unregisterReceiver(completeReceiver);
	    }

	    private void initView() {
	        downloadButton = (Button)findViewById(R.id.download_button);
	        downloadCancel = (Button)findViewById(R.id.download_cancel);
	        downloadProgress = (ProgressBar)findViewById(R.id.download_progress);
	        downloadTip = (TextView)findViewById(R.id.download_tip);
	        downloadTip.setText(getString(R.string.tip_download_file)
	                + Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME));
	        downloadSize = (TextView)findViewById(R.id.download_size);
	        downloadPrecent = (TextView)findViewById(R.id.download_precent);
	    }

	    @SuppressLint("NewApi")
		private void initData() {
	        /**
	         * get download id from preferences.<br/>
	         * if download id bigger than 0, means it has been downloaded, then query status and show right text;
	         */
	        updateView();
	        downloadButton.setOnClickListener(new OnClickListener() {

				@Override
	            public void onClick(View v) {
	                File folder = Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
	                if (!folder.exists() || !folder.isDirectory()) {
	                    folder.mkdirs();
	                }

	                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(APK_URL));
	                request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME, DOWNLOAD_FILE_NAME);
	                request.setTitle(getString(R.string.download_notification_title));
	                request.setDescription("meilishuo desc");
	                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
	                request.setVisibleInDownloadsUi(false);
	                // request.allowScanningByMediaScanner();
	                //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
	                // request.setShowRunningNotification(false);
	                // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
	                request.setMimeType("application/cn.trinea.download.file");
	                downloadId = downloadManager.enqueue(request);
	                /** save download id to preferences **/
	               
	                updateView();
	            }
	        });
	        downloadCancel.setOnClickListener(new OnClickListener() {

	            @SuppressLint("NewApi")
				@Override
	            public void onClick(View v) {
	                downloadManager.remove(downloadId);
	                updateView();
	            }
	        });
	    }
	    
	    class DownloadChangeObserver extends ContentObserver {

	        public DownloadChangeObserver() {
	            super(handler);
	        }

	        @Override
	        public void onChange(boolean selfChange) {
	            updateView();
	        }

	    }

	    class CompleteReceiver extends BroadcastReceiver {

	        @Override
	        public void onReceive(Context context, Intent intent) {
	            /**
	             * get the id of download which have download success, if the id is my id and it's status is successful,
	             * then install it
	             **/
	            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
	            if (completeDownloadId == downloadId) {
	                initData();
	                updateView();
	                // if download successful, install apk
	                if (getStatusById(downloadId) == DownloadManager.STATUS_SUCCESSFUL) {
	                    String apkFilePath = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath())
	                            .append(File.separator).append(DOWNLOAD_FOLDER_NAME).append(File.separator)
	                            .append(DOWNLOAD_FILE_NAME).toString();
	                   Toast.makeText(getApplicationContext(), "download sucesses!!!", 1).show();
	                }
	            }
	        }
	    };
	    public int getStatusById(long downloadId) {
	        return getInt(downloadId, DownloadManager.COLUMN_STATUS);
	    }
	    /**
	     * get int column
	     * 
	     * @param downloadId
	     * @param columnName
	     * @return
	     */
	    @SuppressLint("NewApi")
		private int getInt(long downloadId, String columnName) {
	        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
	        int result = -1;
	        Cursor c = null;
	        try {
	            c = downloadManager.query(query);
	            if (c != null && c.moveToFirst()) {
	                result = c.getInt(c.getColumnIndex(columnName));
	            }
	        } finally {
	            if (c != null) {
	                c.close();
	            }
	        }
	        return result;
	    }
	    @SuppressLint("NewApi")
		public int[] getBytesAndStatus(long downloadId) {
	        int[] bytesAndStatus = new int[] {-1, -1, 0};
	        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
	        Cursor c = null;
	        try {
	            c = downloadManager.query(query);
	            if (c != null && c.moveToFirst()) {
	                bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
	                bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
	                bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
	            }
	        } finally {
	            if (c != null) {
	                c.close();
	            }
	        }
	        return bytesAndStatus;
	    }
	    public void updateView() {
	        int[] bytesAndStatus = getBytesAndStatus(downloadId);
	        handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
	    }

	    /**
	     * MyHandler
	     * 
	     * @author Trinea 2012-12-18
	     */
	    private class MyHandler extends Handler {

	        @Override
	        public void handleMessage(Message msg) {
	            super.handleMessage(msg);

	            switch (msg.what) {
	                case 0:
	                    int status = (Integer)msg.obj;
	                    if (isDownloading(status)) {
	                        downloadProgress.setVisibility(View.VISIBLE);
	                        downloadProgress.setMax(0);
	                        downloadProgress.setProgress(0);
	                        downloadButton.setVisibility(View.GONE);
	                        downloadSize.setVisibility(View.VISIBLE);
	                        downloadPrecent.setVisibility(View.VISIBLE);
	                        downloadCancel.setVisibility(View.VISIBLE);

	                        if (msg.arg2 < 0) {
	                            downloadProgress.setIndeterminate(true);
	                            downloadPrecent.setText("0%");
	                            downloadSize.setText("0M/0M");
	                        } else {
	                            downloadProgress.setIndeterminate(false);
	                            downloadProgress.setMax(msg.arg2);
	                            downloadProgress.setProgress(msg.arg1);
	                            downloadPrecent.setText(getNotiPercent(msg.arg1, msg.arg2));
	                            downloadSize.setText(getAppSize(msg.arg1) + "/" + getAppSize(msg.arg2));
	                        }
	                    } else {
	                        downloadProgress.setVisibility(View.GONE);
	                        downloadProgress.setMax(0);
	                        downloadProgress.setProgress(0);
	                        downloadButton.setVisibility(View.VISIBLE);
	                        downloadSize.setVisibility(View.GONE);
	                        downloadPrecent.setVisibility(View.GONE);
	                        downloadCancel.setVisibility(View.GONE);

	                        if (status == DownloadManager.STATUS_FAILED) {
	                            downloadButton.setText(getString(R.string.app_status_download_fail));
	                        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
	                            downloadButton.setText(getString(R.string.app_status_downloaded));
	                        } else {
	                            downloadButton.setText(getString(R.string.app_status_download));
	                        }
	                    }
	                    break;
	            }
	        }
	    }

	    static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

	    public static final int    MB_2_BYTE             = 1024 * 1024;
	    public static final int    KB_2_BYTE             = 1024;

	    /**
	     * @param size
	     * @return
	     */
	    public static CharSequence getAppSize(long size) {
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

	    public static String getNotiPercent(long progress, long max) {
	        int rate = 0;
	        if (progress <= 0 || max <= 0) {
	            rate = 0;
	        } else if (progress > max) {
	            rate = 100;
	        } else {
	            rate = (int)((double)progress / max * 100);
	        }
	        return new StringBuilder(16).append(rate).append("%").toString();
	    }

	    public static boolean isDownloading(int downloadManagerStatus) {
	        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
	                || downloadManagerStatus == DownloadManager.STATUS_PAUSED
	                || downloadManagerStatus == DownloadManager.STATUS_PENDING;
	    }

}
