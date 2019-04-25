package Slicer;

import javax.swing.JFrame;

public class Driver
{
	public static void main(String[] args)
	{ 
	  JFrame frame = new JFrame("!Slicer");
	  frame.setSize(1400, 1000);
	  frame.setLocation(0, 0);
	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  frame.setContentPane(new IO());
	  frame.setVisible(true);
	}
}