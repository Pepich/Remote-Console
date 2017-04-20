package com.redstoner.remote_console.protected_classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/** This class deals with all encryption related tasks
 * 
 * @author Pepich1851 */
class Ciphers
{
	protected static Cipher RSA_DECODE;
	private final SecretKey AESKey;
	private int encode = 0, decode = 0;
	private static PrivateKey RSA_PRIVATE_KEY;
	
	/** Creates the corresponding Cipher object used for encrypted AES communication. One per user.
	 * 
	 * @param key the private key to use - will be tested on creation
	 * @throws NoSuchAlgorithmException if your JRE is missing the required AES implementation. Dafuq?
	 * @throws NoSuchPaddingException if your JRE is missing the required AES implementation. Dafuq?
	 * @throws InvalidKeyException will be thrown if an invalid key is provided
	 * @throws InvalidAlgorithmParameterException should not be thrown
	 * @throws ClassNotFoundException if your JRE is missing the required AES implementation. Dafuq?
	 * @throws IllegalBlockSizeException should not be thrown
	 * @throws BadPaddingException should not be thrown
	 * @throws IOException should not be thrown */
	protected Ciphers(final SecretKey AESKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException, ClassNotFoundException, IllegalBlockSizeException,
			BadPaddingException, IOException
	{
		this.AESKey = AESKey;
		Main.logger.info((String) new SealedObject("AES key test succesfull!", getNextAESEncode())
				.getObject(getNextAESDecode()));
		encode = 0;
		decode = 0;
	}
	
	/** This method will get the next cryptographically secure AES_Encode cipher. Will get out of sync if no message is sent with it.
	 * 
	 * @return the cipher
	 * @throws NoSuchAlgorithmException if your JRE is missing the required AES implementation. Dafuq?
	 * @throws NoSuchPaddingException if your JRE is missing the required AES implementation. Dafuq?
	 * @throws InvalidKeyException if the provided key is not a proper AES key
	 * @throws InvalidAlgorithmParameterException should not be thrown */
	protected synchronized Cipher getNextAESEncode() throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException
	{
		Cipher AES_ENCODE = Cipher.getInstance("AES/CTR/NoPadding");
		byte[] AES_ENCODE_IV = getRandomBytes(AES_ENCODE.getBlockSize(), encode);
		IvParameterSpec AES_ENCODE_PS = new IvParameterSpec(AES_ENCODE_IV);
		AES_ENCODE.init(Cipher.ENCRYPT_MODE, AESKey, AES_ENCODE_PS);
		encode++;
		return AES_ENCODE;
	}
	
	/** This method will get the next cryptographically secure AES_Decode cipher. Will get out of sync if no message was previously received.
	 * 
	 * @return the cipher
	 * @throws InvalidKeyException if the provided key is not a proper AES key
	 * @throws InvalidAlgorithmParameterException should not be thrown.
	 * @throws NoSuchAlgorithmException if your JRE is missing the required AES implementation. Dafuq?
	 * @throws NoSuchPaddingException if your JRE is missing the required AES implementation. Dafuq? */
	protected synchronized Cipher getNextAESDecode() throws InvalidKeyException, InvalidAlgorithmParameterException,
			NoSuchAlgorithmException, NoSuchPaddingException
	{
		Cipher AES_DECODE = Cipher.getInstance("AES/CTR/NoPadding");
		byte[] AES_DECODE_IV = getRandomBytes(AES_DECODE.getBlockSize(), decode);
		IvParameterSpec AES_DECODE_PS = new IvParameterSpec(AES_DECODE_IV);
		AES_DECODE.init(Cipher.DECRYPT_MODE, AESKey, AES_DECODE_PS);
		decode++;
		return AES_DECODE;
	}
	
	/** Initializes the RSA encryption
	 * 
	 * @throws NoSuchAlgorithmException if your JRE is missing the required RSA implementation. Dafuq?
	 * @throws NoSuchPaddingException if your JRE is missing the required RSA implementation. Dafuq?
	 * @throws IOException if there is no private/public key pair to be found
	 * @throws InvalidKeySpecException should not be thrown
	 * @throws InvalidKeyException if an invalid private/public key pair was detected
	 * @throws ClassNotFoundException if your JRE is missing the required RSA implementation. Dafuq?
	 * @throws IllegalBlockSizeException should not be thrown
	 * @throws BadPaddingException should not be thrown
	 * @throws InvalidAlgorithmParameterException should not be thrown */
	public static void initRSA() throws NoSuchAlgorithmException, NoSuchPaddingException, IOException,
			InvalidKeySpecException, InvalidKeyException, ClassNotFoundException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException
	{
		File privateKeyFile = new File(Main.getDataLocation(), "rsa_private_key");
		Main.logger.info("Trying to load the rsa key file from " + privateKeyFile.getAbsolutePath());
		FileInputStream fileInputStream = new FileInputStream(privateKeyFile);
		byte[] encodedPrivateKey = new byte[(int) privateKeyFile.length()];
		fileInputStream.read(encodedPrivateKey);
		fileInputStream.close();
		KeyFactory keyFac = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
		RSA_PRIVATE_KEY = keyFac.generatePrivate(privateKeySpec);
		RSA_DECODE = Cipher.getInstance("RSA");
		RSA_DECODE.init(Cipher.DECRYPT_MODE, RSA_PRIVATE_KEY);
		Main.logger.info("Found RSA key and loaded it!");
	}
	
	/** This method will generate a pseudo-random, seed-based array of bytes.
	 * 
	 * @param length the desired length of the array
	 * @param seed the seed used to generate the byte[]
	 * @return the generated array of bytes */
	public static byte[] getRandomBytes(int length, int seed)
	{
		byte[] bytes = new byte[length];
		String s = "" + seed;
		byte[] sbytes = s.getBytes();
		for (int i = 0; i <= length / s.length(); i++)
		{
			for (int j = 0; j < s.length() && i * s.length() + j < length; j++)
			{
				bytes[i * s.length() + j] = (byte) (sbytes[j] + Math.pow(10, i));
			}
		}
		return bytes;
	}
}
