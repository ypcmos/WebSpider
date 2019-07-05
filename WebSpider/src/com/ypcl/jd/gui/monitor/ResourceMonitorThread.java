package com.ypcl.jd.gui.monitor;

import java.util.TimerTask;

public class ResourceMonitorThread extends TimerTask{
	static final private Runtime runtime = Runtime.getRuntime();
	private long freeMemory = runtime.freeMemory(), totalMemory = runtime.totalMemory(), maxMemory = runtime.maxMemory();
	private long usedMemory = totalMemory - freeMemory;
	
	public static Runtime getRuntime() {
		return runtime;
	}
	
	public synchronized long getUsedMemory() {
		return usedMemory;
	}
	
	public synchronized long getFreeMemory() {
		return freeMemory;
	}
	
	public synchronized long getTotalMemory() {
		return totalMemory;
	}
	
	public long getMaxMemory() {
		return maxMemory;
	}
	
	@Override
	public void run() {	
		synchronized(this) {
			freeMemory = runtime.freeMemory();
			totalMemory = runtime.totalMemory();
			usedMemory = totalMemory - freeMemory;
		}
	}
}
