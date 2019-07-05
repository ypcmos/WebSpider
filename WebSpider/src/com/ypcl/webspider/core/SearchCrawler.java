package com.ypcl.webspider.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ypcl.webspider.HostCondition;
import com.ypcl.webspider.ICrawlCondition;
import com.ypcl.webspider.IDataProcess;
import com.ypcl.webspider.core.struct.ConcurrentQueue;

public class SearchCrawler implements Runnable {
	private class CrawlWorker implements Runnable {
		private URL url = null;
		private ConcurrentQueue queue = ConcurrentQueue.getInstance();
		private AtomicInteger count = null;
		
		public CrawlWorker(URL url, AtomicInteger count) {
			this.url = url;
			this.count = count;
		}
		
		private void crawl() {		
			String pageContents = pageContext(url); 

			if (pageContents != null && pageContents.length() > 0) {
				retrieveLinks(url, pageContents);
				
				if (processer != null) {
					processer.deal(url, pageContents);
				}		
			}
		}
		
		//assume port is always 80
		private void retrieveLinks(URL pageUrl, String pageContents) { 
			Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(pageContents); 

			while (m.find() && !stopAll) { 
				String link = m.group(1).trim(); 

				if (link.isEmpty()) {
					continue; 
				}
	 
				if (link.charAt(0) == '#') { 
					continue; 
				} 

				if (link.toLowerCase().indexOf("mailto:") != -1) { 
					continue; 
				} 

				if(link.toLowerCase().indexOf("javascript") != -1) { 
					continue; 
				} 
				
				if (link.indexOf("://") == -1){ 
					if (link.charAt(0) == '/') {  			
						link ="http://" + pageUrl.getHost() + link; 
					} else {
						String path = pageUrl.getFile().substring(0, pageUrl.getFile().lastIndexOf('/') + 1); 
						link ="http://" + pageUrl.getHost() + path + link;
					} 
				}

				int index = link.indexOf('#'); 
				if (index != -1) { 
					link = link.substring(0, index); 
				} 
	 
				URL verifiedLink = verifyUrl(link); 
				if (verifiedLink == null) { 
					continue; 
				} 
 
				queue.add(link);
			} 
		} 
		
		private String pageContext(URL pageUrl) { 
			try { 
				BufferedReader reader = new BufferedReader(new InputStreamReader(pageUrl.openStream())); 
	 
				String line; 
				StringBuilder pageBuffer = new StringBuilder(); 
				while ((line =reader.readLine()) != null) { 
					pageBuffer.append(line); 
				} 

				return pageBuffer.toString(); 
			} catch (Exception e) { 
				if (isDebug) {
					e.printStackTrace();
				}
			} 

			return null; 
		} 
		
