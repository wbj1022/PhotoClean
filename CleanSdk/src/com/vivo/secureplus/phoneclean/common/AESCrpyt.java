package com.vivo.secureplus.phoneclean.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.vivo.secureplus.phoneclean.utils.FileUtils;

public class AESCrpyt {
	private static String TYPE = "AES";
    private static String SEED = "YZHQSRZHMMSHZHCH";
    private static byte[] SEED_BYTE;
    static{
        SEED_BYTE = SEED.getBytes();
    }
	private static int KEYSIZEAES128 = 16;
	private static int BUFFER_SIZE = 8192;

	private static Cipher getCipher(int mode, String key) {
		// mode =Cipher.DECRYPT_MODE or Cipher.ENCRYPT_MODE
		Cipher mCipher;
		byte[] keyPtr = new byte[KEYSIZEAES128];
		IvParameterSpec ivParam = new IvParameterSpec(keyPtr);
		byte[] passPtr = key.getBytes();
		try {
			mCipher = Cipher.getInstance(TYPE + "/CBC/PKCS5Padding");
			for (int i = 0; i < KEYSIZEAES128; i++) {
				if (i < passPtr.length)
					keyPtr[i] = passPtr[i];
				else
					keyPtr[i] = 0;
			}
			SecretKeySpec keySpec = new SecretKeySpec(keyPtr, TYPE);
			mCipher.init(mode, keySpec, ivParam);
			return mCipher;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * decrypt
	 * 
	 * @param srcFile
	 * @param destFile
	 * @param privateKey
	 */
	public static void decrypt(String srcFile, String destFile,
			String privateKey) {

		byte[] readBuffer = new byte[BUFFER_SIZE];
		Cipher deCipher = getCipher(Cipher.DECRYPT_MODE, privateKey);
		if (deCipher == null)
			return; // init failed.

		CipherInputStream fis = null;
		BufferedOutputStream fos = null;
		int size;
		try {
			fis = new CipherInputStream(new BufferedInputStream(
					new FileInputStream(srcFile)), deCipher);
			fos = new BufferedOutputStream(new FileOutputStream(
					FileUtils.mkdirFiles(destFile)));
			while ((size = fis.read(readBuffer, 0, BUFFER_SIZE)) >= 0) {
				fos.write(readBuffer, 0, size);
			}
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
			if (fos != null) {
				try {
					fos.flush();
				} catch (IOException e) {
				}
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * encrypt
	 * 
	 * @param srcFile
	 * @param destFile
	 * @param privateKey
	 */
	public static void crypt(String srcFile, String destFile, String privateKey) {

		byte[] readBuffer = new byte[BUFFER_SIZE];
		Cipher enCipher = getCipher(Cipher.ENCRYPT_MODE, privateKey);
		if (enCipher == null)
			return; // init failed.

		CipherOutputStream fos = null;
		BufferedInputStream fis = null;
		int size;
		try {
			fos = new CipherOutputStream(new BufferedOutputStream(
					new FileOutputStream(destFile)), enCipher);
			fis = new BufferedInputStream(new FileInputStream(
					FileUtils.mkdirFiles(srcFile)));
			while ((size = fis.read(readBuffer, 0, BUFFER_SIZE)) >= 0) {
				fos.write(readBuffer, 0, size);
			}
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
			if (fos != null) {
				try {
					fos.flush();
				} catch (IOException e) {
				}
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

    //decode "45,63,38,50,54,60,46,103,0,34,49,33,54,45,18,25,118,11,25,180,220,229,117,56,57,57" like string
    byte[] result = new byte[1024];
    public String decrypt(String encrypted) {
        if(encrypted == null)
            return null;
        String[] encoded = encrypted.split(",");
        int keyIndex = 0 ;
        int tmp;
        for(int i=0;i<encoded.length;i++){
            tmp = Integer.parseInt(encoded[i]);
            tmp = tmp^SEED_BYTE[keyIndex];
            result[i] = (byte)tmp;
            if (++keyIndex == SEED_BYTE.length)
            {
               keyIndex = 0;
            }
        }
        return new String(result, 0, encoded.length);
    }

}
