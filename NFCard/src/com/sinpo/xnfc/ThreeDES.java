package com.sinpo.xnfc;

import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ThreeDES {

  final public static byte[] keyBytes = {    //FM1208 外部认证的出厂密钥
	  (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
	  (byte)0xff,  (byte) 0xff, (byte) 0xff, (byte) 0xff,
      0x0, 0x0, 0x0, 0x0, (byte)0x0, 0x0, 0x0, 0x0,
      0x0, 0x0, 0x0, 0x0, (byte)0x0, 0x0, 0x0, 0x0};    //24字节的密钥
  final public static byte[] keyBytes1 = {    //FM1208 自设的外部认证的密钥
	  (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44,
	  (byte)0x55,  (byte) 0xff, (byte) 0xff, (byte) 0xff,
      0x0, 0x0, 0x0, 0x0, (byte)0x0, 0x0, 0x0, 0x0,
      0x0, 0x0, 0x0, 0x0, (byte)0x0, 0x0, 0x0, 0x0};    //24字节的密钥
  final public static byte[] df_keyBytes = {    //FM1208 自设的df外部认证的密钥
	  (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44,
	  (byte)0x55,  (byte) 0x66, (byte) 0x77, (byte) 0x88,
      0x0, 0x0, 0x0, 0x0, (byte)0x0, 0x0, 0x0, 0x0,
      0x0, 0x0, 0x0, 0x0, (byte)0x0, 0x0, 0x0, 0x0};    //24字节的密钥


//解密用，不可更改
private static final byte[] endbyte ={(byte)0x82,(byte)0x07,(byte)0xEA,(byte)0x5D,(byte)0x3E,(byte)0x19,(byte)0xA5,(byte)0xFD};
private static final String Algorithm = "DESede"; //定义 加密算法,可用 DES,DESede,Blowfish
    
    //keybyte为加密密钥，长度为24字节
    //src为被加密的数据缓冲区（源）
    public static byte[] encryptMode(byte[] keybyte, byte[] src) {
       try {
            //生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);

            //加密
            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            return c1.doFinal(src);
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (java.lang.Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    //keybyte为解密密钥，长度为24字节
    //src为加密后的缓冲区
    public static byte[] decryptMode(byte[] keybyte, byte[] src) {      
    try {
	   byte[] desrc = new byte[src.length+endbyte.length];
	   System.arraycopy(src,0,desrc,0,src.length);
	   System.arraycopy(endbyte,0,desrc,src.length,endbyte.length);
            //生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);

            //解密
            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(Cipher.DECRYPT_MODE, deskey);
            return c1.doFinal(desrc);
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (java.lang.Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    //转换成十六进制字符串
    public static String byte2hex(byte[] b) {
        String hs="";
        String stmp="";

        for (int n=0;n<b.length;n++) {
            stmp=(java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length()==1) hs=hs+"0"+stmp;
            else hs=hs+stmp;
            //if (n<b.length-1)  hs=hs+":";
        }
        return hs.toUpperCase();
    }
    
   
}
