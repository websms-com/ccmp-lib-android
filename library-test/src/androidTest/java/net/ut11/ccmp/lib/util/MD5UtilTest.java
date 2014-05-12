package net.ut11.ccmp.lib.util;

import junit.framework.TestCase;

public class MD5UtilTest extends TestCase {

	private static final String TEXT = "public class MD5UtilTest extends TestCase #-+()'\\";
	private static final String MD5 = "3b6f0ced80ff9998fdf30a9590128d0c";

	public void testMD5() {
		assertEquals(MD5, MD5Util.getMD5Sum(TEXT));
	}

	public void testMD5Null() {
		assertNull(MD5Util.getMD5Sum(null));
	}
}
