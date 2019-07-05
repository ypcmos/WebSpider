package com.ypcl.jd.gui.monitor;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.ypcl.jd.gui.SwingConsole;

public class ResourceMonitorUI extends TimerTask{
	private class ClientFrame extends JFrame {
		private static final long serialVersionUID = -5187595586546852829L;
		JLabel jLabel1 = new JLabel(), jLabel2 = new JLabel(), jLabel3 = new JLabel(), jLabel4 = new JLabel();
		JTextField jTextField1 = new JTextField(), jTextField2 = new JTextField(), jTextField3 = new JTextField(), jTextField4 = new JTextField();
		JProgressBar jpb1 = new JProgressBar(), jpb2 = new JProgressBar();
		
		public ClientFrame() {
			setTitle("×ÊÔ´¼àÊÓÆ÷");
			jLabel1.setText("MaxMemory");
			jLabel1.setBounds(new Rectangle(10, 10, 100, 20));
			
			jLabel2.setText("TotalMemory");
			jLabel2.setBounds(new Rectangle(10, 40, 100, 20));
			
			jLabel3.setText("FreeMemory");
			jLabel3.setBounds(new Rectangle(10, 70, 100, 20));
			
			jLabel4.setText("UsedMemory");
			jLabel4.setBounds(new Rectangle(10, 100, 100, 20));
			
			this.setLayout(null);
			
			jTextField1.setBounds(new Rectangle(120, 10, 50, 20));
			jTextField2.setBounds(new Rectangle(120, 40, 50, 20));
			jTextField3.setBounds(new Rectangle(120, 70, 50, 20));
			jTextField4.setBounds(new Rectangle(120, 100, 50, 20));
			
			jpb1.setBounds(new Rectangle(180, 40, 200, 20));
			jpb2.setBounds(new Rectangle(180, 100, 200, 20));
			
			this.add(jLabel1, null);
			this.add(jLabel2, null);
			this.add(jLabel3, null);
			this.add(jLabel4, null);
			this.add(jTextField1, null);
			this.add(jTextField2, null);
			this.add(jTextField3, null);
			this.add(jTextField4, null);
			this.add(jpb1, null);
			this.add(jpb2, null);
			
			jTextField1.setEditable(false);
			jTextField2.setEditable(false);
			jTextField3.setEditable(false);
			jTextField4.setEditable(false);
			
			jpb1.setMaximum(100);  
		    jpb1.setMinimum(0);  
		    jpb1.setValue(0);
		     
		    jpb2.setMaximum(100);  
		    jpb2.setMinimum(0);  
		    jpb2.setValue(0);
		    
		    jpb1.setStringPainted(true);
		    jpb2.setStringPainted(true);
		    
		    this.addWindowListener(new WindowAdapter() {
		        @Override
		        public void windowClosing(WindowEvent arg0){
		        	ResourceMonitorUI.this.timer.cancel();
		        }
		    });
		}
		
		private void changeColor(JProgressBar jb, int s) {
			if (s < 40) {
				jb.setForeground(Color.green);
			} else if (s < 80) {
				jb.setForeground(Color.orange);
			} else {
				jb.setForeground(Color.red);
			}
		}
		
		public ClientFrame set(String max, String total, String free, String used, int scale1, int scale2) {
			jTextField1.setText(max);
			jTextField2.setText(total);
			jTextField3.setText(free);
			jTextField4.setText(used);
			
			jpb1.setValue(scale1);
			jpb2.setValue(scale2);
			
			changeColor(jpb1, scale1);
			changeColor(jpb2, scale2);
			return this;
		}
		
		@Override
		public void dispose() {
			ResourceMonitorUI.this.timer.cancel();
			super.dispose();
		}
	}
	ClientFrame frame = new ClientFrame();
	
	ResourceMonitorThread thread = null;
	private static final long g = 1024 * 1024 * 1024, m = 1024 * 1024, k = 1024;
	private int height, width;
	private Timer timer = null;
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public ResourceMonitorUI setWidth(int width) {
		this.width = width;
		return this;
	}
	
	public ResourceMonitorUI setHeight(int height) {
		this.height = height;
		return this;
	}
	
	public ClientFrame getFrame() {
		return frame;
	}
	
	public ResourceMonitorUI(ResourceMonitorThread thread, int width, int height, Timer timer) {
		this.thread = thread;
		this.width = width;
		this.height = height;
		this.timer = timer;
	}
	
	public ResourceMonitorUI(ResourceMonitorThread thread, Timer timer) {
		this(thread, 440, 200, timer);
	}
	
	private static String getSizeString(long size) {
		String ret;
		if (size > g) {
			ret = size / g + "G";
		} else if (size > m) {
			ret = size / m + "M";
		} else if (size > k) {
			ret = size / k + "K";
		} else {
			ret = size + "B";
		}
		
		return ret;
	}
	
	public void show() {
		SwingConsole.run(frame, width, height);
	}
	
	@Override
	public void run() {
		frame.set(getSizeString(thread.getMaxMemory()), getSizeString(thread.getTotalMemory()), getSizeString(thread.getFreeMemory()),
				getSizeString(thread.getUsedMemory()), (int)(thread.getTotalMemory() * 100 / thread.getMaxMemory()), (int)(thread.getUsedMemory() * 100 / thread.getMaxMemory()));
	}
	
	public void close() {
		frame.dispose();
	}
}