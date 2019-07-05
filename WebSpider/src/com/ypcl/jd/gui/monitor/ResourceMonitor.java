package com.ypcl.jd.gui.monitor;

import java.util.Timer;

public class ResourceMonitor {
	private ResourceMonitorThread thread = new ResourceMonitorThread();
	private Timer timer = new Timer();
	private ResourceMonitorUI ui = new ResourceMonitorUI(thread, timer);
	private long period = 1000;
	
	public ResourceMonitor() {
		
	}
	
	public ResourceMonitorUI getUI() {
		return ui;
	}
	
	public ResourceMonitor setUI(ResourceMonitorUI ui) {
		this.ui = ui;
		return this;
	}
	
	public ResourceMonitor(long period) {
		this.period = period;
	}
	
	public void start() {	
		timer.schedule(thread, 0, period);
		ui.show();
		timer.schedule(ui, 0, period);
	}
	
	public void stop() {		
		ui.close();
	}
}
