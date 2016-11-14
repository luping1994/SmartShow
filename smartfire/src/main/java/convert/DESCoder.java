package convert;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class DESCoder {
	 // 密钥  
    private static String key = "D0B29EBE";    ///定义密钥
    private static String IV="D0B29EBE";                   //定义偏移量
    // 加密
	 public static String DecryptDoNet(String message)
			throws Exception {
      byte[] bytesrc = Base64.decode(message.getBytes(), Base64.DEFAULT);
      Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
      DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
      IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
      cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
      byte[] retByte = cipher.doFinal(bytesrc);
      return new String(retByte);
  }
 //// 解密
	public static String EncryptAsDoNet(String message)
			throws Exception {
      Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
      DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
      SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
      IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
      byte[] encryptbyte = cipher.doFinal(message.getBytes());
      return new String(Base64.encode(encryptbyte, Base64.DEFAULT));
  }
}