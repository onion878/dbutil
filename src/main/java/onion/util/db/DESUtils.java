package onion.util.db;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.SecureRandom;

/**
 * Created by Administrator on 2016-12-16.
 */
public class DESUtils {
    private static Key key;
    private static String KEY_STR;
    static {
        try{
        	KEY_STR=PropertyReader.get("key").toString().toString();
            KeyGenerator generator=KeyGenerator.getInstance("DES");
            SecureRandom secureRandom=SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(KEY_STR.getBytes());
            generator.init(secureRandom);
            key=generator.generateKey();
            generator=null;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    //加密
    public static String getEncryptString(String str){
        BASE64Encoder base64Encoder=new BASE64Encoder();
        try{
            byte[] bytes=str.getBytes("UTF-8");
            Cipher cipher=Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE,key);
            byte[] encrybyte=cipher.doFinal(bytes);
            return base64Encoder.encode(encrybyte);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    //解密
    public static String getDecryptString(String str){
        BASE64Decoder base64Decoder=new BASE64Decoder();
        try{
            byte[] strbyte=base64Decoder.decodeBuffer(str);
            Cipher cipher=Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE,key);
            byte[] decryptbyte=cipher.doFinal(strbyte);
            return new String(decryptbyte,"UTF-8");
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}


