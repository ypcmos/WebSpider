package com.ypcl.webspider.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
	http://www.jd.com/robots.txt: 
	
	User-agent: *             
	Disallow: /?*     
	Disallow: /pop/*.html  
	Disallow: /pinpai/*.html?*      
	User-agent: EtaoSpider   
	Disallow: /
	User-agent: YisouSpider
	Disallow: /
	User-agent: HuihuiSpider
	Disallow: /
	User-agent: 360Spider
	Disallow: /
	User-agent: GwdangSpider
	Disallow: /
	User-agent: WochachaSpider
	Disallow: /
*/ 

public class RobotsParser {
	private List<String> disallowList = null;
	private String host = null;
	static final private String httpLabel = "http://",
			fileName = "robots.txt",
			clientLabel = "User-agent:", 
			commonLabel = "*", 
			disableLabel = "Disallow:",
			noteLabel = "#";
	
	public RobotsParser(String host) {
		this.host = host;
	}
	
	public String getHost() {
		return host;
	}
	
	public RobotsParser commonParse() throws IOException {
		disallowList = new ArrayList<String>();
		URL robotsFileUrl = new URL(httpLabel + host + "/" + fileName); 
		BufferedReader reader = new BufferedReader(new InputStreamReader(robotsFileUrl.openStream()));   
		
		String line = null;   
		while ((line = reader.readLine()) != null) {
			if (line.indexOf(clientLabel) == 0) {
				if (!line.split(":")[1].trim().equals(commonLabel)) {
					break;
				}
			}
			
			if (line.indexOf(disableLabel) == 0) { 
				String disallowPath = line.substring(disableLabel.length()); 
				int commentIndex = disallowPath.indexOf(noteLabel);   
				if(commentIndex != - 1) {   
					disallowPath = disallowPath.substring(0, commentIndex);
				}   

				disallowPath = disallowPath.trim();   
				disallowList.add(disallowPath);
			}   
		} 
		reader.close();
		return this;
	}
	
	public boolean isAllowed(String url) {
		boolean ret = true;
		
		if (!url.isEmpty()) {
			for (String dis : disallowList) {
				if (equalsExt(dis, url)) {
					ret = false;
					System.out.println(url);
					break;
				}
			}
		}
		return ret;
	}
	
	static private boolean equalsExt(String c, String x) {
		String cc = c.replace(commonLabel, "\\w*").replace("?", "\\?");
		Pattern p = Pattern.compile(cc);
		Matcher m = p.matcher(x);
		return m.matches();
	}
}
