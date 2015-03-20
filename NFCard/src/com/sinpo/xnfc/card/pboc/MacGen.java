package com.sinpo.xnfc.card.pboc;

import com.sinpo.xnfc.ThreeDES;

import android.util.Log;

//只针对Fmcos1208 产生MAC数
public class MacGen {
		final static byte[] Gen(byte[] src, byte[] encryptkey, byte[] randnums){
			
			byte[] deskey = new byte[24];  //DES认证密钥，24位  //密钥左半部分
			byte[] deskey2 = new byte[24];  //DES认证密钥，24位  //密钥右半部分
			byte[] new_src;
			
			if(encryptkey.length !=8 && encryptkey.length != 16){
				Log.i("nick", "-----DES key worng!!!!!!");
				return null;
			}else{
				System.arraycopy(encryptkey, 0, deskey, 0, 8);
				for(int i=8;i<24;i++){
					deskey[i]=(byte)0x00;
				}
				if(encryptkey.length == 16){
					System.arraycopy(encryptkey, 8, deskey2, 0, 8);
					for(int i=8;i<24;i++){
						deskey2[i]=(byte)0x00;
					}
				}
			}
			//将源数组补足8的倍数
			if(src.length % 8 == 0){
				new_src = new byte[src.length+8];
				for(int i=0; i<src.length;i++){
					new_src[i] = src[i];
				}
				byte prefx[]={(byte)0x80,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
				System.arraycopy(prefx, 0, new_src, src.length, prefx.length);
			}else{
				new_src = new byte[(src.length/8+1)*8];
				for(int i=0; i<src.length;i++){
					new_src[i] = src[i];
				}	
				new_src[src.length]=(byte)0x80;
				for(int i=src.length+1;i<(src.length/8+1)*8;i++){
					new_src[i] = 0x00;
				}	
			}
		
			printHexString(new_src);
			if(randnums.length != 8){
				Log.i("nick","Mac randums is wrong!!!!!! randums not 8 bytes");
				return null;
			}
				for(int i = 0; i<8;i++){
					new_src[i] = (byte) (new_src[i] ^ randnums[i]) ;
				}
				Log.i("nick","===============1  ^randnums ==============");
				
				byte[] Data1 = new byte[8];
				System.arraycopy(new_src, 0, Data1, 0, 8);
				printHexString(Data1);
				
				for(int kk = 0;kk<new_src.length/8;kk++){
					byte[] ret =ThreeDES.encryptMode(deskey, Data1);
					for(int i = 0;i<8;i++){
						Data1[i] = ret[i];
					}
					Log.i("nick","==============="+kk+"  DES key ==============");
					printHexString(Data1);
					if(kk == new_src.length/8-1){
						break;
					}
					for(int i = 0; i<8;i++){
						Data1[i] = (byte) (Data1[i] ^ new_src[kk*8+i+8]) ;
					}
					Log.i("nick","==============="+(kk*8+8)+"  ^Data ==============");
					printHexString(Data1);
				}
				if(encryptkey.length == 16){
					byte[] ret =ThreeDES.encryptMode(deskey2, Data1);
					for(int i = 0;i<8;i++){
						Data1[i] = ret[i];
					}
					Log.i("nick","=============== *****deskey2 DES key ==============");
					printHexString(Data1);
					
					byte[] ret2 =ThreeDES.encryptMode(deskey, Data1);
					for(int i = 0;i<8;i++){
						Data1[i] = ret2[i];
					}
					Log.i("nick","=============== *****deskey DES key ==============");
					printHexString(Data1);
					
				}
				

			
			return null;
		}
		
		 //将指定byte数组以16进制的形式打印到控制台  
	    public static void printHexString( byte[] b) {    
	       for (int i = 0; i < b.length; i++) {   
	         String hex = Integer.toHexString(b[i] & 0xFF);   
	         if (hex.length() == 1) {   
	           hex = '0' + hex;   
	         }   
	         Log.i("nick", (hex.toUpperCase()));   
	       }   
	      
	    }  

}
