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

package com.sinpo.xnfc.card.pboc;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.sinpo.xnfc.MyApplication;
import com.sinpo.xnfc.R;
import com.sinpo.xnfc.ThreeDES;
import com.sinpo.xnfc.Util;
import com.sinpo.xnfc.tech.Iso14443;

final public class Fmcos1208 extends PbocCard {
	private static String TAG = "nick";
	private static MyApplication mMyAppliction;
	private final static byte[] DFN_SRV = { (byte) 'P', (byte) 'A', (byte) 'Y',
		(byte) '.', (byte) 'C', (byte) 'N', (byte) 'E' };
	

	private Fmcos1208(Iso14443.Tag tag, Resources res) {
		super(tag);
		
	}

	@SuppressWarnings("unchecked")
	final static Fmcos1208 load(Iso14443.Tag tag, Resources res,Context mContext) {
		
	   byte dat[]=tag.selectFM().getAllBytes();
	   Log.i("nick","response:"+Util.toHexString(dat, 0, dat.length));
		mMyAppliction=(MyApplication)mContext.getApplicationContext();
		mMyAppliction.setCardsType(MyApplication.isOdep);

		/*--------------------------------------------------------------*/
		// select PSF (1PAY.SYS.DDF01)
		/*--------------------------------------------------------------*/
		if (dat.length>2 && ((short)((dat[dat.length-2]<< 8)|(0xff&dat[dat.length-1]))==(short)0x9000)) {
			Log.i("nick","-----------------iso14443 ok------------");
			Iso14443.Response INFO, CASH;
			byte data[] = tag.selectByName(DFN_SRV).getAllBytes();
			Log.i("nick","selectByName:"+Util.toHexString(data, 0, data.length));
			
			if (isOkey(data)) {
				Log.i("nick","-----select DFN_SRV success -----");
				byte info[] =tag.readBinary(SFI_EXTRA).getAllBytes();
				Log.i("nick","readBinary:"+Util.toHexString(info, 0, info.length));
				/*byte src[]={0x00,0,0,0,0,0,1,3,0,0,0,1,3,0,0,0,1,3,0,0};
				byte encrykey[] = {(byte)0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11};
				byte rands[] = {(byte)0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x11};
				MacGen.Gen(src, encrykey, rands);*/
				
				tag.selectByName(DFN_SRV);
				//验证pin码
				byte pinkey[] = {(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04,(byte)0x05,(byte)0x06};
				byte retverify[] = tag.verifyPinKey((byte)0x01, pinkey).getAllBytes();
				Log.i("nick","verifyPinKey:"+Util.toHexString(retverify, 0, retverify.length));
				
				if(isOkey(retverify)){
					byte monery[] = {0x00,0x01,0x02,0x03};
					byte pos_id[] = {0x0,0x0,0x0,0x0,0x0,0x1};
					byte loadret[] = tag.initializeForLoad((byte)0x01, monery, pos_id).getAllBytes();
					Log.i("nick","initializeForLoad:"+Util.toHexString(loadret, 0, loadret.length));
					//生成过程密钥
					byte[] poress_key_src = new byte[16];
					poress_key_src[0] = (byte)0x08;
					poress_key_src[1] =  loadret[8];
					poress_key_src[2] =  loadret[9];
					poress_key_src[3] =  loadret[10];
					poress_key_src[4] =  loadret[11];
					poress_key_src[5] =  loadret[4];
					poress_key_src[6] =  loadret[5];
					poress_key_src[7] =  (byte)0x80;
					poress_key_src[8] =  (byte)0x00;
					poress_key_src[9] =  (byte)0x80;
					for(int i = 0; i<6;i++)
						poress_key_src[10+i] =  (byte)0x00;
					Log.i("nick","=============== poress_key_src ==============");
					MacGen.printHexString(poress_key_src);
					byte creditloadkey[] = {(byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0x88,(byte)0x88,(byte)0x88};
					byte[] deskey = new byte[24];  //DES认证密钥，24位
					System.arraycopy(creditloadkey, 0, deskey, 0, creditloadkey.length);
					for(int i=creditloadkey.length;i<24;i++){
						deskey[i]=(byte)0x00;
					}
					
					byte[] rands = new byte[8];
					rands[0] =  loadret[8];
					rands[1] =  loadret[9];
					rands[2] =  loadret[10];
					rands[3] =  loadret[11];
					rands[4] = 0x0;
					rands[5] = 0x0;
					rands[6] = 0x0;
					rands[7] = 0x0;
					
					
					byte[] poress_key_src1 = new byte[8];
					System.arraycopy(poress_key_src, 0, poress_key_src1, 0, 8);
					byte[] ret =ThreeDES.encryptMode(deskey, poress_key_src1);
					for(int i = 0;i<8;i++){
						poress_key_src[i] = ret[i];
					}
					byte[] poress_key_src2 = new byte[8];
					System.arraycopy(poress_key_src, 8, poress_key_src2, 0, 8);
					byte[] ret2 =ThreeDES.encryptMode(deskey, poress_key_src2);
					for(int i = 0;i<8;i++){
						poress_key_src[i+8] = ret2[i];
					}
					Log.i("nick","=============== DES key ==============");
					MacGen.printHexString(poress_key_src);
					Log.i("nick","======================================");
					byte mac_src[]=new byte[15];
					mac_src[0] = loadret[0];
					mac_src[1] = loadret[1];
					mac_src[2] = loadret[2];
					mac_src[3] = loadret[3];
					mac_src[4] = monery[0];
					mac_src[5] = monery[1];
					mac_src[6] = monery[2];
					mac_src[7] = monery[3];
					mac_src[8] = 0x02;
					for(int i = 0;i<6;i++){
						mac_src[i+9]=pos_id[i];
					}
					
					MacGen.Gen(mac_src, poress_key_src, rands);
					
					
					
				}
				final Fmcos1208 ret = new Fmcos1208(tag, res);
				ret.infodata =Util.decode(Util.toHexString(info, 0, info.length));
				ret.name = "xxx";
				return ret;
				
			}else if(FileNotFound(data)){				
				return null;
		}				
		}
		

		return null;
	}
	final static public int createWriteCard(Iso14443.Tag tag, Resources res,Context mContext,String write_name,String write_nums,String write_account){
			byte dat[]=tag.selectFM().getAllBytes();
			Log.i("nick","selectFM response:"+Util.toHexString(dat, 0, dat.length));
			if (isOkey(dat)) {
				Iso14443.Response INFO, CASH;
				byte data[] = tag.selectByName(DFN_SRV).getAllBytes();
				Log.i("nick","selectByName:"+Util.toHexString(data, 0, data.length));
				if (isOkey(data)) {
					Log.i("nick","-----select DFN_SRV success -----");
					Log.i(TAG,"-----This card already have information not right write,please erase first!!! -----");
					return -2;
					
				}else if(FileNotFound(data)){				
					tag.selectFM();
					byte[] dat3 =getExternAuthenticate(tag,ThreeDES.keyBytes1);
					if(keyFileNotFound(dat3)){
						Log.i("nick","----create MF key file ----");
						byte keysize[]={0x00,(byte)0xff};
						tag.selectFM();
						byte dat4[] = tag.createKeyfile((byte)0x00, (byte)0x01, keysize, (byte)0x01, (byte)0xf0, (byte)0xff).getAllBytes();
						Log.i("nick","MF createkeyfile:"+Util.toHexString(dat4, 0, dat4.length));
						if(FileIsExist(dat4)|| isOkey(dat4)){
							byte fileroadkey[] = {(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff};
							byte dat6[]=tag.createFileRoadKey((byte)0xf0, (byte)0xf0, (byte)0x55, fileroadkey).getAllBytes();
							Log.i("nick","createfilesroadkey:"+Util.toHexString(dat6, 0, dat6.length));
							if(isOkey(dat6)){
								Log.i("nick","------createfilesroadkey success -------");
							}
							byte externalkey[] = {(byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0xff,(byte)0xff,(byte)0xff};
							byte dat7[]=tag.createExternalAuthenticateKey((byte)0xf0, (byte)0xf0, (byte)0xaa, (byte)0x55, externalkey).getAllBytes();
							Log.i("nick","createExternalAuthenticateKey:"+Util.toHexString(dat6, 0, dat6.length));
							if(isOkey(dat7)){
								Log.i("nick","------createExternalAuthenticateKey success -------");
								byte fileid[]={(byte)0x3f,(byte)0x01};
								byte size[]={(byte)0x05,(byte)0x20};
								byte name[] = {(byte) 'P', (byte) 'A', (byte) 'Y',(byte) '.', (byte) 'C', (byte) 'N', (byte)'E'};
								byte ret[] = tag.createDFfile(fileid,size,(byte)0xf0,(byte)0xf0,(byte)0x95,(byte)0xff,name).getAllBytes();
								Log.i("nick","createDFfile:"+Util.toHexString(ret, 0, ret.length));
								if(isOkey(ret)){
									if(tag.selectByName(DFN_SRV).isOkey()){
										Log.i("nick","----create DF key file ----");
										byte dfkeysize[]={0x01,(byte)0x8f};
										byte datdd[] = tag.createKeyfile((byte)0x00, (byte)0x00, dfkeysize, (byte)0x95, (byte)0xf0, (byte)0xff).getAllBytes();
										Log.i("nick","createkeyfile:"+Util.toHexString(dat7, 0, dat7.length));
										if(isOkey(datdd)){
											byte dfexternalkey[] = {(byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0x66,(byte)0x77,(byte)0x88};
											byte dat8[]=tag.createExternalAuthenticateKey((byte)0xf0, (byte)0xf0, (byte)0xaa, (byte)0x55, dfexternalkey).getAllBytes();
											Log.i("nick","createExternalAuthenticateKey:"+Util.toHexString(dat8, 0, dat8.length));
											if(isOkey(dat8)){
												Log.i("nick","------df createExternalAuthenticateKey success -------");
												//圈钱密钥
												byte creditloadkey[] = {(byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0x88,(byte)0x88,(byte)0x88};
												byte retcreditload[]=tag.createCreditLoadKey((byte)0xf0, (byte)0xf0, (byte)0x01, creditloadkey).getAllBytes();
												Log.i("nick","createCreditLoadKey:"+Util.toHexString(retcreditload, 0, retcreditload.length));
												if(isOkey(retcreditload)){
													Log.i("nick","------df createCreditLoadKey success -------");
												}else{
													Log.i("nick","----create createCreditLoadKey  fail ----");
													return -1;
												}
												
												//口令密钥
												byte pinkey[] = {(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04,(byte)0x05,(byte)0x06};
												byte pinload[]=tag.createPINKey((byte)0x01, (byte)0xF0, (byte)0x44, (byte)0x55, pinkey).getAllBytes();
												Log.i("nick","createPINKey:"+Util.toHexString(pinload, 0, pinload.length));
												if(isOkey(pinload)){
													Log.i("nick","------df createPINKey success -------");
												}else{
													Log.i("nick","----create createPINKey  fail ----");
													return -1;
												}
												
												//解锁口令密钥
												byte unlockpinkey[] = {(byte)0x11,(byte)0x22,(byte)0x33,(byte)0x44,(byte)0x55,(byte)0x88,(byte)0x88,(byte)0x88};
												byte unlockpinload[]=tag.createUnlockPINKey((byte)0x01, (byte)0xf0, (byte)0xf0, (byte)0x55, unlockpinkey).getAllBytes();
												Log.i("nick","createUnlockPINKey:"+Util.toHexString(unlockpinload, 0, unlockpinload.length));
												if(isOkey(unlockpinload)){
													Log.i("nick","------df createUnlockPINKey success -------");
												}else{
													Log.i("nick","----create createUnlockPINKey  fail ----");
													return -1;
												}
												
												
												//建立钱包文件
												Log.i("nick","-----create PBOC wallet file  -----");
												byte[] pbocfile_id={0x00,(byte)0x02};
												byte recordfileid = 0x18;
												byte infopboc[] =tag.createPBOCfile(pbocfile_id, (byte)0xf0, recordfileid).getAllBytes();
												Log.i("nick","createPBOCfile:"+Util.toHexString(infopboc, 0, infopboc.length));
												if(isOkey(infopboc)){
													Log.i("nick","------df createPBOCfile success -------");
												}else{
													Log.i("nick","----create createPBOCfile  fail ----");
													return -1;
												}
												//建立循环交易记录文件
												Log.i("nick","-----create recorded file  -----");
												byte[] recordfile_id={0x00,(byte)0x18}; //标识必须跟钱包文件指定的标识一致
												byte[] recordfile_size={0x0A,0x17};  //2583 字节
												byte inforecord[] =tag.createEFfile(recordfile_id, (byte)0x2E, recordfile_size, (byte)0xf1, (byte)0xEF, (byte)0xff).getAllBytes();
												Log.i("nick","createBinary record file:"+Util.toHexString(inforecord, 0, inforecord.length));
												if(isOkey(inforecord)){
													Log.i("nick","------ create record file success -------");
												}else{
													Log.i("nick","----create create record file  fail ----");
													return -1;
												}
												
												//建立基本信息文件
												Log.i("nick","-----create ef file  -----");
												byte[] file_id={0x00,(byte)SFI_EXTRA};
												byte[] file_size={0x00,0x27};  //39 字节
												byte info[] =tag.createEFfile(file_id, (byte)0x28, file_size, (byte)0xf0, (byte)0xf0, (byte)0xff).getAllBytes();
												Log.i("nick","createBinary:"+Util.toHexString(info, 0, info.length));
												if(isOkey(info)){
													byte efdata[] =Util.hexStringToBytes(Util.encode("NAME:"+write_name+ " NUM:"+write_nums + " ACC:"+write_account));
													if(efdata.length<0x27){
														byte info2[] = tag.updateBinary((byte)0x0, (byte)0x0, efdata).getAllBytes();
														Log.i("nick","updateBinary:"+Util.toHexString(info2, 0, info2.length));
														if(isOkey(info2)){
															Log.i("nick","----create updateBinary  success. write informoation success ----");
															return 0;
														}else{
															Log.i("nick","----create updateBinary  fail ----");
															return -7;
														}
													}else{
														Log.i(TAG,"-----write data fail  write data too long !!!!!!!");
														return -8;
													}
												}else{
													Log.i("nick","----create createEFfile createBinary fail ----");
													return -6;
												}
											}else{
												Log.i("nick","----create DF createExternalAuthenticateKey fail ----");
												return -5;
											}
										}else{
											Log.i("nick","----create DF key file fail ----");
											return -4;
										}
										
									}else{
										Log.i("nick","---select DFN_SRV FALL ----");
										return -3;
									}
							}
							
						}
					}
				
				
					}
					
				}
			}else{
					Log.i("nick","----------selectFM  failed-------------");
				return -1;
			}
		return 0;
	}
	final static public int eraseFiles(Iso14443.Tag tag, Resources res,Context mContext) {
		
		byte dat[]=tag.selectFM().getAllBytes();
		 Log.i("nick","selectFM response:"+Util.toHexString(dat, 0, dat.length));	
			if (isOkey(dat)) {
				Log.i("nick","-----------------iso14443 ok------------");
					byte[] dat3 =getExternAuthenticate(tag,ThreeDES.keyBytes1);
					if(isOkey(dat3)){ //外部认证成功
						byte dat4[] = tag.eraseFMAllFiles().getAllBytes();
						Log.i("nick","eraseFMallfiles:"+Util.toHexString(dat4, 0, dat4.length));
						if(isOkey(dat4)){//擦除成功
							Log.i("nick","----------eraseFMallfiles sucess-------------");
							return 0;
						}
					}else{
						Log.i("nick","----------eraseFMallfiles  failed-------------");
						return -1;
					}
				}else{
				Log.i("nick","----------selectFM  failed-------------");
				return -1;
			}
		return 0;
	}
	
	private static byte[] getExternAuthenticate(Iso14443.Tag tag,byte keys[]){
		byte dat2[] = tag.getChallenge().getAllBytes();
		Log.i("nick","getExternAuthenticate get challenge response:"+Util.toHexString(dat2, 0, dat2.length));
		if(isOkey(dat2)){
			byte encodedat[]=new byte[8];
			System.arraycopy(dat2, 0, encodedat, 0, 4);
			encodedat[4]=0;
			encodedat[5]=0;
			encodedat[6]=0;
			encodedat[7]=0;
			byte encodedata[] = ThreeDES.encryptMode(keys, encodedat);
			Log.i("nick","getExternAuthenticate encode data:"+Util.toHexString(encodedata, 0, 8));
			for(int i=0;i<8;i++){
				encodedat[i]=encodedata[i];
			}
			byte dat3[] = tag.externalAuthenticate(encodedat).getAllBytes();
			Log.i("nick","externalAuthent response data:"+Util.toHexString(dat3, 0, dat3.length));
			return dat3;
		}
		return null;
	}
	
	private static boolean  isOkey(byte dat[]){
		if ((dat.length>=2) && ((short)((dat[dat.length-2]<< 8)|(0xff&dat[dat.length-1]))==Iso14443.SW_NO_ERROR)){
			Log.i(TAG,"@@@@@ command isOkey !!!");
			return true;
		}
		return false;
	}
	private static boolean  keyFileNotFound(byte dat[]){  //密钥为找到，注意这里不代表没有密钥文件
		if ((dat.length>=2) && ((short)((dat[dat.length-2]<< 8)|(0xff&dat[dat.length-1]))==Iso14443.SW_KEYFILE_NOT_FOUND)){
			Log.i(TAG,"@@@@@ keyFileNotFound !!!");
			return true;
		}
		return false;
	}
	private static boolean  FileNotFound(byte dat[]){
		if ((dat.length>=2) && ((short)((dat[dat.length-2]<< 8)|(0xff&dat[dat.length-1]))==Iso14443.SW_FILE_NOT_FOUND)){
			Log.i(TAG,"@@@@@ FileNotFound !!!");
			return true;
		}
		return false;
	}
	private static boolean  FileIsExist(byte dat[]){ //文件已存在
		if ((dat.length>=2) && ((short)((dat[dat.length-2]<< 8)|(0xff&dat[dat.length-1]))==Iso14443.SW_INCORRECT_P1P2)){
			Log.i(TAG,"@@@@@ FileIsExist !!!");
			return true;
		}
		return false;
	}
}
