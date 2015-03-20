package com.example.ringscan;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class DBtestActivity extends Activity {
	 private Context mContext;
	 DBHelper helpter=null;
	 private Button btcreatedb,btdeletedb,btupgradedb,btadd,btdelete,dtdisplay,dtreseach;
	 EditText et_addname;
     EditText et_addtel;
     ListView displaylist;
     SimpleAdapter listItemAdapter;
     ArrayList<HashMap<String, Object>> listItem;
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED,FLAG_HOMEKEY_DISPATCHED);
	        setContentView(R.layout.activity_db);
	        mContext=this;
	        
	        btcreatedb = (Button)findViewById(R.id.bt_createDB);
	        btdeletedb = (Button)findViewById(R.id.bt_deleteDB);
	        btupgradedb = (Button)findViewById(R.id.bt_upgradeDB);
	        btadd = (Button)findViewById(R.id.bt_adddat);
	        btdelete = (Button)findViewById(R.id.bt_deledat);
	        dtdisplay = (Button)findViewById(R.id.bt_disdat);
	        dtreseach = (Button)findViewById(R.id.bt_reseach);
	        
	        btcreatedb.setOnClickListener(btOnClickListener);
	        btdeletedb.setOnClickListener(btOnClickListener);
	        btupgradedb.setOnClickListener(btOnClickListener);
	        btadd.setOnClickListener(btOnClickListener);
	        btdelete.setOnClickListener(btOnClickListener);
	        dtdisplay.setOnClickListener(btOnClickListener);
	        dtreseach.setOnClickListener(btOnClickListener);
	        
	        displaylist = (ListView) findViewById(R.id.display_dbdata);
	        //生成动态数组，加入数据  
	        listItem = new ArrayList<HashMap<String, Object>>();  
	      
	            HashMap<String, Object> map = new HashMap<String, Object>();  
	            map.put("ItemName", "NAME");  
	            map.put("ItemTel", "Tel");  
	            listItem.add(map);    
	           
	           
	        
	       //生成适配器的Item和动态数组对应的元素  
	       listItemAdapter = new SimpleAdapter(this,listItem,//数据源   
	            R.layout.db_datalist_items,//ListItem的XML实现  
	            //动态数组与ImageItem对应的子项          
	            new String[] {"ItemName", "ItemTel"},   
	            //ImageItem的XML文件里面的一个ImageView,两个TextView ID  
	            new int[] {R.id.ItemName,R.id.ItemTel}  
	        );  
	         
	        //添加并且显示  
	        displaylist.setAdapter(listItemAdapter);  
	       
	        displaylist.setOnItemLongClickListener(new OnItemLongClickListener(){

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					 HashMap<String, Object> map = (HashMap<String, Object>) arg0.getItemAtPosition(arg2);
		             
		             String name = map.get("ItemName").toString();
		             String tel = map.get("ItemTel").toString();
		             final int id = (Integer) map.get("DBId");
		             
		             Log.i("nick","id:"+id+" "+map.get("ItemName").toString()+"  "+map.get("ItemTel").toString()); 
		             
		             // 取得自定义View    
				        LayoutInflater layoutInflater = LayoutInflater.from(mContext);   
				        final View myChangeDBView = layoutInflater.inflate(R.layout.aertdialog_adddb, null);
				        
				    	EditText et_addname = (EditText)myChangeDBView.findViewById(R.id.et_addname);
                    	EditText et_addtel = (EditText)myChangeDBView.findViewById(R.id.et_addtel);	        
				        
                    	Dialog alertDialog = new AlertDialog.Builder(mContext).   
				                setTitle("change DB data").   
				                setIcon(R.drawable.ic_launcher).   
				                setView(myChangeDBView).   
				                setPositiveButton("Ok", new DialogInterface.OnClickListener() {   
				   
				                    @Override   
				                    public void onClick(DialogInterface dialog, int which) { 
				                    
				                    	EditText et_addname = (EditText)myChangeDBView.findViewById(R.id.et_addname);
				                    	EditText et_addtel = (EditText)myChangeDBView.findViewById(R.id.et_addtel);	       
				                        // TODO Auto-generated method stub
				                    	 String name = et_addname.getText().toString();  
				                         String tel = et_addtel.getText().toString(); 
				                         helpter = DBHelper.getInstance(mContext);
				                         ContentValues values = new ContentValues();  
				                         values.put("name", name);  
				                         values.put("tel", tel);     
				                         helpter.update(values, id);
				                         displayDAT();
				                    }   
				                }).   
				                setNegativeButton("Cancle", new DialogInterface.OnClickListener() {   
				   
				                    @Override   
				                    public void onClick(DialogInterface dialog, int which) {   
				                        // TODO Auto-generated method stub
				                    	
				                    }   
				                }).   
				                create();   
				        alertDialog.show(); 
				        et_addname.setText(name);
                    	et_addtel.setText(tel);
                    	et_addname.setSelectAllOnFocus(true);
                    	et_addtel.setSelectAllOnFocus(true);
					return false;
				}
	        	
	        });
	           
	      
	 }
	 
	 private OnClickListener btOnClickListener = new OnClickListener()
	 {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch(arg0.getId()){
			case R.id.bt_createDB:
				helpter = DBHelper.getInstance(mContext);
				break;
			case R.id.bt_deleteDB:
				if(helpter!=null){
					helpter.deleteDB();
					displayDAT();
				}
				
				break;
			case R.id.bt_upgradeDB:
				break;
			case R.id.bt_adddat:
				helpter = DBHelper.getInstance(mContext);
				 // 取得自定义View    
		        LayoutInflater layoutInflater = LayoutInflater.from(mContext);   
		        final View myAddDBView = layoutInflater.inflate(R.layout.aertdialog_adddb, null);   
		        
		       
		        
		        Dialog alertDialog = new AlertDialog.Builder(mContext).   
		                setTitle("add DB data").   
		                setIcon(R.drawable.ic_launcher).   
		                setView(myAddDBView).   
		                setPositiveButton("Ok", new DialogInterface.OnClickListener() {   
		   
		                    @Override   
		                    public void onClick(DialogInterface dialog, int which) { 
		                    	EditText et_addname = (EditText)myAddDBView.findViewById(R.id.et_addname);
		                    	EditText et_addtel = (EditText)myAddDBView.findViewById(R.id.et_addtel);
		                        // TODO Auto-generated method stub
		                    	 String name = et_addname.getText().toString();  
		                         String tel = et_addtel.getText().toString();  
		                         ContentValues values = new ContentValues();  
		                         values.put("name", name);  
		                         values.put("tel", tel);               
		                         if(helpter!=null){
		                         helpter.insert(values); 
		                         } 
		                         displayDAT();
		                    }   
		                }).   
		                setNegativeButton("Cancle", new DialogInterface.OnClickListener() {   
		   
		                    @Override   
		                    public void onClick(DialogInterface dialog, int which) {   
		                        // TODO Auto-generated method stub
		                    	
		                    }   
		                }).   
		                create();   
		        alertDialog.show();   
				
				break;
			case R.id.bt_deledat:
				 LayoutInflater layoutInflaterdel = LayoutInflater.from(mContext);   
			        final View myAddDBViewdel = layoutInflaterdel.inflate(R.layout.aertdialog_adddb, null);   
			        
			        helpter = DBHelper.getInstance(mContext);
			        
			        Dialog alertDialogdel = new AlertDialog.Builder(mContext).   
			                setTitle("delete DB data").   
			                setIcon(R.drawable.ic_launcher).   
			                setView(myAddDBViewdel).   
			                setPositiveButton("Ok", new DialogInterface.OnClickListener() {   
			   
			                    @Override   
			                    public void onClick(DialogInterface dialog, int which) { 
			                    	EditText et_addname = (EditText)myAddDBViewdel.findViewById(R.id.et_addname);
			                    	EditText et_addtel = (EditText)myAddDBViewdel.findViewById(R.id.et_addtel);
			                        // TODO Auto-generated method stub
			                    	 String etname = et_addname.getText().toString();  
			                         String ettel = et_addtel.getText().toString();
			                        
			                        if(etname.length()>0 && ettel.length()>0){
			                        	 helpter.deleteKey(etname, ettel);
			                        }else if(etname.length()>0){  
			                        	 helpter.deleteName(etname);
			                        }else if(ettel.length()>0){     	
			                        	 helpter.deleteTel(ettel);
			                        }
			                        
			                        displayDAT();
			                    } 
			                }).   
			                setNegativeButton("Cancle", new DialogInterface.OnClickListener() {   
			   
			                    @Override   
			                    public void onClick(DialogInterface dialog, int which) {   
			                        // TODO Auto-generated method stub
			                    	
			                    }   
			                }).   
			                create();   
			        alertDialogdel.show();  
				break;
			case R.id.bt_disdat:
				Thread dis =new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						displayDAT();
					}
					
				});
				dis.start();
				
				break;
			case R.id.bt_reseach:
				  LayoutInflater layoutInflater2 = LayoutInflater.from(mContext);   
			        final View myAddDBView2 = layoutInflater2.inflate(R.layout.aertdialog_adddb, null);   
			        
			     
			        
			        Dialog alertDialog2 = new AlertDialog.Builder(mContext).   
			                setTitle("reseach DB data").   
			                setIcon(R.drawable.ic_launcher).   
			                setView(myAddDBView2).   
			                setPositiveButton("Ok", new DialogInterface.OnClickListener() {   
			   
			                    @Override   
			                    public void onClick(DialogInterface dialog, int which) { 
			                    	EditText et_addname = (EditText)myAddDBView2.findViewById(R.id.et_addname);
			                    	EditText et_addtel = (EditText)myAddDBView2.findViewById(R.id.et_addtel);
			                        // TODO Auto-generated method stub
			                    	final String etname = et_addname.getText().toString();  
			                        final String ettel = et_addtel.getText().toString();
			                       
			                        listItem.clear();
			                        new Thread(new Runnable(){
			                        	@Override
			        					public void run() {
			                        		   helpter = DBHelper.getInstance(mContext);
			                        		  Cursor cursor=null;
						                        if(etname.length()>0){  
						                        	 cursor=helpter.query(etname);
						                        }else if(ettel.length()>0){
						                        	 cursor=helpter.query(ettel);
						                        }
			                        	  //cursor.moveToFirst();
					      				   while (cursor!=null && cursor.moveToNext()){  
					      			            int _id = cursor.getInt(cursor.getColumnIndex("_id"));  
					      			            String name = cursor.getString(cursor.getColumnIndex("name"));  
					      			            String tel = cursor.getString(cursor.getColumnIndex("tel"));  
					      			             
					      			              Log.i("nick","id:"+_id+" name:"+name+" TEL:"+tel);
					      			            HashMap<String, Object> map = new HashMap<String, Object>();
					      			            map.put("DBId",_id);
									            map.put("ItemName", name);  
									            map.put("ItemTel", tel);  
									            listItem.add(map);  
					      			           
					      			          }
					      				   if(cursor!=null){
					      			          cursor.close();
					      				   }
					      				   Message message=new Message();//创建Message对象 
					 		      	      message.what=0;  
					 		      		  mHandler.sendMessage(message);//调用主控制类中的Handler对象发送消息
			                        	}
			                        
			                        }).start();
			      				 
			                    } 
			                }).   
			                setNegativeButton("Cancle", new DialogInterface.OnClickListener() {   
			   
			                    @Override   
			                    public void onClick(DialogInterface dialog, int which) {   
			                        // TODO Auto-generated method stub
			                    	
			                    }   
			                }).   
			                create();   
			        alertDialog2.show();  
				break;
			}
		}
		 
	 };
	 
	 private void displayDAT(){
		 helpter = DBHelper.getInstance(mContext);
		   Cursor cursor=helpter.query();
		   //cursor.moveToFirst();
		   listItem.clear();
		   while (cursor.moveToNext()){  
	            int _id = cursor.getInt(cursor.getColumnIndex("_id"));  
	            String name = cursor.getString(cursor.getColumnIndex("name"));  
	            String tel = cursor.getString(cursor.getColumnIndex("tel"));  
	             
	              Log.i("nick","id:"+_id+" name:"+name+" TEL:"+tel);
	              HashMap<String, Object> map = new HashMap<String, Object>();
	                map.put("DBId",_id);
		            map.put("ItemName", name);  
		            map.put("ItemTel", tel);  
		            listItem.add(map);  
	          }
		   
	          cursor.close();
	          Message message=new Message();//创建Message对象 
    	      message.what=0;  
    		  mHandler.sendMessage(message);//调用主控制类中的Handler对象发送消息
	              
	 }
	 
	 Handler mHandler = new Handler(){
		 public void handleMessage(Message msg)  
	       {  
	           switch(msg.what)//判断传入的消息  
	           {  
	               case 0:  
	            	   displaylist.setAdapter(listItemAdapter);
	            	   break;
	           }
	       }
	 };
	 
	 


}
