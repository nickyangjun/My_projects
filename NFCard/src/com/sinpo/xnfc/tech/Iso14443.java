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

package com.sinpo.xnfc.tech;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import android.nfc.tech.IsoDep;
import android.util.Log;

import com.sinpo.xnfc.Util;

public class Iso14443 {
	public static final byte[] EMPTY = { 0 };

	protected byte[] data;

	protected Iso14443() {
		data = Iso14443.EMPTY;
	}

	protected Iso14443(byte[] bytes) {
		data = (bytes == null) ? Iso14443.EMPTY : bytes;
	}

	public boolean match(byte[] bytes) {
		return match(bytes, 0);
	}

	public boolean match(byte[] bytes, int start) {
		final byte[] data = this.data;
		if (data.length <= bytes.length - start) {
			for (final byte v : data) {
				if (v != bytes[start++])
					return false;
			}
		}
		return true;
	}

	public boolean match(byte tag) {
		return (data.length == 1 && data[0] == tag);
	}

	public boolean match(short tag) {
		final byte[] data = this.data;
		if (data.length == 2) {
			final byte d0 = (byte) (0x000000FF & tag);
			final byte d1 = (byte) (0x000000FF & (tag >> 8));
			return (data[0] == d0 && data[1] == d1);
		}
		return false;
	}

	public int size() {
		return data.length;
	}

	public byte[] getBytes() {
		return data;
	}

	@Override
	public String toString() {
		return Util.toHexString(data, 0, data.length);
	}

	public final static class ID extends Iso14443 {
		public ID(byte[] bytes) {
			super(bytes);
		}
	}

	public final static class Response extends Iso14443 {
		public static final byte[] EMPTY = {};
		public static final byte[] ERROR = { 0x6F, 0x00 }; // SW_UNKNOWN

		public Response(byte[] bytes) {
			super((bytes == null || bytes.length < 2) ? Response.ERROR : bytes);
		}

		public byte getSw1() {
			return data[data.length - 2];
		}

		public byte getSw2() {
			return data[data.length - 1];
		}

		public short getSw12() {
			final byte[] d = this.data;
			int n = d.length;
			return (short) ((d[n - 2] << 8) | (0xFF & d[n - 1]));
		}

		public boolean isOkey() {
			return equalsSw12(SW_NO_ERROR);
		}

		public boolean equalsSw12(short val) {
			return getSw12() == val;
		}

		public int size() {
			return data.length - 2;
		}
		public byte[] getAllBytes() {
			return  Arrays.copyOfRange(data, 0, data.length);
		}
		public byte[] getBytes() {
			return isOkey() ? Arrays.copyOfRange(data, 0, size())
					: Response.EMPTY;
		}
	}

	public final static class Tag {
		private final IsoDep nfcTag;
		private ID id;

		public Tag(IsoDep tag) {
			nfcTag = tag;
			id = new ID(tag.getTag().getId());
			Log.i("nick","ID:"+id.toString());
		}

		public ID getID() {
			return id;
		}

		public Response verify() {
			final byte[] cmd = { (byte) 0x00, // CLA Class
					(byte) 0x20, // INS Instruction
					(byte) 0x00, // P1 Parameter 1
					(byte) 0x00, // P2 Parameter 2
					(byte) 0x02, // Lc
					(byte) 0x12, (byte) 0x34, };

			return new Response(transceive(cmd));
		}
		
		public Response verifyPinKey(byte pinkeyid,byte... pinkey) {
			ByteBuffer buff = ByteBuffer.allocate(pinkey.length + 5);
			buff.put((byte) 0x00) // CLA Class
					.put((byte) 0x20) // INS Instruction
					.put((byte) 0x00) // P1 Parameter 1
					.put((byte) pinkeyid) // P2 Parameter 2
					.put((byte) pinkey.length) // Lc
					.put(pinkey);

			return new Response(transceive(buff.array()));
		}


