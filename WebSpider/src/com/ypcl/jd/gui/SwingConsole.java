package com.ypcl.jd.gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SwingConsole
{
	public static void run(final JFrame frame, final int width, final int height)
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				//
				//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(width, height);
				frame.setLocationRelativeTo(null);
				frame.setResizable(false);
				frame.setVisible(true);
			}
		});
	}
}
