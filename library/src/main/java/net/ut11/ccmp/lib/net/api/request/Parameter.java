package net.ut11.ccmp.lib.net.api.request;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class Parameter {
	private List<NameValuePair> parameter;

	public Parameter() {
		parameter = new ArrayList<NameValuePair>();
	}

	public void set(String name, String value) {
		parameter.add(new BasicNameValuePair(name, value));
	}

	public List<NameValuePair> getAll() {
		return parameter;
	}
}