		@Override
		public void run() {	
			if (stopAll) {
				return;
			}
			if (SearchCrawler.this.isDebug) { 
				System.out.println(url); 
			}
			try {
				if (writer != null) {
					writer.write(url.toString() + "\r\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			crawl();
			count.decrementAndGet();
		}	
	}
	
	private Map<String, RobotsParser> disallowListCache = new HashMap<String, RobotsParser>();
	private List<String> result = new ArrayList<String>();
	private String startUrl;
	private int maxUrl;
	private ICrawlCondition condition;
	private IDataProcess processer;
	private boolean isDebug = false;
	private int threadNumber = 1;
	private BufferedWriter writer = null;
	private boolean stopAll = false;
	
	public SearchCrawler(String startUrl, int maxUrl, ICrawlCondition condition, IDataProcess processer, int threadNumber){ 
		this.startUrl = startUrl; 
		this.maxUrl = maxUrl; 
		this.condition = condition;
		this.processer = processer;
		this.threadNumber = threadNumber;
	}
	
	public SearchCrawler(String startUrl, int maxUrl, ICrawlCondition condition, IDataProcess processer) { 
		this(startUrl, maxUrl, condition, processer, 1);
	}
	
	public SearchCrawler(String startUrl, int maxUrl) {
		this(startUrl, maxUrl, null, null);
	}
	
	public SearchCrawler(String startUrl, int maxUrl, int threadNumber) {
		this(startUrl, maxUrl, null, null, threadNumber);
	}
	
	public SearchCrawler(String startUrl) {
		this(startUrl, -1, null, null);
	}

	public SearchCrawler setDebug(boolean is) {
		isDebug = is;
		return this;
	}
	
	public List<String> getResult() { 
		return result; 
	} 

	private CrawlWorker newCrawlWorkerInstance(URL url, AtomicInteger count) {
		return new CrawlWorker(url, count);
	}
	
	public void run() {
		crawl(); 
	} 

	public static boolean isURL(String str) {
		if (str == null) {
			return false;
		}
		
        str = str.toLowerCase();
        String regex = "^((https|http|ftp|rtsp|mms)?://)"  
                + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?"   
               + "(([0-9]{1,3}\\.){3}[0-9]{1,3}"
                 + "|" 
                 + "([0-9a-z_!~*'()-]+\\.)*"
                 + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." 
                + "[a-z]{2,6})" 
                + "(:[0-9]{1,4})?" 
                + "((/?)|" 
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";  
        return Pattern.matches(regex, str);
    }
	
	//only http 
	private URL verifyUrl(String url) {
		if (!isURL(url)) {
			return null;
		}
		
		if (!url.toLowerCase().startsWith("http://")) {
			return null; 
		}
		
		URL verifiedUrl = null; 
		try { 
			verifiedUrl = new URL(url);;
		} catch (Exception e) { 
			return null; 
		} 
		return verifiedUrl; 
	} 

	private boolean isAllowed(URL urlToCheck) {   
		String host = urlToCheck.getHost().toLowerCase();
		RobotsParser robots = disallowListCache.get(host);   
		if (condition != null && !condition.matches(host)) {
			return false;
		}
		
		if (robots == null) {   
			robots = new RobotsParser(host);   
			try {   
				robots.commonParse();
				disallowListCache.put(host, robots);   
			} catch (Exception e) {   
				return true; 
			}   
		}
		String file = urlToCheck.getFile();
		return robots.isAllowed(file); 
	}   

	//BFS
	public List<String> crawl() {
		int count = 1;

		ExecutorService pool = Executors.newFixedThreadPool(threadNumber);
		ConcurrentQueue queue = ConcurrentQueue.getInstance();
		queue.clear();
		
		try {
			writer = new BufferedWriter(new FileWriter("urlrecords.txt"));
		} catch (IOException e1) {
			writer = null;
		}
		queue.add(startUrl);
		String url = queue.fetch();
		try {
			byte[] sign = {0, 0};
			AtomicInteger threadActiveCount = new AtomicInteger(0);
			
			do {
				URL verifiedUrl = verifyUrl(url); 
				if (verifiedUrl != null && isAllowed(verifiedUrl)) {

					threadActiveCount.incrementAndGet();
					pool.execute(newCrawlWorkerInstance(verifiedUrl, threadActiveCount));
					if (count++ > maxUrl && maxUrl != -1) {
						break;
					}
				}

				//can not change the order
				sign[0] = (byte) (threadActiveCount.get() != 0 ? 1 : 0);
				sign[1] = (byte) ((url = queue.fetch()) != null ? 1 : 0);
			} while((sign[0] | sign[1]) == 1 && !Thread.interrupted()); 
		} finally {
			stopAll = true;
			queue.clear();
			pool.shutdownNow();
			while(!pool.isTerminated()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				if (writer != null) {
					writer.flush();
					writer.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if (isDebug) {
			System.out.println("pages number:" + count);
		}
		return result; 
	} 

	public static void main(String[] args) {  
		String url = "http://ee.seu.edu.cn";
		SearchCrawler crawler = new SearchCrawler(url, -1, new HostCondition(url), null, 1);
		crawler.setDebug(true);
		long startTime = System.currentTimeMillis();
		crawler.run(); 
		long useTime = System.currentTimeMillis() - startTime;
        System.out.println("time:" + useTime / 1000 + "s"); 
	} 
}
