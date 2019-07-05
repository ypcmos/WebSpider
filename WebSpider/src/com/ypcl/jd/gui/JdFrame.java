package com.ypcl.jd.gui;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import com.ypcl.common.Configure;
import com.ypcl.jd.Commodity;
import com.ypcl.jd.MiningMachine;
import com.ypcl.jd.dao.CommodityDao;
import com.ypcl.jd.gui.monitor.ResourceMonitor;
import com.ypcl.webspider.core.SearchCrawler;

public class JdFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	JButton start, stop;
	JTable table;
	JScrollPane pane;
	DefaultTableModel tableModel = new DefaultTableModel();
	int width = 430, height = 500;
	JMenuBar menuBar;
	JLabel label;
	JLabel dbc;
	JTextArea text1;
	Timer timer;
	Thread t;
	Connection conn = null;
	Configure conf = null;
	
	public JdFrame() {
		this.setTitle("京东商品信息提取工具");
		this.setLayout(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Vector<String> tableHeadName = new Vector<String>(Arrays.asList("ID", "商品名", "价格/元")); 
		Vector<Vector<String>> rows = new Vector<Vector<String>>();
		tableModel.setDataVector(rows, tableHeadName);
		table = new JTable(tableModel);
		
		table.getColumnModel().getColumn(0).setPreferredWidth(70);
		table.getColumnModel().getColumn(1).setPreferredWidth(270);
		table.setBounds(new Rectangle(10, 10, 400, 350));
		start = new JButton("开始");
		start.setBounds(new Rectangle(120, 410, 70, 30));
		stop = new JButton("结束");
		stop.setBounds(new Rectangle(220, 410, 70, 30));
		
		pane = new JScrollPane(table);
		pane.setBounds(new Rectangle(10, 10, 400, 350));
		
		label = new JLabel("商品总数:");
		label.setBounds(new Rectangle(140, 370, 60, 20));
		dbc = new JLabel("数据库未连接");
		dbc.setBounds(new Rectangle(330, 420, 80, 20));
		text1 = new JTextArea();
		text1.setBounds(new Rectangle(210, 370, 50, 20));
		text1.setText("0");
		text1.setEditable(false);
		this.add(pane);
		this.add(start);
		this.add(stop);
		this.add(label);
		this.add(text1);
		this.add(dbc);
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		JMenu menu1 = new JMenu("工具");
		JMenu menu3 = new JMenu("操作");
		menuBar.add(menu1);
		menuBar.add(menu3);
		JMenuItem item1 = new JMenuItem("资源监视");
		menu1.add(item1);
		JMenu menu2 = new JMenu("数据库");
		menu1.add(menu2);
		JMenuItem item2 = new JMenuItem("创建表");
		JMenuItem item3 = new JMenuItem("删除表");
		JMenuItem item4 = new JMenuItem("连接");
		JMenuItem item5 = new JMenuItem("断开连接");
		menu2.add(item4);
		menu2.add(item2);
		menu2.add(item3);
		menu2.add(item5);
		
		JMenuItem item21 = new JMenuItem("清空信息");
		JMenuItem item22 = new JMenuItem("从数据库中读取");
		menu3.add(item21);
		menu3.add(item22);
			
		try {
			conf = new Configure();
		} catch (Exception e) {
			conf = null;
			JOptionPane.showMessageDialog(null, "无法找到配置文件，程序即将退出", "错误", JOptionPane.ERROR_MESSAGE);
		}
		
		if (conf.getBoolean("AutoConnect", false)) {
			connect();
		}
		
		item1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ResourceMonitor rn = new ResourceMonitor();
				rn.start();
			}		
		});
		
		item21.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clearTable();
				text1.setText("0");
			}		
		});
		
		item22.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clearTable();
				if (conn == null) {
					JOptionPane.showMessageDialog(null, "未连接数据库", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				CommodityDao dao = new CommodityDao(conn);
				List<Commodity> cs = null;
				try {
					cs = dao.getAll();
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
					
				for (Commodity c : cs) {
					addTableRow(c);
				}
				text1.setText(String.valueOf(cs.size()));
			}		
		});
		
		item2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (conn == null) {
						JOptionPane.showMessageDialog(null, "未连接数据库", "错误", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Statement stmt = conn.createStatement();
					stmt.execute("CREATE TABLE commodity (" +
					"auto_id INT IDENTITY(1,1) PRIMARY KEY," +
					"id INT NOT NULL," +
					"name VARCHAR(1000) NOT NULL," +
					"price REAL NOT NULL)");
					stmt.close();
					JOptionPane.showMessageDialog(JdFrame.this, "已成功创建表");
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, "无法创建表 :" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}		
		});
		
		
		item3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (conn == null) {
					JOptionPane.showMessageDialog(null, "未连接数据库", "错误", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Statement stmt;
				try {
					stmt = conn.createStatement();
					stmt.execute("DROP TABLE commodity");
					stmt.close();
					JOptionPane.showMessageDialog(JdFrame.this, "已成功删除表");
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, "无法删除表 :" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}		
		});
		
		item4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				connect();
			}		
		});
		
		item5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (conn != null) {
					try {
						conn.close();
						dbc.setText("数据库未连接");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}		
		});
		
		addListener();
	}
	
	private void connect() {
		try {
			Class.forName(conf.getString("DatabaseDriver", "null")).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		String url = conf.getString("DatabaseURL", "null");
		try {
			conn = DriverManager.getConnection(url, conf.getString("DatabaseUserName", "null"), conf.getString("DatabaseUserPassword", "null"));
			dbc.setText("数据库已连接");
		} catch (SQLException e) {
			conn = null;
			JOptionPane.showMessageDialog(null, "无法连接数据库:" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private void clearTable() {
		for (int index = tableModel.getRowCount() - 1; index >= 0; index--) {
			tableModel.removeRow(index);
        }	
	}
	
	private void addTableRow(Commodity c) {
		String price = c.price < 0 ? "未知" : String.valueOf(c.price);
		tableModel.addRow(new Vector<String>(Arrays.asList(String.valueOf(c.id), 
				c.name, price)));
		int rowCount = table.getRowCount();			
		table.getSelectionModel().setSelectionInterval(rowCount, rowCount);
		Rectangle rect = table.getCellRect(rowCount-1, 0, true);
		table.scrollRectToVisible(rect);
	}
	
	private void addListener() {
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				MiningMachine machine = new MiningMachine();
				
				String ignore = conf.getConf("IgnoreHost");
				
				if (ignore != null) {
					String [] is = ignore.trim().split(",");
					List<String> l = new LinkedList<String>();
					
					for (String s : is) {
						l.add(s.trim());
					}
					
					machine.setPasshost(l);
				}

				SearchCrawler crawler = new SearchCrawler(conf.getString("startURL", "http://www.jd.com/"), conf.getInt("MaxUrlsNumber", -1), machine, machine, conf.getInt("ThreadsNumber", 1)); 
				crawler.setDebug(true);
				t = new Thread(crawler);
				t.start();
				timer = new Timer();
				timer.schedule(new TimerTask() {
					int count = 0;
					int last = 0;
					int index = 0;
					CommodityDao dao = new CommodityDao(conn);
					@Override
					public void run() {	
						if (!t.isAlive()) {
							timer.cancel();
						}
						List<Commodity> list = machine.getCommodityList();
						int size = list.size();
						if (size > index) {							
							for (int i = index; i < size; i++) {
								Commodity c = list.get(i);
								addTableRow(c);	
								if (conn != null) {
									try {
										dao.insert(c);
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							}
							index = size;
							text1.setText(String.valueOf(size));				
						}
						int l = (count++) / 10;
						if (l != last) {
							last = l;
							//System.out.println("Takes:" + l + "s");
						}
						
					}				
				}, 0, 100);
			}
		});
		
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clean();
			}
			
		});
		
		 this.addWindowListener(new WindowAdapter() {
		        @Override
		        public void windowClosing(WindowEvent arg0){
		        	//clean();
		        }
		    });
	}
	
	private void clean() {
		if (t != null) {
			t.interrupt();
			while (t.isAlive()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void showFrame() {
		SwingConsole.run(this, width, height);
	}
	
	public static void main(String[] args) {
		JdFrame frame = new JdFrame();
		frame.showFrame();
	}
}
