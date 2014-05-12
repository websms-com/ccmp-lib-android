package net.ut11.ccmp.lib;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;

public class LocationHeader implements Header {

	private String location = null;

	public LocationHeader(String location) {
		this.location = location;
	}

	@Override
	public String getName() {
		return "Location";
	}

	@Override
	public String getValue() {
		return location;
	}

	@Override
	public HeaderElement[] getElements() throws ParseException {
		return null;
	}
}
