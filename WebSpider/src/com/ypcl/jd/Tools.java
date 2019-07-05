package com.ypcl.jd;

public class Tools {
	public static String unicode2String(String unicode) {	 
	    StringBuffer string = new StringBuffer(); 
	    String[] hex = unicode.split("\\\\u");
	 
	    for (int i = 1; i < hex.length; i++) {
	        int data = Integer.parseInt(hex[i], 16);
	        string.append((char) data);
	    }
	    return string.toString();
	}
}
