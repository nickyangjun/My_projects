/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.sinpo.xnfc.card;

import java.nio.charset.Charset;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcV;
import android.util.Log;
import android.widget.Toast;

import com.sinpo.xnfc.MyApplication;
import com.sinpo.xnfc.R;
import com.sinpo.xnfc.TextRecord;
import com.sinpo.xnfc.Util;
import com.sinpo.xnfc.info.Ndef_info;

public final class NdefCard {
	private static final int SYS_UNKNOWN = 0x00000000;
	private static final int SYS_SZLIB = 0x00010000;
	private static final int DEP_SZLIB_CENTER = 0x0100;
	private static final int DEP_SZLIB_NANSHAN = 0x0200;

	private static final int SRV_USER = 0x0001;
	private static final int SRV_BOOK = 0x0002;

	public static final int SW1_OK = 0x00;
	public static boolean isNdefCard=false;
	public static Ndef mndef = null;
	private static MyApplication mMyAppliction;

	static String load(Ndef tech, Resources res,Context mContext) {
		
		String data = "";
		try {
			tech.connect();
			
			mMyAppliction=(MyApplication)mContext.getApplicationContext();
			mMyAppliction.setCardsType(MyApplication.isNdef);
			
			mndef = tech;
			
		//	data=tech.getType()+"\r\n";
		//	data+="最大容量："+tech.getMaxSize()+ "bytes\r\n";

			int pos, BLKSIZE, BLKCNT;
			byte cmd[], rsp[], ID[], RAW[], STA[], flag, DSFID;

			ID = tech.getTag().getId();
			
			Log.i("nick","ID:"+Util.toHexString(ID, 0, ID.length));
		
			if (ID == null)
				throw new Exception();
			NdefMessage msgs = null;
			data+="ID:"+Util.toHexString(ID, 0, ID.length)+"<br />"+"<br />";
			
			
			msgs = tech.getNdefMessage();
			 if (msgs != null) {
				 Log.i("nick","----msgs");
				 NdefRecord record[]=null;
                 //程序中只考虑了1个NdefRecord对象，若是通用软件应该考虑所有的NdefRecord对象
                  record = msgs.getRecords();
                  if(record != null){
                	  Log.i("nick","record:"+record.length);
                	  for(int i=0;i<record.length;i++){
                		  //分析第1个NdefRecorder，并创建TextRecord对象
                          TextRecord textRecord = TextRecord.parse(record[i]);
                          //获取实际的数据占用的大小，并显示在窗口上
                          data += textRecord.getText() + "\r\n";
                         
                       
                	  }
                	  String strarray[] = data.split("<br />");
                	  Ndef_info ninfo = new Ndef_info();
                	  for(int i = 0;i<strarray.length;i++){
                		
                		  if(strarray[i].equals("Name:")){
                			  ninfo.setName(strarray[i+1]);
                		  }
                		  if(strarray[i].equals("Account Number:")){
                			  ninfo.setAccount(strarray[i+1]);
                		  }
                		  if(strarray[i].equals("Card Number:")){
                			  ninfo.setCard_nums(strarray[i+1]);
                		  }
                		  if(strarray[i].equals("Balance:")){
                			  ninfo.setBalance(strarray[i+1]);
                		  }
                		  if(strarray[i].equals("Password:")){
                			  ninfo.setPasswd(strarray[i+1]);
                		  }
                		  if(strarray[i].equals("TXT:")){
                			  ninfo.setTxt(strarray[i+1]);
                		  }
                	  }
                	  Log.i("nick","read data: "+ninfo.toString());
                	  mMyAppliction.tagsMap.put(MyApplication.isNdef, ninfo);
                  }
                  
                  
               
              

             }


		
		} catch (Exception e) {
			data = null;
			// data = e.getMessage();
		}

		try {
			tech.close();
		} catch (Exception e) {
		}
		

		return data;
	}
	
	public static boolean write(String string,Ndef ndef){
		
        //创建NdefMessage对象和NdefRecord对象
        NdefMessage ndefMessage = new NdefMessage(
                new NdefRecord[] {createTextRecord(string)});

        //开始向标签写入文本
        if (writeTag(ndefMessage, ndef.getTag())) {
           
           Log.i("nick","----write ok-----");
           return true;
           
        }
        return false;

	}
	
