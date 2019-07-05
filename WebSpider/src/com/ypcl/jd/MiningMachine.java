package com.ypcl.jd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.ypcl.jd.gui.monitor.ResourceMonitor;
import com.ypcl.webspider.ICrawlCondition;
import com.ypcl.webspider.IDataProcess;
import com.ypcl.webspider.core.SearchCrawler;

public class MiningMachine implements ICrawlCondition, IDataProcess {
	private List<Commodity> list = Collections.synchronizedList(new LinkedList<Commodity>());
	private final static Pattern hostPattern = Pattern.compile("\\S*\\.jd\\.com$", Pattern.CASE_INSENSITIVE);
	private final static String hostTarget = "item.jd.com";
	private List<String> passHost = null;
	@Override
	public void deal(URL url, String content) {
		if (url.getHost().equals(hostTarget)) {
			Commodity c = createCommodity(content);
			if (c != null) {
				System.out.println(c);
				list.add(c);
			}	
		}
	}

	public void setPasshost(List<String> passHost) {
		this.passHost = passHost;
		System.out.println(this.passHost);
	}
	
	public List<Commodity> getCommodityList() {
		return list;
	}
	
	@Override
	public boolean matches(String host) {
		if (!hostPattern.matcher(host).find()) {
			return false;
		}
		
		for (String s : passHost) {
			if (host.equals(s)) {
				return false;
			}
		}
		return true;
	}
	
	private Commodity createCommodity(String content) {
		Commodity c = null;
		int i = content.indexOf("product: {"), j = content.indexOf("skuidkey:");
		if (i > 0 && j > i) {
			c = new Commodity();
			String value = content.substring(i + 10, j);
			String[] ps = value.split(",");
			c.id = Integer.parseInt(ps[0].trim().split(":")[1].trim());
			String unicodeName = ps[1].trim().split(":")[1].trim();
			c.name = Tools.unicode2String(unicodeName.substring(1, unicodeName.length() - 1));
			try {
				c.price = getCommodityPrice(c.id);
			} catch (IOException e) {
				System.err.println("Can not get commodity information.");
				e.printStackTrace();
				c = null;
			} 
		}
		return c;
	}
	
	
	private static float getCommodityPrice(int id) throws IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						new URL("http://p.3.cn/prices/get?skuid=J_" + id).openStream()));   
		//[{"id":"J_60141843","p":"9.90","m":"69.90"}]
		String line = reader.readLine();   
		if (line != null && !line.isEmpty()) {
			String p = line.split(",")[1].trim().split(":")[1].trim();
			return Float.parseFloat(p.substring(1, p.length() - 1));
		}
		return -1;
	}
	
	public static void main(String[] args) {
		ResourceMonitor rn = new ResourceMonitor();
		rn.start();
		MiningMachine machine = new MiningMachine();
		SearchCrawler crawler = new SearchCrawler("http://item.jd.com/1257425.html", 1, machine, machine, 4); 
		//crawler.setDebug(true);
		Thread t = new Thread(crawler);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		rn.stop();
	}
}
