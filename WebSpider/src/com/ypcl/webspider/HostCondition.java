package com.ypcl.webspider;

import java.net.MalformedURLException;
import java.net.URL;

public class HostCondition implements ICrawlCondition {
	private String host = null;
	
	public HostCondition(String url) {
		try {
			host = new URL(url).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean matches(String host) {
		return this.host.equals(host);
	}
}
