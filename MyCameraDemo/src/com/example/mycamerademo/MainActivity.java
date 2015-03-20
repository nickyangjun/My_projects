package com.example.mycamerademo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements 
CameraPreview.OnCameraStatusListener {
	
	public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; 
    public static final String PATH = Environment.getExternalStorageDirectory() 
            .toString() + "/AndroidMedia/"; 
 
    private CameraPreview mCameraPreview; 
    private ImageView focusView; 
    private boolean isTaking = false; // 拍照中 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		  // 设置横屏 
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
        // 设置全屏 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        
		setContentView(R.layout.activity_main);
		 // 照相预览界面 
        mCameraPreview = (CameraPreview) findViewById(R.id.preview); 
        mCameraPreview.setOnCameraStatusListener(this); 
        // 焦点图片 
        focusView = (ImageView) findViewById(R.id.focusView); 
	}

	/** 
     * 触屏事件 
     */ 
    @Override 
    public boolean onTouchEvent(MotionEvent event) { 
        if (event.getAction() == MotionEvent.ACTION_DOWN && !isTaking) { 
            isTaking = true; 
            mCameraPreview.takePicture(); 
        } 
        return super.onTouchEvent(event); 
    } 
    
    /** 
     * 存储图像并将信息添加入媒体数据库 
     */ 
    private Uri insertImage(ContentResolver cr, String name, long dateTaken, 
            String directory, String filename, Bitmap source, byte[] jpegData) { 
 
        OutputStream outputStream = null; 
        String filePath = directory + filename; 
        Log.i("nick","directory:"+directory+"  filePath:"+filePath);
        try { 
            File dir = new File(directory); 
            if (!dir.exists()) { 
                dir.mkdirs(); 
            } 
            File file = new File(directory, filename); 
            if (file.createNewFile()) { 
                outputStream = new FileOutputStream(file); 
                if (source != null) { 
                    source.compress(CompressFormat.JPEG, 75, outputStream); 
                } else { 
                    outputStream.write(jpegData); 
                } 
            } 
        } catch (FileNotFoundException e) { 
            e.printStackTrace(); 
            return null; 
        } catch (IOException e) { 
            e.printStackTrace(); 
            return null; 
        } finally { 
            if (outputStream != null) { 
                try { 
                    outputStream.close(); 
                } catch (Throwable t) { 
                } 
            } 
        } 
        ContentValues values = new ContentValues(7); 
        values.put(MediaStore.Images.Media.TITLE, name); 
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename); 
        values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken); 
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); 
        values.put(MediaStore.Images.Media.DATA, filePath); 
        return cr.insert(IMAGE_URI, values); 
    } 
    
    /** 
     * 相机拍照结束事件 
     */ 
    @Override 
    public void onCameraStopped(byte[] data) { 
        Log.e("onCameraStopped", "==onCameraStopped=="); 
        // 创建图像 
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length); 
        // 系统时间 
        long dateTaken = System.currentTimeMillis(); 
        // 图像名称 
        String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken) 
                .toString() + ".jpg"; 
        // 存储图像（PATH目录） 
        Uri uri = insertImage(getContentResolver(), filename, dateTaken, PATH, 
                filename, bitmap, data); 
        // 返回结果 
      //  Intent intent = getIntent(); 
      //  intent.putExtra("uriStr", uri.toString()); 
      //  intent.putExtra("dateTaken", dateTaken); 
        // intent.putExtra("filePath", PATH + filename); 
        // intent.putExtra("orientation", orientation); // 拍摄方向 
       // setResult(20, intent); 
        // 关闭当前Activity 
        finish(); 
    } 
 
    /** 
     * 拍摄时自动对焦事件 
     */ 
    @Override 
    public void onAutoFocus(boolean success) { 
        // 改变对焦状态图像 
        if (success) { 
            focusView.setImageResource(R.drawable.btn_shutter_photo_focus); 
        } else { 
            focusView.setImageResource(R.drawable.btn_shutter_photo_pressed); 
            Toast.makeText(this, "焦距不准，请重拍！", Toast.LENGTH_SHORT).show(); 
            isTaking = false; 
        } 
    } 
	
   

}
