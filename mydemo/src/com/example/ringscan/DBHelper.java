package com.example.ringscan;

import android.content.ContentValues;  
import android.content.Context;  
import android.database.Cursor;  
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper;  
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper { 
	private static final String TAG="dbhelper";
	private static final boolean Debug=true;
	
	private static DBHelper mInstance = null;  
	
	private static final int VERSION = 1;
    private static final String DB_NAME = "coll.db";   //db name
    private static final String TBL_NAME = "CollTbl";  //table name
    private static final String CREATE_TBL = " create table "  
            + TBL_NAME+"(_id integer primary key autoincrement,name text,tel text) ";  
      
    private SQLiteDatabase db=null; 
    
    /**单例模式**/
    static synchronized DBHelper getInstance(Context context) {  
    if (mInstance == null) {  
        mInstance = new DBHelper(context);  
    }  
    return mInstance;  
    }  
    
    DBHelper(Context c) {  
        super(c, DB_NAME, null, VERSION); 
        if(Debug){
    		Log.i(TAG,"--DBHelper--");
    	}
        db = getWritableDatabase();  
    }  
    @Override  
    public void onCreate(SQLiteDatabase db) {  
        this.db = db;  
        db.execSQL(CREATE_TBL);  
     
        if(Debug){
    		Log.i(TAG,"--onCreate--");
    	}
    }  
    public void insert(ContentValues values) { 
    	if(Debug){
    		Log.i(TAG,"--insert--");
    	}
        db = getWritableDatabase();  
        long rowid = db.insert(TBL_NAME, null, values);
        db.close();  
    }
    /**
     * 根据ID号修改记录数据
     * */
    public void update(ContentValues values,int id){
    	if(Debug){
    		Log.i(TAG,"--update--");
    	}
        db = getWritableDatabase(); 
        String[] args = {String.valueOf(id)};
        db.update(TBL_NAME, values, "_id=?", args);
    }
    /**  
     * 查，查询表中所有的数据  
     */  
    public Cursor query() {  
    	if(Debug){
    		Log.i(TAG,"--query--");
    	}
         db = getReadableDatabase();  
        Cursor c = db.query(TBL_NAME, null, null, null, null, null, null);  
        return c;  
    } 
    /**  
     * 查询指定id的数据  
     */  
    public Cursor query(int id) {
    	if(Debug){
    		Log.i(TAG,"--query--");
    	}
         db = getReadableDatabase();  
        Cursor cursor = db.query(TBL_NAME, null, "_id=?", new String[]{String.valueOf(id)}, null, null, null);  
        return cursor;   
    }
    /**  
     * 查询指定关键字的数据  
     */  
    public Cursor query(String name) {
    	if(Debug){
    		Log.i(TAG,"--query--");
    	}
         db = getReadableDatabase();  
        Cursor cursor = db.query(TBL_NAME, new String[]{"_id,name,tel"}, "name=? or tel=?", new String[]{name,name}, null,null,null);
        
        return cursor;   
    }  
   
    public void deleteid(int id) {
    	if(Debug){
    		Log.i(TAG,"--deleteid--");
    	}
        if (db != null){  
            db = getWritableDatabase();  
            db.delete(TBL_NAME, "_id=?", new String[] { String.valueOf(id) });
        }
    }  
    public void deleteName(String name) {
    	if(Debug){
    		Log.i(TAG,"--deleteid--");
    	}
        if (db != null){  
            db = getWritableDatabase();  
            db.delete(TBL_NAME, "name=?", new String[] { name });
        }
    }  
    public void deleteTel(String tel) {
    	if(Debug){
    		Log.i(TAG,"--deleteid--");
    	}
        if (db != null){  
            db = getWritableDatabase();  
            db.delete(TBL_NAME, "tel=?", new String[] { tel });
        }
    }  
    public void deleteKey(String name ,String tel) {
    	if(Debug){
    		Log.i(TAG,"--deleteid--");
    	}
        if (db != null){  
            db = getWritableDatabase();  
            db.delete(TBL_NAME, "name=? and tel=?", new String[] { name,tel });
        }
    }  
    public void close() {
    	if(Debug){
    		Log.i(TAG,"--close--");
    	}
        if (db != null)  
            db.close();  
    }  
    public void deleteDB(){
    	if(Debug){
    		Log.i(TAG,"--deleteDB--");
    	}
    	 if (db != null){  
             db = getWritableDatabase();  
             db.delete(TBL_NAME, null, null);
             db=null;
    	 }else{
    		 if(Debug){
    	    		Log.i(TAG,"--DB is null--");
    	    	} 
    	 }
    }
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	if(Debug){
    		Log.i(TAG,"--onUpgrade--");
    	}
    }  
}  
