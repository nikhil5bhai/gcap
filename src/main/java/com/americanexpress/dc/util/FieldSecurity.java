/*
* -------------------------------------------------------------------------
*
* (C) Copyright / American Express, Inc. All rights reserved.
* The contents of this file represent American Express trade secrets and
* are confidential. Use outside of American Express is prohibited and in
* violation of copyright law.
*
* -------------------------------------------------------------------------
*/
package com.americanexpress.dc.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.americanexpress.wss.shr.authorization.token.SecurityToken;

/**
 * 
 * @author 
 * This class is taken from AccountHub and provides encryption capabilities based on java crypto libraries.
 * This will be used to encrypt the ids being passed to FINS and other backend services until the Voltage based
 * encryption becomes available.
 */

public final class FieldSecurity {

	/**
	 * 
	 */
	public FieldSecurity() {

	}

	/**
	 * 
	 */
	private SecretKeySpec secretKey;

	/**
	 * Intialize encryption to use the Security Token that was passed in, and set the secretkey.
	 * 
	 * @param token
	 * @throws Exception
	 */
	public FieldSecurity(SecurityToken token) throws Exception {

		// Get the private GUID
		String privateGUID = token.getUniversalID();

		if (privateGUID != null) {
			try {
				byte[] securityTokenInBytes = privateGUID.getBytes("UTF8");
				// Using Private GUID along with the static key for encrypting
				// the data
				byte[] encryptionKey = getKey(securityTokenInBytes);
				secretKey = new SecretKeySpec(encryptionKey, "AES");
			} catch (UnsupportedEncodingException e) {
				throw new Exception(e.getMessage());
			}
		}

	}

	/**
	 * Method to encrypt a value.
	 * 
	 * @param fieldId 	- The identifier of the field being encrypted
	 * @param plainText	- The source value being encrypted
	 * @return			- The encrypted value 
	 * @throws Exception
	 */
	public String encrypt(String fieldId, String plainText) throws Exception {
		return encrypt(fieldId, plainText, secretKey);
	}

	/**
	 * Method to decrypt an encrypted value.
	 * 
	 * @param fieldId	- The identifier of the field being decrypted
	 * @param cryptText - The encrypted text to be decrypted
	 * @return			- The decrypted value
	 * @throws Exception
	 */
	public String decrypt(String fieldId, String cryptText) throws Exception {
		return decrypt(fieldId, cryptText, secretKey);
	}

	/**
	 * Method to encrypt a value.
	 * 
	 * @param fieldId 	- The identifier of the field being encrypted
	 * @param plainText	- The source value being encrypted
	 * @param key		- The secret key used to encrypt the value
	 * @return
	 * @throws Exception
	 */
	private static String encrypt(String fieldId, String plainText, SecretKeySpec key) throws Exception {
		try {
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] cipherText = aesCipher.doFinal(((fieldId + ":" + plainText).getBytes()));

			return Base64.encodeBytes(cipherText).toString();
		} catch (Exception e) {

			throw new Exception(e.getMessage());
		}
	}

	/**
	 * Method to decrypt an encrypted value.
	 * 
	 * @param fieldId	- The identifier of the field being decrypted
	 * @param cryptText - The encrypted text to be decrypted
	 * @param key		- The secret key used to decrypt the text.
	 * @return
	 * @throws Exception
	 */
	private static String decrypt(String fieldId, String cryptText, SecretKeySpec key) throws Exception {
		try {
			Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.DECRYPT_MODE, key);
			byte[] decodedCipherText = Base64.decode(cryptText);
			String plainText = new String(aesCipher.doFinal(decodedCipherText));

			if (plainText.startsWith(fieldId + ":")) {
				plainText = plainText.substring((fieldId + ":").length(), plainText.length());
			} else {
				throw new Exception("Exception while Decrpytion");
			}

			return plainText;
		} catch (Exception e) {

			throw new Exception(e.getMessage());
		}
	}

	/**
	 * Internal method to generate an MD5 digest as a byte array, based on the security token passed in and a static key.
	 * 
	 * @param securityToken - byte array representing the security token
	 * @return				- byte array representing the 
	 * @throws Exception
	 */
	private static byte[] getKey(byte[] securityToken) throws Exception {

		// The security token is an object. The private GUID would be extracted,
		// converted to 16 bytes of data to use as another part of the combined
		// key.
		// the MD5 hash algorithm is used to combine the two byte arrays and
		// create
		// a single security key
		// which is 128 bit (16 bytes) long.

		byte[] privateGUID = securityToken;
		byte[] staticKey = getStaticKey();

		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(privateGUID);
			digest.update(staticKey);
			byte[] key = digest.digest();
			return key;

		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e.getMessage());
		}
	}

	/**
	 * @return -static hard coded key
	 */
	private static byte[] getStaticKey() {
		final byte[] staticKey = { (byte) 0xAE, (byte) 0xC3, (byte) 0x44, (byte) 0x02, (byte) 0x53, (byte) 0x64,
				(byte) 0x07, (byte) 0x61, (byte) 0xF2, (byte) 0xBC, (byte) 0x15, (byte) 0xC2, (byte) 0xBF, (byte) 0x87,
				(byte) 0x08, (byte) 0xCD };

		return (staticKey);
	}

}