		public Response initPurchase(boolean isEP) {
			final byte[] cmd = {
					(byte) 0x80, // CLA Class
					(byte) 0x50, // INS Instruction
					(byte) 0x01, // P1 Parameter 1
					(byte) (isEP ? 2 : 1), // P2 Parameter 2
					(byte) 0x0B, // Lc
					(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
					(byte) 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x33,
					(byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x0F, // Le
			};

			return new Response(transceive(cmd));
		}

		public Response getBalance(boolean isEP) {
			final byte[] cmd = { (byte) 0x80, // CLA Class
					(byte) 0x5C, // INS Instruction
					(byte) 0x00, // P1 Parameter 1
					(byte) (isEP ? 2 : 1), // P2 Parameter 2
					(byte) 0x04, // Le
			};

			return new Response(transceive(cmd));
		}

		public Response readRecord(int sfi, int index) {
			final byte[] cmd = { (byte) 0x00, // CLA Class
					(byte) 0xB2, // INS Instruction
					(byte) index, // P1 Parameter 1
					(byte) ((sfi << 3) | 0x04), // P2 Parameter 2
					(byte) 0x00, // Le
			};

			return new Response(transceive(cmd));
		}

		public Response readRecord(int sfi) {
			final byte[] cmd = { (byte) 0x00, // CLA Class
					(byte) 0xB2, // INS Instruction
					(byte) 0x01, // P1 Parameter 1
					(byte) ((sfi << 3) | 0x05), // P2 Parameter 2
					(byte) 0x00, // Le
			};

			return new Response(transceive(cmd));
		}

		public Response readBinary(int sfi) {
			final byte[] cmd = { (byte) 0x00, // CLA Class
					(byte) 0xB0, // INS Instruction
					(byte) (0x00000080 | (sfi & 0x1F)), // P1 Parameter 1
					(byte) 0x00, // P2 Parameter 2
					(byte) 0x00, // Le
			};

			return new Response(transceive(cmd));
		}
		
		public Response updateBinary(byte pos1, byte pos2,byte data[]) {
			ByteBuffer buff = ByteBuffer.allocate(data.length + 5);
			buff.put((byte) 0x00) // CLA Class
					.put((byte) 0xD6) // INS Instruction
					.put((byte) pos1) // P1 Parameter 1
					.put((byte) pos2) // P2 Parameter 2
					.put((byte) data.length) // Lc
					.put(data);

			return new Response(transceive(buff.array()));
		}

		public Response readData(int sfi) {
			final byte[] cmd = { (byte) 0x80, // CLA Class
					(byte) 0xCA, // INS Instruction
					(byte) 0x00, // P1 Parameter 1
					(byte) (sfi & 0x1F), // P2 Parameter 2
					(byte) 0x00, // Le
			};

			return new Response(transceive(cmd));
		}

		public Response selectByID(byte... name) {
			ByteBuffer buff = ByteBuffer.allocate(name.length + 6);
			buff.put((byte) 0x00) // CLA Class
					.put((byte) 0xA4) // INS Instruction
					.put((byte) 0x00) // P1 Parameter 1
					.put((byte) 0x00) // P2 Parameter 2
					.put((byte) name.length) // Lc
					.put(name).put((byte) 0x00); // Le

			return new Response(transceive(buff.array()));
		}

		public Response selectByName(byte... name) {
			ByteBuffer buff = ByteBuffer.allocate(name.length + 6);
			buff.put((byte) 0x00) // CLA Class
					.put((byte) 0xA4) // INS Instruction
					.put((byte) 0x04) // P1 Parameter 1
					.put((byte) 0x00) // P2 Parameter 2
					.put((byte) name.length) // Lc
					.put(name).put((byte) 0x00); // Le

			return new Response(transceive(buff.array()));
		}
		public Response selectFM() {
			ByteBuffer buff = ByteBuffer.allocate(7);
			buff.put((byte) 0x00) // CLA Class
					.put((byte) 0xA4) // INS Instruction
					.put((byte) 0x00) // P1 Parameter 1
					.put((byte) 0x00) // P2 Parameter 2
					.put((byte) 0x02) // Lc
					.put((byte) 0x3F)
					.put((byte) 0x00); // Le

			return new Response(transceive(buff.array()));
		}
		//擦除FM下所有文件
		public Response eraseFMAllFiles() {
			ByteBuffer buff = ByteBuffer.allocate(5);
			buff.put((byte) 0x80) // CLA Class
					.put((byte) 0x0E) // INS Instruction
					.put((byte) 0x00) // P1 Parameter 1
					.put((byte) 0x00) // P2 Parameter 2
					.put((byte) 0x00); // Lc
					
			return new Response(transceive(buff.array()));
		}
		public Response createKeyfile(byte p1,byte p2, byte size[],byte file_id,byte right,byte byte7) {
			ByteBuffer buff = ByteBuffer.allocate(12);
			buff.put((byte) 0x80) // CLA Class
					.put((byte) 0xE0) // INS Instruction
					.put((byte) p1) // P1 Parameter 1
					.put((byte) p2) // P2 Parameter 2
					.put((byte) 0x07) // Lc
					.put((byte) 0x3F) 
					.put((byte) size[0]) 
					.put((byte) size[1]) 
					.put((byte) file_id)
					.put((byte) right) 
					.put((byte) 0xFF)
					.put((byte) byte7); 

			return new Response(transceive(buff.array()));
		}
		public Response createFileRoadKey(byte useright,byte changright, byte errercounts,byte[] pwd) {
			ByteBuffer buff = ByteBuffer.allocate(10+pwd.length);
			if(pwd.length != 8 && pwd.length != 10){
				Log.i("nick","password length not right!!!!");
				return null;
			}
			buff.put((byte) 0x80) // CLA Class
					.put((byte) 0xD4) // INS Instruction
					.put((byte) 0x01) // P1 Parameter 1
					.put((byte) 0x00) // P2 Parameter 2
					.put((byte) ((pwd.length == 8)?0x0d:0x0f)) // Lc
					.put((byte) 0x36) 
					.put((byte) useright) 
					.put((byte) useright) 
					.put((byte) 0xFF)
					.put((byte) errercounts)
					.put(pwd); 

			return new Response(transceive(buff.array()));
		}
		//圈存密钥
		public Response createCreditLoadKey(byte useright,byte changright, byte keyversion,byte[] pwd) {
			ByteBuffer buff = ByteBuffer.allocate(10+pwd.length);
			if(pwd.length != 8 && pwd.length != 10){
				Log.i("nick","password length not right!!!!");
				return null;
			}
			buff.put((byte) 0x80) // CLA Class
					.put((byte) 0xD4) // INS Instruction
					.put((byte) 0x01) // P1 Parameter 1
					.put((byte) 0x00) // P2 Parameter 2  //密钥标识
					.put((byte) ((pwd.length == 8)?0x0d:0x0f)) // Lc
					.put((byte) 0x3F) 
					.put((byte) useright) 
					.put((byte) changright) 
					.put((byte) keyversion)        //版本号
					.put((byte) 0x00)        //算法标识
					.put(pwd); 

			return new Response(transceive(buff.array()));
		}
		
		//口令密钥
		public Response createPINKey(byte key_id,byte useright,byte status, byte error_num,byte[] pwd) {
					ByteBuffer buff = ByteBuffer.allocate(10+pwd.length);
					if(pwd.length > 8 || pwd.length < 2){
						Log.i("nick","password length not right!!!!");
						return null;
					}
					buff.put((byte) 0x80) // CLA Class
							.put((byte) 0xD4) // INS Instruction
							.put((byte) 0x01) // P1 Parameter 1
							.put((byte) key_id) // P2 Parameter 2  //密钥标识
							.put((byte) (pwd.length + 5)) // Lc
							.put((byte) 0x3A) 
							.put((byte) useright) 
							.put((byte) 0xEF) 
							.put((byte) status)        //版本号
							.put((byte) error_num)        //算法标识
							.put(pwd); 

					return new Response(transceive(buff.array()));
				}
			//解锁口令密钥
			public Response createUnlockPINKey(byte key_id,byte useright,byte changeriht, byte error_num,byte[] pwd) {
							ByteBuffer buff = ByteBuffer.allocate(10+pwd.length);
							if(pwd.length != 8 && pwd.length != 10){
								Log.i("nick","password length not right!!!!");
								return null;
							}
							buff.put((byte) 0x80) // CLA Class
									.put((byte) 0xD4) // INS Instruction
									.put((byte) 0x01) // P1 Parameter 1
									.put((byte) key_id) // P2 Parameter 2  //密钥标识
									.put((byte) (pwd.length + 5)) // Lc
									.put((byte) 0x37) 
									.put((byte) useright) 
									.put((byte) changeriht)
									.put((byte) 0xFF)        
									.put((byte) error_num)        //算法标识
									.put(pwd); 

							return new Response(transceive(buff.array()));
						}
		
		public Response createExternalAuthenticateKey(byte useright,byte changright, byte afterstatus, byte errercounts,byte[] pwd) {
			ByteBuffer buff = ByteBuffer.allocate(10+pwd.length);
			if(pwd.length != 8 && pwd.length != 10){
				Log.i("nick","password length not right!!!!");
				return null;
			}
			buff.put((byte) 0x80) // CLA Class
					.put((byte) 0xD4) // INS Instruction
					.put((byte) 0x01) // P1 Parameter 1
					.put((byte) 0x00) // P2 Parameter 2
					.put((byte) ((pwd.length == 8)?0x0d:0x0f)) // Lc
					.put((byte) 0x39) 
					.put((byte) useright) 
					.put((byte) useright) 
					.put((byte) afterstatus)
					.put((byte) errercounts) 
					.put(pwd); 

			return new Response(transceive(buff.array()));
		}
		//获取4字节随机数
		public Response getChallenge() {
			ByteBuffer buff = ByteBuffer.allocate(5);
			buff.put((byte) 0x00) // CLA Class
					.put((byte) 0x84) // INS Instruction
					.put((byte) 00) // P1 Parameter 1
					.put((byte) 00) // P2 Parameter 2
					.put((byte) 04); // Le
			return new Response(transceive(buff.array()));
		}
		public Response externalAuthenticate(byte... data) {
			ByteBuffer buff = ByteBuffer.allocate(5+data.length);
			buff.put((byte) 0x00) // CLA Class
					.put((byte) 0x82) // INS Instruction
					.put((byte) 00) // P1 Parameter 1
					.put((byte) 0x00) // P2 Parameter 2
					.put((byte) data.length) // Le
					.put(data);
			return new Response(transceive(buff.array()));
		}

		public Response createDFfile(byte[] file_id,byte[] file_size,byte createright,byte eraseright,byte app_id,
				byte clas, byte... name) {
			ByteBuffer buff = ByteBuffer.allocate(name.length+13);
			buff.put((byte) 0x80) // CLA Class
					.put((byte) 0xE0) // INS Instruction
					.put((byte) file_id[0]) // P1 Parameter 1
					.put((byte) file_id[1]) // P2 Parameter 2
					.put((byte) (name.length+8)) // Lc
					.put((byte)0x38)
					.put((byte)file_size[0])
					.put((byte)file_size[1])
					.put((byte)createright)
					.put((byte)eraseright)
					.put((byte)app_id)
					.put((byte)clas)
					.put((byte)0xff)
					.put(name); // data

			return new Response(transceive(buff.array()));
		}
		public Response createEFfile(byte[] file_id,byte byte1,byte[] file_size,byte readright,byte writeright,byte byte7) {
			ByteBuffer buff = ByteBuffer.allocate(12);
			buff.put((byte) 0x80) // CLA Class
					.put((byte) 0xE0) // INS Instruction
					.put((byte) file_id[0]) // P1 Parameter 1
					.put((byte) file_id[1]) // P2 Parameter 2
					.put((byte) 0x07) // Lc
					.put((byte)byte1)
					.put((byte)file_size[0])
					.put((byte)file_size[1])
					.put((byte)readright)
					.put((byte)writeright)
					.put((byte)0xff)
					.put((byte)byte7); // data

			return new Response(transceive(buff.array()));
		}
		//建立电子钱包文件
		/*
		 * recordfileid : 交易明细的文件标识ID
		 * */
		public Response createPBOCfile(byte[] file_id,byte useright,byte recordfileid) {
			ByteBuffer buff = ByteBuffer.allocate(12);
			buff.put((byte) 0x80) // CLA Class
					.put((byte) 0xE0) // INS Instruction
					.put((byte) file_id[0]) // P1 Parameter 1
					.put((byte) file_id[1]) // P2 Parameter 2
					.put((byte) 0x07) // Lc
					.put((byte) 0x2F)
					.put((byte) 0x02)
					.put((byte) 0x08)
					.put((byte) useright)
					.put((byte) 0x00)
					.put((byte)0xff)
					.put((byte)recordfileid); // data

			return new Response(transceive(buff.array()));
		}
		
			//初始化圈存
				/*
				 * keyfile_id: 圈存密钥的标识
				 * monery［4］： 待存入的金钱
				 * pos_id［6］：pos机的编号
				 * */
		public Response initializeForLoad(byte keyfile_id,byte[] monery,byte[] pos_id) {
					ByteBuffer buff = ByteBuffer.allocate(17);
					buff.put((byte) 0x80) // CLA Class
							.put((byte) 0x50) // INS Instruction
							.put((byte) 0x00) // P1 Parameter 1
							.put((byte) 0x02) // P2 Parameter 2 //电子钱包  01：电子存折
							.put((byte) 0x0B) // Lc
							.put((byte) keyfile_id)
							.put(monery)
							.put(pos_id)
							.put((byte)0x10); // data

					return new Response(transceive(buff.array()));
				}
				
		

		public void connect() {
			try {
				nfcTag.connect();
			} catch (Exception e) {
			}
		}

		public void close() {
			try {
				nfcTag.close();
			} catch (Exception e) {
			}
		}

		public byte[] transceive(final byte[] cmd) {
			try {
				return nfcTag.transceive(cmd);
			} catch (Exception e) {
				return Response.ERROR;
			}
		}
	}

	public static final short SW_NO_ERROR = (short) 0x9000;
	public static final short SW_BYTES_REMAINING_00 = 0x6100;
	public static final short SW_WRONG_LENGTH = 0x6700;
	public static final short SW_SECURITY_STATUS_NOT_SATISFIED = 0x6982;
	public static final short SW_FILE_INVALID = 0x6983;
	public static final short SW_DATA_INVALID = 0x6984;
	public static final short SW_CONDITIONS_NOT_SATISFIED = 0x6985;
	public static final short SW_COMMAND_NOT_ALLOWED = 0x6986;
	public static final short SW_APPLET_SELECT_FAILED = 0x6999;
	public static final short SW_WRONG_DATA = 0x6A80;
	public static final short SW_FUNC_NOT_SUPPORTED = 0x6A81;
	public static final short SW_FILE_NOT_FOUND = 0x6A82;
	public static final short SW_KEYFILE_NOT_FOUND = 0x6A88;
	public static final short SW_RECORD_NOT_FOUND = 0x6A83;
	public static final short SW_INCORRECT_P1P2 = 0x6A86;
	public static final short SW_WRONG_P1P2 = 0x6B00;
	public static final short SW_CORRECT_LENGTH_00 = 0x6C00;
	public static final short SW_INS_NOT_SUPPORTED = 0x6D00;
	public static final short SW_CLA_NOT_SUPPORTED = 0x6E00;
	public static final short SW_UNKNOWN = 0x6F00;
	public static final short SW_FILE_FULL = 0x6A84;
}
