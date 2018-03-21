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

/**
 * 
 * @author gpthomas
 * Class that stores the static keys used by the FieldSecurity class to help in encrypting /decrypting values
 */
public final class Base64 {

	/* P R I V A T E F I E L D S */

	/** The equals sign (=) as a byte. */
	private final static byte EQUALS_SIGN = (byte) '=';
	/** Preferred encoding. */
	private final static String PREFERRED_ENCODING = "UTF-8";
	private final static byte WHITE_SPACE_ENC = -5; // Indicates white space in
	// encoding
	private final static byte EQUALS_SIGN_ENC = -1; // Indicates equals sign in
	// encoding

	/* U R L S A F E B A S E 6 4 A L P H A B E T */
	private final static byte[] _URL_SAFE_ALPHABET = { (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E',
			(byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N',
			(byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W',
			(byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f',
			(byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o',
			(byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x',
			(byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
			(byte) '7', (byte) '8', (byte) '9', (byte) '-', (byte) '_' };

	/**
	 * Used in decoding URL- and Filename-safe dialects of Base64.
	 */
	private final static byte[] _URL_SAFE_DECODABET = { -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal
			// 0
			// -
			// 8
			-5, -5, // Whitespace: Tab and Linefeed
			-9, -9, // Decimal 11 - 12
			-5, // Whitespace: Carriage Return
			-9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 -
																// 26
			-9, -9, -9, -9, -9, // Decimal 27 - 31
			-5, // Whitespace: Space
			-9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
			-9, // Plus sign at decimal 43
			-9, // Decimal 44
			62, // Minus sign at decimal 45
			-9, // Decimal 46
			-9, // Slash at decimal 47
			52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
			-9, -9, -9, // Decimal 58 - 60
			-1, // Equals sign at decimal 61
			-9, -9, -9, // Decimal 62 - 64
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through
															// 'N'
			14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O'
															// through 'Z'
			-9, -9, -9, -9, // Decimal 91 - 94
			63, // Underscore at decimal 95
			-9, // Decimal 96
			26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a'
																// through
			// 'm'
			39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n'
																// through
			// 'z'
			-9, -9, -9, -9 // Decimal 123 - 126
	};

	// ******** D E T E R M I N E W H I C H A L H A B E T ********

	/**
	 * Returns the alphabet byte array.
	 */
	private final static byte[] getAlphabet() {

		return _URL_SAFE_ALPHABET;
	} // end getAlphabet

	/**
	 * Returns the decode byte array.
	 */
	private final static byte[] getDecodabet() {

		return _URL_SAFE_DECODABET;
	} // end getDecodabet

	/** Defeats instantiation. */
	private Base64() {

	}

	/* E N C O D I N G M E T H O D S */

	private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination,
			int destOffset) {

		byte[] ALPHABET = getAlphabet();
		int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
				| (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
				| (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);
		switch (numSigBytes) {
		case 3:
			destination[destOffset] = ALPHABET[(inBuff >>> 18)];
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
			destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];
			return destination;

		case 2:
			destination[destOffset] = ALPHABET[(inBuff >>> 18)];
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
			destination[destOffset + 3] = EQUALS_SIGN;
			return destination;

		case 1:
			destination[destOffset] = ALPHABET[(inBuff >>> 18)];
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = EQUALS_SIGN;
			destination[destOffset + 3] = EQUALS_SIGN;
			return destination;

		default:
			return destination;
		} // end switch
	} // end encode3to4

	public static String encodeBytes(byte[] source) {

		int len = source.length;
		int off = 0;
		int len43 = len * 4 / 3;
		byte[] outBuff = new byte[(len43) // Main 4:3
				+ ((len % 3) > 0 ? 4 : 0)]; // Account for padding

		int d = 0;
		int e = 0;
		int len2 = len - 2;
		int lineLength = 0;

		for (; d < len2; d += 3, e += 4) {
			encode3to4(source, d + off, 3, outBuff, e);
			lineLength += 4;
		} // end for: each piece of array
		if (d < len) {
			encode3to4(source, d + off, len - d, outBuff, e);
			e += 4;
		} // end if: some padding needed

		// Return value according to relevant encoding.
		try {
			return new String(outBuff, 0, e, PREFERRED_ENCODING);
		} // end try
		catch (java.io.UnsupportedEncodingException uue) {
			return new String(outBuff, 0, e);
		} // end catch

	} // end encodeBytes

	/* D E C O D I N G M E T H O D S */

	private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset) {

		byte[] DECODABET = getDecodabet();

		// Example: Dk==
		if (source[srcOffset + 2] == EQUALS_SIGN) {
			int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
					| ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12);

			destination[destOffset] = (byte) (outBuff >>> 16);
			return 1;
		}

		// Example: DkL=
		else if (source[srcOffset + 3] == EQUALS_SIGN) {
			int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
					| ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
					| ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6);

			destination[destOffset] = (byte) (outBuff >>> 16);
			destination[destOffset + 1] = (byte) (outBuff >>> 8);
			return 2;
		}

		// Example: DkLE
		else {
			try {
				int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18)
						| ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
						| ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6)
						| ((DECODABET[source[srcOffset + 3]] & 0xFF));

				destination[destOffset] = (byte) (outBuff >> 16);
				destination[destOffset + 1] = (byte) (outBuff >> 8);
				destination[destOffset + 2] = (byte) (outBuff);
				return 3;
			} catch (Exception e) {

				return -1;
			} // end catch
		}
	} // end decodeToBytes

	public static byte[] decode(byte[] source) {

		int len = source.length;
		byte[] DECODABET = getDecodabet();

		int len34 = len * 3 / 4;
		byte[] outBuff = new byte[len34]; // Upper limit on size of output
		int outBuffPosn = 0;

		byte[] b4 = new byte[4];
		int b4Posn = 0;
		int i = 0;
		byte sbiCrop = 0;
		byte sbiDecode = 0;
		for (i = 0; i < len; i++) {
			sbiCrop = (byte) (source[i] & 0x7f); // Only the low seven bits
			sbiDecode = DECODABET[sbiCrop];

			if (sbiDecode >= WHITE_SPACE_ENC) // White space, Equals sign or
												// better
			{
				if (sbiDecode >= EQUALS_SIGN_ENC) {
					b4[b4Posn++] = sbiCrop;
					if (b4Posn > 3) {
						outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
						b4Posn = 0;

						// If that was the equals sign, break out of 'for' loop
						if (sbiCrop == EQUALS_SIGN)
							break;
					} // end if: quartet built

				} // end if: equals sign or better

			} // end if: white space, equals sign or better
			else {
				return null;
			} // end else:
		} // each input character

		byte[] out = new byte[outBuffPosn];
		System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
		return out;
	} // end decode

	public static byte[] decode(String s) {

		byte[] bytes;
		bytes = s.getBytes();

		// Decode
		bytes = decode(bytes);

		return bytes;
	} // end decode

} // end class Base64
