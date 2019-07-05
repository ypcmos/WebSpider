package com.ypcl.webspider;

public interface ICrawlCondition {
	public boolean matches(String host);
}
