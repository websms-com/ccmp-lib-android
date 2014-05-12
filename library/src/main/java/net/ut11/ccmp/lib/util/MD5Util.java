package net.ut11.ccmp.lib.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

	public static String getMD5Sum(String text) {
		if (text == null) {
			return null;
		}

		text = text.replace('[', '(').replace('{', '(').replace('}', ')').replace(']', ')');

		MessageDigest mdEnc;
		try {
			mdEnc = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

		mdEnc.update(text.getBytes(), 0, text.length());

		String md5 = new BigInteger(1, mdEnc.digest()).toString(16) ;
		return md5;
	}
}
