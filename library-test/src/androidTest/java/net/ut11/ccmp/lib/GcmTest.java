package net.ut11.ccmp.lib;

import android.test.AndroidTestCase;

import net.ut11.ccmp.lib.net.gcm.GcmRegistration;
import net.ut11.ccmp.lib.util.LibPreferences;

public class GcmTest extends AndroidTestCase {

	private LibPreferences prefs;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		prefs = LibApp.getLibPreferences();
		prefs.setGcmRegistrationId(null);
		prefs.setGcmAppVersion(0);
		prefs.setGcmNeedsDeviceUpdate(false);
	}

	public void testGetRegistrationId() {
		assertNull(prefs.getGcmRegistrationId());

		GcmRegistration.checkRegistration();
		assertNotNull(prefs.getGcmRegistrationId());
	}

	public void testCheckVersionCode() {
		String fake = "47110815";
		prefs.setDeviceToken(fake);

		assertNull(prefs.getGcmRegistrationId());

		GcmRegistration.checkRegistration();
		assertNotNull(prefs.getGcmRegistrationId());

		prefs.setGcmRegistrationId(fake);

		assertEquals(GcmRegistration.getAppVersion(), prefs.getGcmAppVersion());
		GcmRegistration.checkRegistration();
		assertEquals(fake, prefs.getGcmRegistrationId());

		prefs.setGcmAppVersion(0);
		GcmRegistration.checkRegistration();
		assertFalse(fake.equals(prefs.getGcmRegistrationId()));
	}

	public void testNeedUpdate() {
		assertFalse(prefs.gcmNeedsDeviceUpdate());
		GcmRegistration.checkRegistration();
		assertTrue(prefs.gcmNeedsDeviceUpdate());
	}
}
