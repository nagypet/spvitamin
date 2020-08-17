
/*
 * Copyright header
 * The ultimate spring based webservice template project.
 * Author Peter Nagy <nagy.peter.home@gmail.com>
 */

package hu.perit.crypto;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class CryptoUtil
{
    // 8-byte Salt
    private byte[] salt = {
            (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
            (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
    };
    // Iteration count
    private int iterationCount = 19;

    /**
     *
     * @param secretKey Key used to encrypt data
     * @param plainText Text input to be encrypted
     * @return Returns encrypted text
     *
     */
    public String encrypt(String secretKey, String plainText) throws CryptoException
    {
        if (!StringUtils.isNoneBlank(secretKey, plainText))
        {
            return null;
        }

        try
        {
            //Key generation for enc and desc
            KeySpec keySpec = new PBEKeySpec(secretKey.toCharArray(), salt, iterationCount);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            // Prepare the parameter to the ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

            //Enc process
            Cipher ecipher = Cipher.getInstance(key.getAlgorithm());
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            String charSet = "UTF-8";
            byte[] in = plainText.getBytes(charSet);
            byte[] out = ecipher.doFinal(in);
            return new String(Base64.getEncoder().encode(out));
        }
        catch (InvalidKeySpecException
                | InvalidKeyException
                | BadPaddingException
                | InvalidAlgorithmParameterException
                | NoSuchPaddingException
                | IllegalBlockSizeException
                | UnsupportedEncodingException
                | NoSuchAlgorithmException e)
        {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    /**
     * @param secretKey Key used to decrypt data
     * @param encryptedText encrypted text input to decrypt
     * @return Returns plain text after decryption
     */
    public String decrypt(String secretKey, String encryptedText) throws CryptoException
    {
        if (!StringUtils.isNoneBlank(secretKey, encryptedText))
        {
            return null;
        }

        try
        {
            //Key generation for enc and desc
            KeySpec keySpec = new PBEKeySpec(secretKey.toCharArray(), salt, iterationCount);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            // Prepare the parameter to the ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
            //Decryption process; same key will be used for decr
            Cipher dcipher = Cipher.getInstance(key.getAlgorithm());
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
            byte[] enc = Base64.getDecoder().decode(encryptedText);
            byte[] utf8 = dcipher.doFinal(enc);
            String charSet = "UTF-8";
            return new String(utf8, charSet);
        }
        catch (InvalidKeySpecException
                | InvalidKeyException
                | BadPaddingException
                | InvalidAlgorithmParameterException
                | NoSuchPaddingException
                | IllegalBlockSizeException
                | UnsupportedEncodingException
                | NoSuchAlgorithmException e)
        {
            throw new CryptoException(e.getMessage(), e);
        }
    }
}
