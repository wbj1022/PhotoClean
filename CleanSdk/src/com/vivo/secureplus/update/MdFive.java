package com.vivo.secureplus.update;

import com.vivo.secureplus.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MdFive
{
	// =======================================
	// Constants
	// =======================================
	
	private static final String TAG = "MdFive";

	// =======================================
	// Methods
	// =======================================
	
    public static boolean checkMdFive(String mdFive, File updateFile)
    {
        if (mdFive == null || mdFive.equals("") || updateFile == null)
        {
        	LogUtils.logD("md5 String NULL or UpdateFile NULL. md5 = " + mdFive);
            return false;
        }

        String digest = calculateMdFive(updateFile);

        if (digest == null)
        {
            LogUtils.logD("md5 calculatedDigest NULL");
            return false;
        }

        LogUtils.logD("md5  Calculated digest: " + digest+
                ",Provided digest: " + mdFive);

        return digest.equalsIgnoreCase(mdFive);
    }

    public static String calculateMdFive(File updateFile)
    {
        if (!updateFile.isFile())
        {
        	return null;
        }
    	
    	MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            LogUtils.logE("Exception while getting Digest");
            return null;
        }
        
        InputStream is;
        try
        {
            is = new FileInputStream(updateFile);
        }
        catch (FileNotFoundException e)
        {
            LogUtils.logE("Exception while getting FileInputStream");
            return null;
        }
        
        byte[] buffer = new byte[8192];
        int read;
        try
        {
            while ((read = is.read(buffer)) > 0)
            {
                digest.update(buffer, 0, read);
            }
            byte[] mdFiveSum = digest.digest();
            BigInteger bigInt = new BigInteger(1, mdFiveSum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to process file for MD5", e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                LogUtils.logE("Exception on closing MD5 input stream");
            }
        }
    }
}
