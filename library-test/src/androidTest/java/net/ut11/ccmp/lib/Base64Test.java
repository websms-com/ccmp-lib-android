package net.ut11.ccmp.lib;

import android.util.Base64;

import junit.framework.TestCase;

public class Base64Test extends TestCase {

	public void testEncode() {
		assertEquals("VGVzdCBUZXN0IFRlc3QKIy8=", Base64.encodeToString("Test Test Test\n#/".getBytes(), Base64.NO_WRAP));
	}

	public void testDecode() {
		assertEquals("Test Test Test\n#/", new String(Base64.decode("VGVzdCBUZXN0IFRlc3QKIy8=", Base64.NO_WRAP)));
	}
}