	  //创建一个封装要写入的文本的NdefRecord对象
    public static NdefRecord createTextRecord(String text) {
        //生成语言编码的字节数组，中文编码
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(
                Charset.forName("US-ASCII"));
        //将要写入的文本以UTF_8格式进行编码
        Charset utfEncoding = Charset.forName("UTF-8");
        //由于已经确定文本的格式编码为UTF_8，所以直接将payload的第1个字节的第7位设为0
        byte[] textBytes = text.getBytes(utfEncoding);
        int utfBit = 0;
        //定义和初始化状态字节
        char status = (char) (utfBit + langBytes.length);
        //创建存储payload的字节数组
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        //设置状态字节
        data[0] = (byte) status;
        //设置语言编码
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        //设置实际要写入的文本
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length,
                textBytes.length);
        //根据前面设置的payload创建NdefRecord对象
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }
    
    //将NdefMessage对象写入标签，成功写入返回ture，否则返回false
    static boolean  writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
         Log.i("nick","----size:"+size);
        try {
            //获取Ndef对象
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
            	Log.i("nick","------write-------");
                //允许对标签进行IO操作
                ndef.connect();
 
                if (!ndef.isWritable()) {
                 /*   Toast.makeText(this, "NFC Tag是只读的！", Toast.LENGTH_LONG)
                            .show();*/
                	Log.i("nick","NFC Tag是只读的！");
                   
                    return false;
 
                }
                if (ndef.getMaxSize() < size) {
                    //Toast.makeText(this, "NFC Tag的空间不足！", Toast.LENGTH_LONG)
                      //      .show();
                    Log.i("nick","NFC Tag的空间不足！");
                    return false;
                }
 
                //向标签写入数据
                ndef.writeNdefMessage(message);
               // Toast.makeText(this, "已成功写入数据！", Toast.LENGTH_LONG).show();
                Log.i("nick","已成功写入数据！");
                return true;
 
            } else {
            	Log.i("nick","------no denf write-------");
                //获取可以格式化和向标签写入数据NdefFormatable对象
                NdefFormatable format = NdefFormatable.get(tag);
                //向非NDEF格式或未格式化的标签写入NDEF格式数据
                if (format != null) {
                    try {
                        //允许对标签进行IO操作
                        format.connect();
                        format.format(message);
                      /*  Toast.makeText(this, "已成功写入数据！", Toast.LENGTH_LONG)
                                .show();*/
                        Log.i("nick","已成功写入数据！");
                        return true;
 
                    } catch (Exception e) {
                        /*Toast.makeText(this, "写入NDEF格式数据失败！", Toast.LENGTH_LONG)
                                .show();*/
                    	  Log.i("nick","写入NDEF格式数据失败！");
                        return false;
                    }
                } else {
                    /*Toast.makeText(this, "NFC标签不支持NDEF格式！", Toast.LENGTH_LONG)
                            .show();*/
                	Log.i("nick",	"NFC标签不支持NDEF格式！");
                    return false;
 
                }
            }
        } catch (Exception e) {
        //    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        	Log.i("nick","----"+e.getMessage());
            return false;
        }
 
    }


	private static int parseType(byte dsfid, byte[] raw, int blkSize) {
		int ret = SYS_UNKNOWN;
		if (blkSize == 4 && (raw[4] & 0x10) == 0x10 && (raw[14] & 0xAB) == 0xAB
				&& (raw[13] & 0xE0) == 0xE0) {
			ret = SYS_SZLIB;

			if ((raw[13] & 0x0F) == 0x05)
				ret |= DEP_SZLIB_CENTER;
			else
				ret |= DEP_SZLIB_NANSHAN;

			if (raw[4] == 0x12)
				ret |= SRV_USER;
			else
				ret |= SRV_BOOK;

		}
		return ret;
	}

	private static String parseName(int type, Resources res) {
		if ((type & SYS_SZLIB) == SYS_SZLIB) {
			final String dep;
			if ((type & DEP_SZLIB_CENTER) == DEP_SZLIB_CENTER)
				dep = res.getString(R.string.name_szlib_center);
			else if ((type & DEP_SZLIB_NANSHAN) == DEP_SZLIB_NANSHAN)
				dep = res.getString(R.string.name_szlib_nanshan);
			else
				dep = null;

			final String srv;
			if ((type & SRV_BOOK) == SRV_BOOK)
				srv = res.getString(R.string.name_lib_booktag);
			else if ((type & SRV_USER) == SRV_USER)
				srv = res.getString(R.string.name_lib_readercard);
			else
				srv = null;

			if (dep != null && srv != null)
				return dep + " " + srv;
		}

		return res.getString(R.string.name_unknowntag);
	}

	private static String parseInfo(byte[] id, Resources res) {
		final StringBuilder r = new StringBuilder();
		final String i = res.getString(R.string.lab_id);
		r.append("<b>").append(i).append("</b> ")
				.append(Util.toHexStringR(id, 0, id.length));

		return r.toString();
	}

	private static String parseData(int type, byte[] raw, byte[] sta,
			int blkSize, Resources res) {
		if ((type & SYS_SZLIB) == SYS_SZLIB) {
			return parseSzlibData(type, raw, sta, blkSize, res);
		}
		return null;
	}

	private static String parseSzlibData(int type, byte[] raw, byte[] sta,
			int blkSize, Resources res) {

		long id = 0;
		for (int i = 3; i > 0; --i)
			id = (id <<= 8) | (0x000000FF & raw[i]);

		for (int i = 8; i > 4; --i)
			id = (id <<= 8) | (0x000000FF & raw[i]);

		final String sid;
		if ((type & SRV_USER) == SRV_USER)
			sid = res.getString(R.string.lab_user_id);
		else
			sid = res.getString(R.string.lab_bktg_sn);

		final StringBuilder r = new StringBuilder();
		r.append("<b>").append(sid).append(" <font color=\"teal\">");
		r.append(String.format("%013d", id)).append("</font></b><br />");

		final String scat;
		if ((type & SRV_BOOK) == SRV_BOOK) {
			final byte cat = raw[12];
			if ((type & DEP_SZLIB_NANSHAN) == DEP_SZLIB_NANSHAN) {
				if (cat == 0x10)
					scat = res.getString(R.string.name_bkcat_soc);
				else if (cat == 0x20) {
					if (raw[11] == (byte) 0x84)
						scat = res.getString(R.string.name_bkcat_ltr);
					else
						scat = res.getString(R.string.name_bkcat_sci);
				} else
					scat = null;
			} else {
				scat = null;
			}

			if (scat != null) {
				final String scl = res.getString(R.string.lab_bkcat);
				r.append("<b>").append(scl).append("</b> ").append(scat)
						.append("<br />");
			}
		}

		// final int len = raw.length;
		// for (int i = 1, n = 0; i < len; i += blkSize) {
		// final String blk = Util.toHexString(raw, i, blkSize);
		// r.append("<br />").append(n++).append(": ").append(blk);
		// }

		// final String blk = Util.toHexString(sta, 0, blkSize);
		// r.append("<br />S: ").append(blk);
		return r.toString();
	}
}
