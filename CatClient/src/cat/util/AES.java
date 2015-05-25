package cat.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	public static String password = "snow";

	public static String encrypt(String content, String password) {// 加密
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			String resultStr = parseByte2HexStr(result);
			return resultStr; // 加密
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解密
	 * 
	 * @param content
	 *            待解密内容
	 * @param password
	 *            解密密钥
	 * @return
	 */
	public static String decrypt(String contentStr, String password) {// 解密
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			byte[] content = parseHexStr2Byte(contentStr);
			byte[] result = cipher.doFinal(content);
			return new String(result);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将二进制转换成16进制
	 * 
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte[] buf) {

		byte add = getAdd(buf);//计算检验和
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		String hex = Integer.toHexString(add & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		//将检验和添加到末尾
		sb.append(hex.toUpperCase());
		return sb.toString();
	}

	/**
	 * 将16进制转换为二进制
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[(hexStr.length()-2) / 2];
		int i = 0;
		for (; i < (hexStr.length()-2) / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
					16);
			result[i] = (byte) (high * 16 + low);
		}
		// 得到传过来的检验和
		int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
		int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
		byte addfrom = (byte) (high * 16 + low);
		
		byte add = getAdd(result);
		//判断检验和是否相同
		if (add == addfrom) {
			return result;
		}else {
			return null;
		}
	}

	private static byte getAdd(byte[] result) {
		// TODO Auto-generated method stub
		// 重新计算检验和
				byte add = 0x0;
				for (int i = 0; i < result.length; i++) {
					if (((result[i] & 0x80) == 0x80) && ((add & 0x80) == 0x80)) {// 判断首位是否都为1
						// 若需要进位，则+0x1
						add += result[i];
						add += 0x1;
					} else {
						add += result[i];
					}
				}
				add = (byte) ~add;//取反
		return add;
	}

	public static void main(String[] args) {
		String content = "test";
		String password = "12345678";
		// 加密
		System.out.println("加密前：" + content);
		String encryptResultStr = encrypt(content, password);
		System.out.println("加密后：" + encryptResultStr);
		// 解密
		String decryptResult = decrypt(encryptResultStr, password);
		System.out.println("解密后：" + decryptResult);
	}

}